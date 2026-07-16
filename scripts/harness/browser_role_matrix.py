#!/usr/bin/env python3
"""Run the four-identity authorization matrix in a dedicated Chrome instance."""

from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
import mimetypes
import os
from pathlib import Path
import re
import secrets
import shutil
import signal
import subprocess
import sys
import tempfile
from typing import Any
from urllib.parse import urlsplit


ROOT = Path(__file__).resolve().parents[2]
DRIVER = Path(__file__).with_name("browser_driver.cjs")
DEFAULT_ARTIFACT_ROOT = ROOT / "artifacts" / "runs"
ROLE_USERS = {
    "ordinary": "phase4-ordinary",
    "departmentAdmin": "phase4-dept-admin",
    "approver": "phase4-approver",
    "systemAdmin": "phase4-sysadmin",
}
FIXTURE_USER_IDS = tuple(ROLE_USERS.values())
RUN_ID_RE = re.compile(r"[A-Za-z0-9._-]{1,96}")
SAFE_ID_RE = re.compile(r"[A-Za-z0-9._:-]{1,128}")
TEXT_ARTIFACT_SUFFIXES = frozenset({".json", ".md", ".txt", ".log", ".xml"})
SECRET_PATTERNS = (
    ("bearer-token", re.compile(r"(?i)\bbearer\s+[A-Za-z0-9._~+/-]{12,}")),
    ("authorization-header", re.compile(r"(?i)\bauthorization\s*[:=]")),
    (
        "sensitive-json-key",
        re.compile(
            r"""(?ix)
            ["']?(?:password|passwd|pwd|token|authorization|checkcode|
            checkcodekey|captcha|secret|connectionstring)["']?\s*[:=]
            """
        ),
    ),
    ("captcha-data-image", re.compile(r"(?i)data:image/[^;]+;base64,")),
    ("jdbc-url", re.compile(r"(?i)\bjdbc:[a-z0-9]+:")),
    ("mysql-url", re.compile(r"(?i)\bmysql://")),
    (
        "persisted-query-or-fragment",
        re.compile(r'(?i)"(?:path|url)"\s*:\s*"[^"]*[?#][^"]*"'),
    ),
)
SENSITIVE_BODY_KEYS = frozenset(
    {
        "authorization",
        "captcha",
        "checkcode",
        "checkcodekey",
        "password",
        "passwd",
        "pwd",
        "secret",
        "token",
    }
)


class HarnessError(RuntimeError):
    """A safe-to-report harness failure."""


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run the four-role UI/network authorization matrix in dedicated Chrome."
    )
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--base-url", default="http://localhost:8081")
    parser.add_argument("--chrome-debug-port", type=int, default=9334)
    parser.add_argument("--artifact-root", type=Path, default=DEFAULT_ARTIFACT_ROOT)
    parser.add_argument("--timeout", type=float, default=240.0)
    parser.add_argument("--driver", type=Path, default=DRIVER)
    args = parser.parse_args(argv)
    if not RUN_ID_RE.fullmatch(args.run_id):
        parser.error("--run-id must match [A-Za-z0-9._-]{1,96}")
    parsed = urlsplit(args.base_url)
    if parsed.scheme not in {"http", "https"} or not parsed.hostname:
        parser.error("--base-url must be an absolute HTTP(S) URL")
    if not 1024 <= args.chrome_debug_port <= 65535:
        parser.error("--chrome-debug-port must be between 1024 and 65535")
    if args.timeout <= 0:
        parser.error("--timeout must be positive")
    return args


def driver_command(driver_path: Path = DRIVER) -> list[str]:
    return ["node", str(driver_path)]


def sanitize_path(value: str) -> str:
    parsed = urlsplit(value)
    return parsed.path or "/"


def sanitize_text(value: object, forbidden_values: tuple[str, ...] = ()) -> str:
    text = str(value or "").replace("\r", " ").replace("\n", " ").strip()
    for secret in forbidden_values:
        if secret:
            text = text.replace(secret, "<redacted>")
    for _rule, pattern in SECRET_PATTERNS:
        text = pattern.sub("<redacted>", text)
    return text[:500]


def sanitize_network_event(event: dict[str, object]) -> dict[str, object]:
    result: dict[str, object] = {
        "role": sanitize_text(event.get("role")),
        "method": sanitize_text(event.get("method")).upper()[:12],
        "path": sanitize_path(str(event.get("path") or "/")),
        "status": int(event.get("status") or 0),
        "traceId": sanitize_text(event.get("traceId")),
    }
    body_keys = event.get("bodyKeys")
    if isinstance(body_keys, list):
        result["bodyKeys"] = sorted(
            {
                str(key)
                for key in body_keys
                if isinstance(key, str)
                and re.fullmatch(r"[A-Za-z][A-Za-z0-9_.-]{0,63}", key)
                and key.replace("_", "").replace("-", "").lower()
                not in SENSITIVE_BODY_KEYS
            }
        )
    if isinstance(event.get("bodyEmpty"), bool):
        result["bodyEmpty"] = event["bodyEmpty"]
    return result


def legacy_password_hash(user_id: str, raw_password: str) -> str:
    return hashlib.md5(f"{user_id}{raw_password}".encode("utf-8")).hexdigest()


def sql_quote(value: str) -> str:
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def fixture_setup_sql(raw_password: str) -> str:
    hashes = {
        user_id: legacy_password_hash(user_id, raw_password)
        for user_id in FIXTURE_USER_IDS
    }
    users = ",\n".join(
        f"  ({sql_quote(user_id)}, '', {sql_quote(display_name)}, "
        f"{sql_quote(hashes[user_id])}, CURRENT_TIMESTAMP(6))"
        for user_id, display_name in (
            ("phase4-ordinary", "Phase 4 Ordinary"),
            ("phase4-dept-admin", "Phase 4 Department Admin"),
            ("phase4-approver", "Phase 4 Independent Approver"),
            ("phase4-sysadmin", "Phase 4 System Administrator"),
        )
    )
    fixture_ids = ", ".join(sql_quote(item) for item in FIXTURE_USER_IDS)
    return f"""
START TRANSACTION;

DELETE approval
  FROM `FOOL_AGENT_APPROVAL` approval
  JOIN `FOOL_AGENT_ACTION_REQUEST` action_request
    ON action_request.`ACTION_REQUEST_ID` = approval.`ACTION_REQUEST_ID`
 WHERE action_request.`OWNER_USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AGENT_APPROVAL`
 WHERE `APPROVER_USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AGENT_ACTION_REQUEST`
 WHERE `OWNER_USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AUTHZ_BINDING`
 WHERE `BINDING_ID` LIKE 'phase4-%';
DELETE FROM `auth_user_role`
 WHERE `user_id` IN ({fixture_ids});
DELETE FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
 WHERE `SW_APP_AUTH_USER_ID` IN (91001, 91002, 91003, 91004);

INSERT INTO `auth_role` (`id`, `name`)
VALUES
  (9101, 'Phase 4 Ordinary Reader'),
  (9102, 'Phase 4 Department Administrator')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `auth_user` (`id`, `mobile`, `name`, `password`, `created_at`)
VALUES
{users}
ON DUPLICATE KEY UPDATE
  `mobile` = VALUES(`mobile`),
  `name` = VALUES(`name`),
  `password` = VALUES(`password`);

DELETE FROM `FOOL_AUTH_CREDENTIAL`
 WHERE `USER_ID` IN ({fixture_ids});

INSERT IGNORE INTO `auth_user_role` (`user_id`, `role_id`)
VALUES
  ('phase4-ordinary', 9101),
  ('phase4-dept-admin', 9102),
  ('phase4-approver', 9001),
  ('phase4-sysadmin', 1);

INSERT INTO `SW_APP_AUTH_DEPARTMENT`
  (`APP_DEP_ID`, `APP_DEP_NAME`, `APP_DEP_DEFAULTVIEW`,
   `SW_APP_AUTH_COMPANY_DepsAPP_COR_ID`)
VALUES
  (3001, 'Phase 4 Department A', 100, NULL),
  (3002, 'Phase 4 Department B', 100, NULL)
ON DUPLICATE KEY UPDATE
  `APP_DEP_NAME` = VALUES(`APP_DEP_NAME`),
  `APP_DEP_DEFAULTVIEW` = VALUES(`APP_DEP_DEFAULTVIEW`);

INSERT INTO `SW_APP_AUTH_USER`
  (`APP_AUTH_ID`, `APP_AUTH_USERID`, `APP_AUTH_USERLOGINNAME`, `APP_AUTH_DEP`)
VALUES
  (91001, 'phase4-ordinary', 'phase4-ordinary', 3001),
  (91002, 'phase4-dept-admin', 'phase4-dept-admin', 3002),
  (91003, 'phase4-approver', 'phase4-approver', 3002),
  (91004, 'phase4-sysadmin', 'phase4-sysadmin', NULL)
ON DUPLICATE KEY UPDATE
  `APP_AUTH_USERID` = VALUES(`APP_AUTH_USERID`),
  `APP_AUTH_USERLOGINNAME` = VALUES(`APP_AUTH_USERLOGINNAME`),
  `APP_AUTH_DEP` = VALUES(`APP_AUTH_DEP`);

INSERT IGNORE INTO `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
  (`SW_APP_AUTH_ROLE_ID`, `SW_APP_AUTH_USER_ID`)
VALUES
  (1, 91001),
  (1, 91002),
  (1, 91003),
  (1, 91004),
  (2, 91004);

INSERT INTO `FOOL_AUTHZ_DATA_POLICY`
  (`DATA_POLICY_ID`, `SCOPE_TYPE`, `FILTER_JSON`, `READABLE_FIELDS_JSON`,
   `WRITABLE_FIELDS_JSON`, `MASK_FIELDS_JSON`, `MAX_QUERY_ROWS`,
   `MAX_EXPORT_ROWS`, `LLM_POLICY_JSON`, `CREATED_AT`, `UPDATED_AT`)
VALUES
  (
    'phase4-ordinary-read',
    'EXPLICIT',
    '{{"all":[{{"op":"eq","field":"customer","value":"3001"}}]}}',
    '["orderId","symbol","amount","price","customer","state"]',
    '[]',
    '{{}}',
    25,
    0,
    '{{"sortableFields":["orderId","symbol","customer","state"],"classifications":{{}},"allowedProviders":["local"],"filterableFields":["orderId","symbol","customer","state"],"llmVisibleFields":["orderId","symbol","amount","price","customer","state"]}}',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
  ),
  (
    'phase4-department-read',
    'DEPARTMENT',
    '{{"field":"customer"}}',
    '["orderId","symbol","amount","price","customer","state"]',
    '[]',
    '{{}}',
    25,
    0,
    '{{"sortableFields":["orderId","symbol","customer","state"],"classifications":{{}},"allowedProviders":["local"],"filterableFields":["orderId","symbol","customer","state"],"llmVisibleFields":["orderId","symbol","amount","price","customer","state"]}}',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
  ),
  (
    'phase4-approver-read',
    'ALL',
    '{{}}',
    '["*"]',
    '[]',
    '{{}}',
    100,
    0,
    '{{"classifications":{{}},"allowedProviders":["local"],"llmVisibleFields":["*"]}}',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
  )
ON DUPLICATE KEY UPDATE
  `SCOPE_TYPE` = VALUES(`SCOPE_TYPE`),
  `FILTER_JSON` = VALUES(`FILTER_JSON`),
  `READABLE_FIELDS_JSON` = VALUES(`READABLE_FIELDS_JSON`),
  `WRITABLE_FIELDS_JSON` = VALUES(`WRITABLE_FIELDS_JSON`),
  `MASK_FIELDS_JSON` = VALUES(`MASK_FIELDS_JSON`),
  `MAX_QUERY_ROWS` = VALUES(`MAX_QUERY_ROWS`),
  `MAX_EXPORT_ROWS` = VALUES(`MAX_EXPORT_ROWS`),
  `LLM_POLICY_JSON` = VALUES(`LLM_POLICY_JSON`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_BINDING`
  (`BINDING_ID`, `SUBJECT_TYPE`, `SUBJECT_ID`, `PERMISSION_ID`, `EFFECT`,
   `APP_ID`, `DATABASE_ID`, `INCLUDE_CHILDREN`, `DATA_POLICY_ID`,
   `VALID_FROM`, `VALID_UNTIL`, `CREATED_AT`, `UPDATED_AT`)
VALUES
  ('phase4-ordinary-discover', 'ROLE', 'auth:9101', 'view-discover', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-ordinary-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-ordinary-read', 'ROLE', 'auth:9101', 'view-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-ordinary-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-ordinary-query', 'ROLE', 'auth:9101', 'view-query', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-ordinary-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-ordinary-model', 'ROLE', 'auth:9101', 'model-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-ordinary-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-dept-discover', 'ROLE', 'auth:9102', 'view-discover', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-department-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-dept-read', 'ROLE', 'auth:9102', 'view-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-department-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-dept-query', 'ROLE', 'auth:9102', 'view-query', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-department-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-dept-model', 'ROLE', 'auth:9102', 'model-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-department-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-approver-discover', 'ROLE', 'auth:9001', 'view-discover', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-approver-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-approver-read', 'ROLE', 'auth:9001', 'view-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-approver-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-approver-query', 'ROLE', 'auth:9001', 'view-query', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-approver-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
  ('phase4-approver-model', 'ROLE', 'auth:9001', 'model-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'phase4-approver-read', NULL, NULL,
   CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
  `SUBJECT_TYPE` = VALUES(`SUBJECT_TYPE`),
  `SUBJECT_ID` = VALUES(`SUBJECT_ID`),
  `PERMISSION_ID` = VALUES(`PERMISSION_ID`),
  `EFFECT` = VALUES(`EFFECT`),
  `APP_ID` = VALUES(`APP_ID`),
  `DATABASE_ID` = VALUES(`DATABASE_ID`),
  `INCLUDE_CHILDREN` = VALUES(`INCLUDE_CHILDREN`),
  `DATA_POLICY_ID` = VALUES(`DATA_POLICY_ID`),
  `VALID_FROM` = VALUES(`VALID_FROM`),
  `VALID_UNTIL` = VALUES(`VALID_UNTIL`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_BINDING`
  (`BINDING_ID`, `SUBJECT_TYPE`, `SUBJECT_ID`, `PERMISSION_ID`, `EFFECT`,
   `APP_ID`, `DATABASE_ID`, `INCLUDE_CHILDREN`, `DATA_POLICY_ID`,
   `VALID_FROM`, `VALID_UNTIL`, `CREATED_AT`, `UPDATED_AT`)
SELECT
  CONCAT('phase4-sysadmin-', SUBSTRING(`BINDING_ID`, 7)),
  'USER',
  'phase4-sysadmin',
  `PERMISSION_ID`,
  `EFFECT`,
  `APP_ID`,
  `DATABASE_ID`,
  `INCLUDE_CHILDREN`,
  `DATA_POLICY_ID`,
  `VALID_FROM`,
  `VALID_UNTIL`,
  CURRENT_TIMESTAMP(6),
  CURRENT_TIMESTAMP(6)
FROM `FOOL_AUTHZ_BINDING`
WHERE `SUBJECT_TYPE` = 'USER'
  AND `SUBJECT_ID` = 'admin'
  AND `BINDING_ID` LIKE 'admin-%'
ON DUPLICATE KEY UPDATE
  `PERMISSION_ID` = VALUES(`PERMISSION_ID`),
  `EFFECT` = VALUES(`EFFECT`),
  `APP_ID` = VALUES(`APP_ID`),
  `DATABASE_ID` = VALUES(`DATABASE_ID`),
  `INCLUDE_CHILDREN` = VALUES(`INCLUDE_CHILDREN`),
  `DATA_POLICY_ID` = VALUES(`DATA_POLICY_ID`),
  `VALID_FROM` = VALUES(`VALID_FROM`),
  `VALID_UNTIL` = VALUES(`VALID_UNTIL`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

COMMIT;
""".strip() + "\n"


def fixture_cleanup_sql(action_request_id: str | None) -> str:
    if action_request_id and not SAFE_ID_RE.fullmatch(action_request_id):
        raise HarnessError("driver returned an invalid action request id")
    fixture_ids = ", ".join(sql_quote(item) for item in FIXTURE_USER_IDS)
    return f"""
START TRANSACTION;

DELETE approval
  FROM `FOOL_AGENT_APPROVAL` approval
  JOIN `FOOL_AGENT_ACTION_REQUEST` action_request
    ON action_request.`ACTION_REQUEST_ID` = approval.`ACTION_REQUEST_ID`
 WHERE action_request.`OWNER_USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AGENT_APPROVAL`
 WHERE `APPROVER_USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AGENT_ACTION_REQUEST`
 WHERE `OWNER_USER_ID` IN ({fixture_ids});

DELETE FROM `FOOL_AUTH_CREDENTIAL`
 WHERE `USER_ID` IN ({fixture_ids});
DELETE FROM `FOOL_AUTHZ_BINDING`
 WHERE `BINDING_ID` LIKE 'phase4-%';
DELETE FROM `auth_user_role`
 WHERE `user_id` IN ({fixture_ids});
DELETE FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
 WHERE `SW_APP_AUTH_USER_ID` IN (91001, 91002, 91003, 91004);
DELETE FROM `SW_APP_AUTH_USER`
 WHERE `APP_AUTH_ID` IN (91001, 91002, 91003, 91004);
DELETE FROM `auth_user`
 WHERE `id` IN ({fixture_ids});
DELETE FROM `auth_role`
 WHERE `id` IN (9101, 9102);
DELETE FROM `FOOL_AUTHZ_DATA_POLICY`
 WHERE `DATA_POLICY_ID` IN
       ('phase4-ordinary-read', 'phase4-department-read', 'phase4-approver-read');
DELETE FROM `SW_APP_AUTH_DEPARTMENT`
 WHERE `APP_DEP_ID` IN (3001, 3002)
   AND NOT EXISTS (
     SELECT 1
       FROM `SW_APP_AUTH_USER`
      WHERE `APP_AUTH_DEP` = `SW_APP_AUTH_DEPARTMENT`.`APP_DEP_ID`
   );

COMMIT;
""".strip() + "\n"


def run_mysql(sql: str) -> str:
    completed = subprocess.run(
        [
            "docker",
            "compose",
            "exec",
            "-T",
            "mysql",
            "mysql",
            "-uroot",
            "-pPa88word",
            "-N",
            "-B",
            "car_wash",
        ],
        cwd=ROOT,
        input=sql,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if completed.returncode != 0:
        raise HarnessError(
            "fixture database command failed: "
            + sanitize_text(completed.stderr or completed.stdout)
        )
    return completed.stdout.strip()


def ensure_fixture_identifiers_available() -> None:
    fixture_ids = ", ".join(sql_quote(item) for item in FIXTURE_USER_IDS)
    output = run_mysql(f"""
SELECT
  (SELECT COUNT(*) FROM `auth_user` WHERE `id` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `FOOL_AUTH_CREDENTIAL` WHERE `USER_ID` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `auth_role` WHERE `id` IN (9101, 9102)),
  (SELECT COUNT(*) FROM `auth_user_role` WHERE `user_id` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `SW_APP_AUTH_USER`
    WHERE `APP_AUTH_ID` IN (91001, 91002, 91003, 91004)
       OR `APP_AUTH_USERID` IN ({fixture_ids})
       OR `APP_AUTH_USERLOGINNAME` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `SW_APP_AUTH_DEPARTMENT` WHERE `APP_DEP_ID` IN (3001, 3002)),
  (SELECT COUNT(*) FROM `FOOL_AUTHZ_DATA_POLICY`
    WHERE `DATA_POLICY_ID` IN
      ('phase4-ordinary-read', 'phase4-department-read', 'phase4-approver-read')),
  (SELECT COUNT(*) FROM `FOOL_AUTHZ_BINDING` WHERE `BINDING_ID` LIKE 'phase4-%'),
  (SELECT COUNT(*) FROM `FOOL_AGENT_ACTION_REQUEST`
    WHERE `OWNER_USER_ID` IN ({fixture_ids}));
""")
    counts = [int(value) for value in output.split("\t")] if output else []
    if len(counts) != 9 or any(counts):
        raise HarnessError("fixture identifiers unavailable")


def revoke_fixture_tokens() -> None:
    lua = """
local deleted = 0
for _, user in ipairs(ARGV) do
  local user_key = 'fool:auth:user:token-hash:' .. user
  local hash = redis.call('GET', user_key)
  if hash then
    deleted = deleted + redis.call(
      'DEL',
      'fool:auth:token:sha256:' .. hash,
      'fool:auth:absolute:sha256:' .. hash,
      'fool:auth:legacy:app:sha256:' .. hash,
      'fool:auth:legacy:db:sha256:' .. hash,
      'fool:auth:step-up:session:auth:' .. string.sub(hash, 1, 24)
    )
  end
  deleted = deleted + redis.call('DEL', user_key)
end
return deleted
""".strip()
    completed = subprocess.run(
        [
            "docker",
            "compose",
            "exec",
            "-T",
            "redis",
            "redis-cli",
            "--raw",
            "EVAL",
            lua,
            "0",
            *FIXTURE_USER_IDS,
        ],
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        check=False,
    )
    if completed.returncode != 0:
        raise HarnessError(
            "fixture token cleanup failed: "
            + sanitize_text(completed.stderr or completed.stdout)
        )


def fixture_verification() -> dict[str, object]:
    fixture_ids = ", ".join(sql_quote(item) for item in FIXTURE_USER_IDS)
    output = run_mysql(f"""
SELECT `USER_ID`, `ALGORITHM`
  FROM `FOOL_AUTH_CREDENTIAL`
 WHERE `USER_ID` IN ({fixture_ids})
 ORDER BY `USER_ID`;
SELECT COUNT(*)
  FROM `FOOL_AUTHZ_BINDING` source_binding
 WHERE source_binding.`SUBJECT_TYPE` = 'USER'
   AND source_binding.`SUBJECT_ID` = 'admin'
   AND source_binding.`BINDING_ID` LIKE 'admin-%';
SELECT COUNT(*)
  FROM `FOOL_AUTHZ_BINDING` clone_binding
 WHERE clone_binding.`SUBJECT_TYPE` = 'USER'
   AND clone_binding.`SUBJECT_ID` = 'phase4-sysadmin'
   AND clone_binding.`BINDING_ID` LIKE 'phase4-sysadmin-%';
SELECT COUNT(*)
  FROM `FOOL_AUTHZ_BINDING` source_binding
 WHERE source_binding.`SUBJECT_TYPE` = 'USER'
   AND source_binding.`SUBJECT_ID` = 'admin'
   AND source_binding.`BINDING_ID` LIKE 'admin-%'
   AND NOT EXISTS (
     SELECT 1
       FROM `FOOL_AUTHZ_BINDING` clone_binding
      WHERE clone_binding.`SUBJECT_TYPE` = 'USER'
        AND clone_binding.`SUBJECT_ID` = 'phase4-sysadmin'
        AND clone_binding.`BINDING_ID` LIKE 'phase4-sysadmin-%'
        AND clone_binding.`PERMISSION_ID` = source_binding.`PERMISSION_ID`
        AND clone_binding.`EFFECT` = source_binding.`EFFECT`
        AND clone_binding.`APP_ID` = source_binding.`APP_ID`
        AND clone_binding.`DATABASE_ID` = source_binding.`DATABASE_ID`
        AND clone_binding.`INCLUDE_CHILDREN` = source_binding.`INCLUDE_CHILDREN`
        AND clone_binding.`DATA_POLICY_ID` <=> source_binding.`DATA_POLICY_ID`
        AND clone_binding.`VALID_FROM` <=> source_binding.`VALID_FROM`
        AND clone_binding.`VALID_UNTIL` <=> source_binding.`VALID_UNTIL`
   );
""")
    lines = [line for line in output.splitlines() if line.strip()]
    credentials: list[dict[str, str]] = []
    binding_counts: list[int] = []
    for line in lines:
        parts = line.split("\t")
        if len(parts) == 2:
            credentials.append({"userId": parts[0], "algorithm": parts[1]})
        elif len(parts) == 1 and parts[0].isdigit():
            binding_counts.append(int(parts[0]))
    users = {item["userId"] for item in credentials if item["algorithm"] == "BCRYPT"}
    source_count, clone_count, missing_count = (
        binding_counts if len(binding_counts) == 3 else (-1, -1, -1)
    )
    return {
        "status": "PASS"
        if (
            users == set(FIXTURE_USER_IDS)
            and source_count > 0
            and clone_count == source_count
            and missing_count == 0
        )
        else "FAIL",
        "credentials": credentials,
        "systemAdminBindings": {
            "source": source_count,
            "clone": clone_count,
            "missing": missing_count,
        },
    }


def fixture_cleanup_verification(action_request_id: str | None) -> dict[str, object]:
    fixture_ids = ", ".join(sql_quote(item) for item in FIXTURE_USER_IDS)
    output = run_mysql(f"""
SELECT
  (SELECT COUNT(*) FROM `auth_user` WHERE `id` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `FOOL_AUTH_CREDENTIAL` WHERE `USER_ID` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `auth_role` WHERE `id` IN (9101, 9102)),
  (SELECT COUNT(*) FROM `auth_user_role` WHERE `user_id` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `SW_APP_AUTH_USER`
    WHERE `APP_AUTH_ID` IN (91001, 91002, 91003, 91004)
       OR `APP_AUTH_USERID` IN ({fixture_ids})
       OR `APP_AUTH_USERLOGINNAME` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER`
    WHERE `SW_APP_AUTH_USER_ID` IN (91001, 91002, 91003, 91004)),
  (SELECT COUNT(*) FROM `SW_APP_AUTH_DEPARTMENT`
    WHERE `APP_DEP_ID` IN (3001, 3002)),
  (SELECT COUNT(*) FROM `FOOL_AUTHZ_DATA_POLICY`
    WHERE `DATA_POLICY_ID` IN
      ('phase4-ordinary-read', 'phase4-department-read', 'phase4-approver-read')),
  (SELECT COUNT(*) FROM `FOOL_AUTHZ_BINDING` WHERE `BINDING_ID` LIKE 'phase4-%'),
  (SELECT COUNT(*) FROM `FOOL_AGENT_ACTION_REQUEST`
    WHERE `OWNER_USER_ID` IN ({fixture_ids})),
  (SELECT COUNT(*) FROM `FOOL_AGENT_APPROVAL`
    WHERE `APPROVER_USER_ID` IN ({fixture_ids}));
""")
    counts = [int(value) for value in output.split("\t")] if output else []
    names = (
        "users",
        "credentials",
        "roles",
        "roleAssignments",
        "legacyUsers",
        "legacyRoleAssignments",
        "departments",
        "dataPolicies",
        "bindings",
        "actionRequests",
        "approvals",
    )
    return {
        "status": "PASS"
        if len(counts) == len(names) and not any(counts)
        else "FAIL",
        "remaining": {
            name: counts[index] if len(counts) > index else -1
            for index, name in enumerate(names)
        },
    }


def build_driver_input(
    *,
    run_id: str,
    base_url: str,
    debug_port: int,
    profile_dir: Path,
    artifact_dir: Path,
    password: str,
) -> dict[str, object]:
    return {
        "schemaVersion": 1,
        "run": {
            "runId": run_id,
            "baseUrl": base_url.rstrip("/"),
            "debugPort": debug_port,
            "profileDir": str(profile_dir),
            "artifactDir": str(artifact_dir),
        },
        "credentials": {
            role: {"username": user_id, "password": password}
            for role, user_id in ROLE_USERS.items()
        },
    }


def terminate_process_group(process: subprocess.Popen[str]) -> None:
    if process.poll() is not None:
        return
    try:
        os.killpg(process.pid, signal.SIGTERM)
        process.wait(timeout=5)
    except (ProcessLookupError, subprocess.TimeoutExpired):
        try:
            os.killpg(process.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass
        process.wait(timeout=5)


def run_driver(
    driver_path: Path,
    payload: dict[str, object],
    timeout: float,
    forbidden_values: tuple[str, ...],
) -> tuple[dict[str, object], str]:
    process = subprocess.Popen(
        driver_command(driver_path),
        cwd=ROOT,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        start_new_session=True,
    )
    try:
        stdout, stderr = process.communicate(
            json.dumps(payload, ensure_ascii=False),
            timeout=timeout,
        )
    except subprocess.TimeoutExpired as exc:
        terminate_process_group(process)
        raise HarnessError("browser driver timed out") from exc
    if process.returncode not in {0, 1, 2}:
        raise HarnessError(
            f"browser driver exited with {process.returncode}: "
            + sanitize_text(stderr, forbidden_values)
        )
    try:
        result = json.loads(stdout)
    except json.JSONDecodeError as exc:
        raise HarnessError(
            "browser driver returned invalid JSON: "
            + sanitize_text(stderr or stdout, forbidden_values)
        ) from exc
    if not isinstance(result, dict):
        raise HarnessError("browser driver result must be an object")
    rendered = json.dumps(result, ensure_ascii=False)
    for secret in forbidden_values:
        if secret and secret in rendered:
            raise HarnessError("browser driver exposed an input secret")
    return result, sanitize_text(stderr, forbidden_values)


def action_request_id(result: dict[str, object]) -> str | None:
    high_risk = result.get("highRisk")
    if not isinstance(high_risk, dict):
        return None
    value = high_risk.get("actionRequestId")
    if isinstance(value, str) and SAFE_ID_RE.fullmatch(value):
        return value
    return None


def trace_ids(result: dict[str, object]) -> list[str]:
    values: set[str] = set()
    network = result.get("network")
    if isinstance(network, list):
        for item in network:
            if not isinstance(item, dict):
                continue
            value = item.get("traceId")
            if isinstance(value, str) and SAFE_ID_RE.fullmatch(value):
                values.add(value)
    request_id = action_request_id(result)
    if request_id:
        values.add(request_id)
    return sorted(values)


def audit_evidence(result: dict[str, object]) -> dict[str, object]:
    ids = trace_ids(result)
    if not ids:
        return {"status": "FAIL", "events": [], "reason": "no trace ids returned"}
    id_sql = ", ".join(sql_quote(item) for item in ids)
    output = run_mysql(f"""
SELECT
  `TRACE_ID`,
  COALESCE(`ACTION_REQUEST_ID`, ''),
  COALESCE(`ACTOR_USER_ID`, ''),
  `ACTION_ID`,
  `RESOURCE_KEY`,
  `DECISION`,
  `REASON_CODE`,
  COALESCE(`RISK_LEVEL`, ''),
  COALESCE(`CHAIN_SEQUENCE`, 0)
FROM `FOOL_SECURITY_AUDIT_EVENT`
WHERE `TRACE_ID` IN ({id_sql})
   OR `ACTION_REQUEST_ID` IN ({id_sql})
ORDER BY `CHAIN_SEQUENCE`;
""")
    events: list[dict[str, object]] = []
    for line in output.splitlines():
        parts = line.split("\t")
        if len(parts) != 9:
            continue
        events.append(
            {
                "traceId": parts[0],
                "actionRequestId": parts[1] or None,
                "actorUserId": parts[2] or None,
                "action": parts[3],
                "resourceKey": parts[4],
                "decision": parts[5],
                "reasonCode": parts[6],
                "riskLevel": parts[7] or None,
                "chainSequence": int(parts[8]),
            }
        )
    reasons = {str(item["reasonCode"]) for item in events}
    action_request = action_request_id(result)
    action_events = [
        item for item in events
        if action_request and item["actionRequestId"] == action_request
    ]
    action_reasons = {str(item["reasonCode"]) for item in action_events}
    passed = (
        "AWAITING_APPROVAL" in action_reasons
        and "APPROVAL_GRANTED" in action_reasons
        and "SELF_APPROVAL_FORBIDDEN" in action_reasons
        and "STEP_UP_REQUIRED" in action_reasons
        and "DATA_UPDATED" not in action_reasons
        and "ACTION_WORKFLOW_REQUIRED" in reasons
    )
    return {
        "status": "PASS" if passed else "FAIL",
        "traceIds": ids,
        "events": events,
        "expected": {
            "awaitingApproval": "AWAITING_APPROVAL" in action_reasons,
            "approvalGranted": "APPROVAL_GRANTED" in action_reasons,
            "selfApprovalDenied": "SELF_APPROVAL_FORBIDDEN" in action_reasons,
            "executeWithoutStepUpDenied": "STEP_UP_REQUIRED" in action_reasons,
            "directWriteDenied": "ACTION_WORKFLOW_REQUIRED" in reasons,
            "noExecutionSuccess": "DATA_UPDATED" not in action_reasons,
        },
    }


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def scan_artifacts(
    run_dir: Path,
    forbidden_values: tuple[str, ...] = (),
) -> list[dict[str, str]]:
    findings: list[dict[str, str]] = []
    for path in sorted(item for item in run_dir.rglob("*") if item.is_file()):
        relative = path.relative_to(run_dir).as_posix()
        data = path.read_bytes()
        for secret in forbidden_values:
            if secret and secret.encode("utf-8") in data:
                findings.append({"path": relative, "rule": "exact-secret"})
        if path.suffix.lower() == ".png":
            if not data.startswith(b"\x89PNG\r\n\x1a\n"):
                findings.append({"path": relative, "rule": "invalid-png"})
            if len(data) < 5_000:
                findings.append({"path": relative, "rule": "placeholder-or-empty-png"})
            continue
        if path.suffix.lower() not in TEXT_ARTIFACT_SUFFIXES:
            continue
        text = data.decode("utf-8", errors="replace")
        for rule, pattern in SECRET_PATTERNS:
            if pattern.search(text):
                findings.append({"path": relative, "rule": rule})
    deduplicated: list[dict[str, str]] = []
    seen: set[tuple[str, str]] = set()
    for finding in findings:
        key = (finding["path"], finding["rule"])
        if key not in seen:
            seen.add(key)
            deduplicated.append(finding)
    return deduplicated


def write_json(path: Path, payload: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(payload, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def build_manifest_payload(run_dir: Path) -> dict[str, object]:
    files: list[dict[str, object]] = []
    for path in sorted(item for item in run_dir.rglob("*") if item.is_file()):
        relative = path.relative_to(run_dir).as_posix()
        if relative == "manifest.json":
            continue
        media_type = mimetypes.guess_type(path.name)[0] or "application/octet-stream"
        files.append(
            {
                "path": relative,
                "mediaType": media_type,
                "size": path.stat().st_size,
                "sha256": sha256_file(path),
            }
        )
    return {"schemaVersion": 1, "files": files}


def role_statuses(result: dict[str, object]) -> dict[str, str]:
    roles = result.get("roles")
    if not isinstance(roles, dict):
        return {role: "FAIL" for role in ROLE_USERS}
    return {
        role: str(roles.get(role, {}).get("status", "FAIL"))
        if isinstance(roles.get(role), dict)
        else "FAIL"
        for role in ROLE_USERS
    }


def summary_payload(
    *,
    run_id: str,
    driver_result: dict[str, object],
    credential_check: dict[str, object],
    audit_check: dict[str, object],
    cleanup_check: dict[str, object],
    profile_removed: bool,
    artifact_findings: list[dict[str, str]],
    driver_error: str = "",
) -> dict[str, object]:
    roles = role_statuses(driver_result)
    high_risk = driver_result.get("highRisk")
    high_risk_pass = (
        isinstance(high_risk, dict)
        and high_risk.get("previewStatus") == "AWAITING_APPROVAL"
        and isinstance(high_risk.get("selfApproval"), dict)
        and high_risk["selfApproval"].get("status") == 403
        and high_risk["selfApproval"].get("reason") == "SELF_APPROVAL_FORBIDDEN"
        and isinstance(high_risk.get("approval"), dict)
        and high_risk["approval"].get("finalStatus") == "APPROVED"
        and isinstance(high_risk.get("executeWithoutStepUp"), dict)
        and high_risk["executeWithoutStepUp"].get("status") == 403
        and high_risk["executeWithoutStepUp"].get("reason") == "STEP_UP_REQUIRED"
        and high_risk["executeWithoutStepUp"].get("requestStatus") == "APPROVED"
        and high_risk.get("executed") is False
    )
    security = driver_result.get("security")
    dynamic_scan_pass = (
        isinstance(security, dict)
        and security.get("dynamicSecretScan") == "PASS"
    )
    checks = {
        "ui": "PASS" if roles and all(value == "PASS" for value in roles.values()) else "FAIL",
        "network": "PASS" if driver_result.get("status") == "PASS" else "FAIL",
        "highRiskWorkflow": "PASS" if high_risk_pass else "FAIL",
        "credentialMigration": str(credential_check.get("status", "FAIL")),
        "auditEvidence": str(audit_check.get("status", "FAIL")),
        "artifactSecurity": "PASS"
        if dynamic_scan_pass and not artifact_findings
        else "FAIL",
        "profileCleanup": "PASS" if profile_removed else "FAIL",
        "fixtureCleanup": str(cleanup_check.get("status", "FAIL")),
    }
    status = "PASS" if all(value == "PASS" for value in checks.values()) else "FAIL"
    payload: dict[str, object] = {
        "schemaVersion": 1,
        "runId": run_id,
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "status": status,
        "engine": driver_result.get("engine", "chrome-cdp"),
        "roles": roles,
        "checks": checks,
        "actionRequestId": action_request_id(driver_result),
        "finalActionStatus": (
            high_risk.get("executeWithoutStepUp", {}).get("requestStatus")
            if isinstance(high_risk, dict)
            and isinstance(high_risk.get("executeWithoutStepUp"), dict)
            else None
        ),
        "executed": high_risk.get("executed")
        if isinstance(high_risk, dict)
        else None,
        "artifactFindings": artifact_findings,
    }
    if driver_error:
        payload["driverDiagnostic"] = driver_error
    return payload


def safe_driver_artifact(result: dict[str, object]) -> dict[str, object]:
    payload = dict(result)
    network = payload.get("network")
    payload["network"] = (
        [sanitize_network_event(item) for item in network if isinstance(item, dict)]
        if isinstance(network, list)
        else []
    )
    console = payload.get("console")
    payload["console"] = (
        [
            {
                "role": sanitize_text(item.get("role")),
                "type": sanitize_text(item.get("type")),
                "text": sanitize_text(item.get("text")),
            }
            for item in console
            if isinstance(item, dict)
        ]
        if isinstance(console, list)
        else []
    )
    return payload


def ensure_prerequisites(driver_path: Path) -> None:
    if shutil.which("node") is None:
        raise HarnessError("node is unavailable")
    if shutil.which("docker") is None:
        raise HarnessError("docker is unavailable")
    if not driver_path.is_file():
        raise HarnessError("browser driver is missing")
    if not (ROOT / "frontend" / "node_modules" / "playwright-core").exists():
        raise HarnessError("frontend playwright-core is not installed")


def main(argv: list[str] | None = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv)
    artifact_root = args.artifact_root.resolve()
    run_dir = artifact_root / args.run_id
    if run_dir.exists() and any(run_dir.iterdir()):
        print(
            json.dumps(
                {
                    "status": "ENVIRONMENT_UNAVAILABLE",
                    "reason": "artifact directory already contains files",
                    "runId": args.run_id,
                },
                ensure_ascii=False,
            )
        )
        return 2
    run_dir.mkdir(parents=True, exist_ok=True)

    raw_password = secrets.token_urlsafe(24)
    forbidden_values = (raw_password,)
    driver_result: dict[str, object] = {
        "schemaVersion": 1,
        "status": "FAIL",
        "engine": "chrome-cdp",
        "roles": {},
    }
    driver_error = ""
    request_id: str | None = None
    credential_check: dict[str, object] = {"status": "FAIL", "credentials": []}
    audit_check: dict[str, object] = {"status": "FAIL", "events": []}
    cleanup_check: dict[str, object] = {"status": "FAIL", "remaining": {}}
    profile_removed = False
    profile_path: Path | None = None
    exit_code = 1

    try:
        ensure_prerequisites(args.driver.resolve())
        ensure_fixture_identifiers_available()
        run_mysql(fixture_setup_sql(raw_password))
        profile_path = Path(
            tempfile.mkdtemp(prefix=f"fool-browser-{args.run_id}-")
        )
        driver_input = build_driver_input(
            run_id=args.run_id,
            base_url=args.base_url,
            debug_port=args.chrome_debug_port,
            profile_dir=profile_path,
            artifact_dir=run_dir,
            password=raw_password,
        )
        driver_result, driver_error = run_driver(
            args.driver.resolve(),
            driver_input,
            args.timeout,
            forbidden_values,
        )
        request_id = action_request_id(driver_result)
        credential_check = fixture_verification()
        audit_check = audit_evidence(driver_result)
    except (HarnessError, OSError, subprocess.SubprocessError) as exc:
        driver_error = sanitize_text(exc, forbidden_values)
        if "unavailable" in driver_error.lower() or "occupied" in driver_error.lower():
            driver_result["status"] = "ENVIRONMENT_UNAVAILABLE"
            exit_code = 2
    finally:
        try:
            revoke_fixture_tokens()
        except (HarnessError, OSError, subprocess.SubprocessError) as exc:
            driver_error = (
                driver_error + "; " if driver_error else ""
            ) + sanitize_text(exc, forbidden_values)
        try:
            run_mysql(fixture_cleanup_sql(request_id))
            cleanup_check = fixture_cleanup_verification(request_id)
        except (HarnessError, OSError, subprocess.SubprocessError) as exc:
            driver_error = (
                driver_error + "; " if driver_error else ""
            ) + sanitize_text(exc, forbidden_values)
        if profile_path is None:
            profile_removed = True
        else:
            try:
                if profile_path.exists():
                    shutil.rmtree(profile_path)
                profile_removed = not profile_path.exists()
            except OSError as exc:
                driver_error = (
                    driver_error + "; " if driver_error else ""
                ) + sanitize_text(exc, forbidden_values)

    write_json(run_dir / "browser-role-matrix.json", safe_driver_artifact(driver_result))
    write_json(run_dir / "audit.json", audit_check)

    findings = scan_artifacts(run_dir, forbidden_values)
    summary = summary_payload(
        run_id=args.run_id,
        driver_result=driver_result,
        credential_check=credential_check,
        audit_check=audit_check,
        cleanup_check=cleanup_check,
        profile_removed=profile_removed,
        artifact_findings=findings,
        driver_error=driver_error,
    )
    write_json(run_dir / "summary.json", summary)
    write_json(run_dir / "manifest.json", build_manifest_payload(run_dir))

    final_findings = scan_artifacts(run_dir, forbidden_values)
    if final_findings != findings:
        summary = summary_payload(
            run_id=args.run_id,
            driver_result=driver_result,
            credential_check=credential_check,
            audit_check=audit_check,
            cleanup_check=cleanup_check,
            profile_removed=profile_removed,
            artifact_findings=final_findings,
            driver_error=driver_error,
        )
        write_json(run_dir / "summary.json", summary)
        write_json(run_dir / "manifest.json", build_manifest_payload(run_dir))

    if summary["status"] == "PASS":
        exit_code = 0
    elif driver_result.get("status") == "ENVIRONMENT_UNAVAILABLE":
        exit_code = 2
    else:
        exit_code = 1
    print(json.dumps(summary, indent=2, ensure_ascii=False))
    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
