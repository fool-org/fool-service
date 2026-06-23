from __future__ import annotations

import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import textwrap
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT_PATH = ROOT / "scripts" / "check_repo_harness.py"


def load_harness_module():
    spec = importlib.util.spec_from_file_location("check_repo_harness", SCRIPT_PATH)
    if spec is None or spec.loader is None:
        raise AssertionError(f"Cannot load {SCRIPT_PATH}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def write_file(root: Path, relative_path: str, content: str) -> None:
    path = root / relative_path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(textwrap.dedent(content).lstrip(), encoding="utf-8")


class CheckRepoHarnessTest(unittest.TestCase):
    def write_minimum_harness(self, root: Path, module) -> None:
        write_file(
            root,
            "AGENTS.md",
            """
            # Agent Entrypoint

            ## Project Shape
            Maven multi-module backend with a Vue frontend.

            ## Source Of Truth
            Use docs/validation.md and docs/standards/.

            ## Validation
            Run python scripts/check_repo_harness.py.

            ## Delivery Evidence
            Record meaningful changes under agent_chats/.
            """,
        )
        write_file(
            root,
            "README.md",
            """
            # Test Repo

            See AGENTS.md, docs/validation.md, and scripts/check_repo_harness.py.
            """,
        )
        write_file(
            root,
            "docs/validation.md",
            """
            # Validation Harness

            ## Change-Type Matrix
            | Change type | Minimum check | Escalation |
            | --- | --- | --- |
            | Harness/docs | python scripts/check_repo_harness.py | Full build when commands change |

            ## Command Surface
            - python scripts/check_repo_harness.py

            ## Report Outputs
            - stdout
            - --report-json

            ## CI Gates
            - .github/workflows/repo-harness.yml

            ## Fallback / Skip Policy
            Skips must name the missing prerequisite and residual risk.
            """,
        )
        write_file(
            root,
            "docs/standards/README.md",
            "\n".join(
                [
                    "# Standard Engine",
                    "",
                    "## Standard Object Shape",
                    (
                        "Every standard defines intent, scope, enforcement, "
                        "evidence, repair path, and revision trigger."
                    ),
                    "",
                    "## Active Standards",
                    *[f"- {standard_id}" for standard_id in module.REQUIRED_STANDARD_IDS],
                    "",
                ]
            ),
        )
        for standard_id in module.REQUIRED_STANDARD_IDS:
            standard = module.STANDARDS[standard_id]
            write_file(
                root,
                f"docs/standards/{standard_id}.md",
                f"""
                # {standard_id}: Test Standard

                ## Intent
                Keep the harness behavior explicit.

                ## Scope
                Repository harness files.

                ## Automatic Enforcement
                {standard.enforcement}

                ## Evidence
                Harness report output.

                ## Repair Path
                Fix the missing harness file or marker.

                ## Revision Trigger
                Update when the standard changes.
                """,
            )
        write_file(
            root,
            "scripts/check_repo_harness.py",
            """
            REQUIRED_STANDARD_IDS = ()

            def validate_repo(root):
                return None
            """,
        )
        write_file(
            root,
            "scripts/standard_engine.py",
            """
            STANDARDS = {}

            def standard_catalog_payload():
                return {}
            """,
        )
        write_file(
            root,
            ".github/workflows/repo-harness.yml",
            """
            name: Repo Harness
            jobs:
              repo-harness:
                steps:
                  - run: python scripts/check_repo_harness.py
            """,
        )
        write_file(
            root,
            "tasks.md",
            """
            # Tasks

            ## Current Focus
            - [ ] Keep harness standards discoverable.

            ## Backlog
            - [ ] Add runtime evidence once runtime checks stabilize.
            """,
        )
        write_file(
            root,
            "agent_chats/README.md",
            """
            # Agent Chats

            Entries record Prompt, Scope, Changes, Validation, Risks, and Follow-ups.
            """,
        )

    def test_minimum_harness_passes_and_reports_standards(self) -> None:
        module = load_harness_module()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            self.write_minimum_harness(root, module)

            report = module.validate_repo(root)

            self.assertEqual([], report.errors)
            payload = report.to_payload()
            self.assertEqual("passed", payload["status"])
            self.assertIn("STD-HARNESS-001", payload["standards"])
            self.assertIn("AGENTS.md", payload["checked_files"])

    def test_missing_agent_entrypoint_is_actionable(self) -> None:
        module = load_harness_module()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            self.write_minimum_harness(root, module)
            (root / "AGENTS.md").unlink()

            report = module.validate_repo(root)

            self.assertIn("Missing required file: AGENTS.md", report.errors)
            self.assertEqual("failed", report.to_payload()["status"])

    def test_cli_writes_stable_json_report(self) -> None:
        module = load_harness_module()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            self.write_minimum_harness(root, module)
            report_path = root / "artifacts" / "repo-harness.json"

            exit_code = module.main(["--root", str(root), "--report-json", str(report_path)])

            self.assertEqual(0, exit_code)
            payload = json.loads(report_path.read_text(encoding="utf-8"))
            self.assertEqual("passed", payload["status"])
            self.assertEqual(str(root.resolve()), payload["root"])
            self.assertIn("checked_files", payload)


if __name__ == "__main__":
    unittest.main()
