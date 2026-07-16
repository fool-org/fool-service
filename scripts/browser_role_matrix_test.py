#!/usr/bin/env python3

from __future__ import annotations

import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

from harness.browser_role_matrix import (
    FIXTURE_USER_IDS,
    HarnessError,
    build_driver_input,
    build_manifest_payload,
    driver_command,
    ensure_fixture_identifiers_available,
    fixture_cleanup_sql,
    fixture_cleanup_verification,
    fixture_setup_sql,
    fixture_verification,
    legacy_password_hash,
    sanitize_network_event,
    sanitize_path,
    scan_artifacts,
    sha256_file,
    summary_payload,
)


ROOT = Path(__file__).resolve().parents[1]
DRIVER = ROOT / "scripts" / "harness" / "browser_driver.cjs"


class BrowserRoleMatrixTest(unittest.TestCase):
    def test_driver_command_keeps_secrets_off_argv(self) -> None:
        command = driver_command(DRIVER)

        self.assertEqual(["node", str(DRIVER)], command)
        self.assertNotIn("password", " ".join(command).lower())

    def test_build_driver_input_uses_stdin_secret_partition(self) -> None:
        payload = build_driver_input(
            run_id="run-1",
            base_url="http://localhost:8081",
            debug_port=9334,
            profile_dir=Path("/tmp/fool-browser-run-1"),
            artifact_dir=Path("/tmp/artifacts/run-1"),
            password="temporary-secret",
        )

        self.assertEqual(1, payload["schemaVersion"])
        self.assertNotIn("password", payload["run"])
        self.assertEqual(
            {"temporary-secret"},
            {
                credential["password"]
                for credential in payload["credentials"].values()
            },
        )

    def test_fixture_setup_uses_hashes_and_isolated_system_admin(self) -> None:
        sql = fixture_setup_sql("temporary-secret")

        self.assertNotIn("temporary-secret", sql)
        self.assertIn(
            legacy_password_hash("phase4-ordinary", "temporary-secret"),
            sql,
        )
        self.assertIn("'phase4-sysadmin'", sql)
        self.assertIn("SUBJECT_ID` = 'admin'", sql)
        self.assertNotIn("UPDATE `auth_user` SET `password`", sql)

    def test_fixture_cleanup_does_not_delete_real_admin(self) -> None:
        sql = fixture_cleanup_sql("request-1")

        self.assertIn("'phase4-sysadmin'", sql)
        self.assertNotIn("`id` IN ('admin'", sql)
        self.assertIn("WHERE `OWNER_USER_ID` IN", sql)
        self.assertNotIn("'request-1'", sql)

    def test_fixture_preflight_accepts_unused_identifiers(self) -> None:
        with patch(
            "harness.browser_role_matrix.run_mysql",
            return_value="\t".join("0" for _ in range(9)),
        ):
            ensure_fixture_identifiers_available()

    def test_fixture_preflight_rejects_any_identifier_conflict(self) -> None:
        with (
            patch(
                "harness.browser_role_matrix.run_mysql",
                return_value="0\t0\t0\t0\t0\t1\t0\t0\t0",
            ),
            self.assertRaisesRegex(HarnessError, "fixture identifiers unavailable"),
        ):
            ensure_fixture_identifiers_available()

    def test_fixture_verification_requires_exact_admin_binding_clone(self) -> None:
        credentials = "\n".join(
            f"{user_id}\tBCRYPT"
            for user_id in FIXTURE_USER_IDS
        )
        with patch(
            "harness.browser_role_matrix.run_mysql",
            return_value=f"{credentials}\n12\n12\n0",
        ):
            result = fixture_verification()

        self.assertEqual("PASS", result["status"])
        self.assertEqual(
            {"source": 12, "clone": 12, "missing": 0},
            result["systemAdminBindings"],
        )

        with patch(
            "harness.browser_role_matrix.run_mysql",
            return_value=f"{credentials}\n12\n11\n1",
        ):
            result = fixture_verification()

        self.assertEqual("FAIL", result["status"])

    def test_fixture_cleanup_verification_requires_every_fixture_to_be_gone(self) -> None:
        with patch(
            "harness.browser_role_matrix.run_mysql",
            return_value="\t".join("0" for _ in range(11)),
        ):
            result = fixture_cleanup_verification("request-1")

        self.assertEqual("PASS", result["status"])
        self.assertTrue(all(value == 0 for value in result["remaining"].values()))

        with patch(
            "harness.browser_role_matrix.run_mysql",
            return_value="0\t0\t0\t0\t0\t0\t1\t0\t0\t0\t0",
        ):
            result = fixture_cleanup_verification("request-1")

        self.assertEqual("FAIL", result["status"])
        self.assertEqual(1, result["remaining"]["departments"])

    def test_sanitize_network_event_keeps_only_allowlisted_fields(self) -> None:
        event = sanitize_network_event(
            {
                "role": "ordinary",
                "method": "post",
                "path": "http://localhost:8081/api/v1/data/saveobj?token=no#fragment",
                "status": 403,
                "traceId": "trace-1",
                "bodyKeys": ["SaveObj", "SaveObj", "token", "bad key"],
                "bodyEmpty": True,
                "headers": {"Authorization": "Bearer secret"},
                "body": {"password": "secret"},
            }
        )

        self.assertEqual("/api/v1/data/saveobj", event["path"])
        self.assertEqual(
            ["SaveObj"],
            event["bodyKeys"],
        )
        self.assertTrue(event["bodyEmpty"])
        self.assertNotIn("headers", event)
        self.assertNotIn("body", event)

    def test_sanitize_path_drops_query_and_fragment(self) -> None:
        self.assertEqual(
            "/api/v1/actions/a",
            sanitize_path("http://localhost:8081/api/v1/actions/a?x=1#secret"),
        )

    def test_scan_artifacts_reports_rule_without_leaking_match(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "report.json").write_text(
                '{"Authorization":"Bearer abcdefghijklmnop"}',
                encoding="utf-8",
            )

            findings = scan_artifacts(root, ("abcdefghijklmnop",))

        self.assertTrue(findings)
        rendered = json.dumps(findings)
        self.assertNotIn("abcdefghijklmnop", rendered)

    def test_manifest_uses_relative_paths_and_sha256(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            path = root / "summary.json"
            path.write_text("{}\n", encoding="utf-8")
            expected_hash = sha256_file(path)

            manifest = build_manifest_payload(root)

        self.assertEqual("summary.json", manifest["files"][0]["path"])
        self.assertEqual(expected_hash, manifest["files"][0]["sha256"])

    def test_summary_fails_when_any_security_check_fails(self) -> None:
        result = {
            "status": "PASS",
            "engine": "chrome-cdp",
            "roles": {
                role: {"status": "PASS"}
                for role in (
                    "ordinary",
                    "departmentAdmin",
                    "approver",
                    "systemAdmin",
                )
            },
            "highRisk": {
                "actionRequestId": "a1",
                "previewStatus": "AWAITING_APPROVAL",
                "selfApproval": {
                    "status": 403,
                    "reason": "SELF_APPROVAL_FORBIDDEN",
                },
                "approval": {"finalStatus": "APPROVED"},
                "executeWithoutStepUp": {
                    "status": 403,
                    "reason": "STEP_UP_REQUIRED",
                    "requestStatus": "APPROVED",
                },
                "executed": False,
            },
            "security": {"dynamicSecretScan": "PASS"},
        }

        summary = summary_payload(
            run_id="run-1",
            driver_result=result,
            credential_check={"status": "PASS"},
            audit_check={"status": "PASS"},
            cleanup_check={"status": "PASS"},
            profile_removed=True,
            artifact_findings=[{"path": "x", "rule": "exact-secret"}],
        )

        self.assertEqual("FAIL", summary["status"])
        self.assertEqual("FAIL", summary["checks"]["artifactSecurity"])

    def test_node_driver_self_test_passes(self) -> None:
        if not DRIVER.exists():
            self.skipTest("browser driver has not been generated")
        completed = subprocess.run(
            ["node", str(DRIVER), "--self-test"],
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
        )

        self.assertEqual(0, completed.returncode, completed.stderr)
        self.assertEqual("PASS", json.loads(completed.stdout)["status"])


if __name__ == "__main__":
    unittest.main()
