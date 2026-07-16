#!/usr/bin/env python3
"""Reversible runtime checks for policy invalidation and audit integrity."""

from __future__ import annotations

import argparse
from concurrent.futures import ThreadPoolExecutor
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
import json
from pathlib import Path
import subprocess
import sys
from typing import Any
from urllib import error, request
import uuid


@dataclass(frozen=True)
class Check:
    name: str
    status: str
    detail: str


class Runtime:
    def __init__(self, base_url: str, timeout: float) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.token = ""

    def json_request(
            self,
            method: str,
            path: str,
            payload: Any | None = None,
            extra_headers: dict[str, str] | None = None) -> dict[str, Any]:
        body = None if payload is None else json.dumps(payload).encode("utf-8")
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        if extra_headers:
            headers.update(extra_headers)
        http_request = request.Request(
            f"{self.base_url}{path}", data=body, headers=headers, method=method)
        with request.urlopen(http_request, timeout=self.timeout) as response:
            return json.loads(response.read().decode("utf-8"))

    def mysql(self, sql: str) -> str:
        completed = subprocess.run(
            ["docker", "compose", "exec", "-T", "mysql", "mysql",
             "-uroot", "-pPa88word", "-N", "-B", "car_wash", "-e", sql],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
        return completed.stdout.strip()

    def login(self) -> None:
        check = self.json_request("POST", "/api/v1/auth/getchk", {})["data"]
        login = self.json_request("POST", "/api/v1/auth/loginv2", {
            "name": "admin",
            "pwd": "admin",
            "dbid": "car_wash",
            "chk": check["Code"],
            "chkid": check["Key"],
            "AppId": "fool-service",
            "AppKey": "fool-service",
        })
        data = login.get("data") if isinstance(login, dict) else None
        token = data.get("token") if isinstance(data, dict) else None
        if not token:
            raise RuntimeError("runtime admin login failed")
        self.token = str(token)


def sql_quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def reason(exc: error.HTTPError) -> str:
    return exc.read().decode("utf-8")


def policy_invalidation(runtime: Runtime) -> Check:
    action_id = ""
    original_updated_at = runtime.mysql(
        "SELECT DATE_FORMAT(UPDATED_AT,'%Y-%m-%d %H:%i:%s.%f') "
        "FROM FOOL_AUTHZ_BINDING WHERE BINDING_ID='admin-report-save'")
    if not original_updated_at:
        return Check("policy-invalidation", "FAIL", "admin-report-save binding is missing")
    try:
        intent = {
            "schemaVersion": 1,
            "action": "report.save",
            "resource": {"type": "view", "id": "100"},
            "arguments": {"request": {
                "ViewId": 100,
                "ReportName": "Policy invalidation regression",
                "CurrentPage": 1,
                "PageSize": 10,
                "ReportCols": [{"ColId": "orderId", "ColName": "Order ID", "Index": 0}],
            }},
            "rationale": "verify immediate policy invalidation",
        }
        created = runtime.json_request(
            "POST", "/api/v1/actions", intent,
            {"Idempotency-Key": f"policy-regression-{uuid.uuid4()}", "X-Action-Source": "API"})
        action_id = str(created["data"]["actionRequestId"])
        preview = runtime.json_request("POST", f"/api/v1/actions/{action_id}/preview", {})
        if preview["data"].get("status") != "AWAITING_CONFIRMATION":
            return Check("policy-invalidation", "FAIL", "report.save did not reach confirmation")
        runtime.mysql(
            "UPDATE FOOL_AUTHZ_BINDING SET UPDATED_AT=CURRENT_TIMESTAMP(6) "
            "WHERE BINDING_ID='admin-report-save'")
        try:
            confirmed = runtime.json_request("POST", f"/api/v1/actions/{action_id}/confirm", {})
            status = runtime.mysql(
                "SELECT STATUS FROM FOOL_AGENT_ACTION_REQUEST WHERE ACTION_REQUEST_ID="
                + sql_quote(action_id))
            rejected = "POLICY_CHANGED" in json.dumps(confirmed) and status == "PREVIEW_READY"
            return Check(
                "policy-invalidation", "PASS" if rejected else "FAIL",
                "next confirmation rejected with POLICY_CHANGED and reset to PREVIEW_READY"
                if rejected else "stale preview was not invalidated")
        except error.HTTPError as exc:
            body = reason(exc)
            ok = exc.code == 409 and "POLICY_CHANGED" in body
            return Check(
                "policy-invalidation", "PASS" if ok else "FAIL",
                "next confirmation rejected with POLICY_CHANGED" if ok else f"HTTP {exc.code}: {body[:240]}")
    finally:
        runtime.mysql(
            "UPDATE FOOL_AUTHZ_BINDING SET UPDATED_AT=" + sql_quote(original_updated_at) +
            " WHERE BINDING_ID='admin-report-save'")
        if action_id:
            runtime.mysql(
                "DELETE FROM FOOL_AGENT_ACTION_REQUEST WHERE ACTION_REQUEST_ID=" + sql_quote(action_id))


def audit_tamper_detection(runtime: Runtime) -> Check:
    row = runtime.mysql(
        "SELECT AUDIT_EVENT_ID,DECISION FROM FOOL_SECURITY_AUDIT_EVENT "
        "WHERE EVENT_HASH IS NOT NULL ORDER BY CHAIN_SEQUENCE LIMIT 1")
    event_id, separator, original_decision = row.partition("\t")
    if not separator or not event_id:
        return Check("audit-tamper-detection", "FAIL", "no chained audit event is available")
    try:
        runtime.mysql(
            "UPDATE FOOL_SECURITY_AUDIT_EVENT SET DECISION='TAMPERED' "
            "WHERE AUDIT_EVENT_ID=" + sql_quote(event_id))
        report = runtime.json_request("GET", "/api/v1/authz/audit-integrity")
        data = report.get("data") if isinstance(report, dict) else None
        alert_count = runtime.mysql(
            "SELECT COUNT(*) FROM FOOL_SECURITY_ALERT "
            "WHERE ALERT_TYPE='AUDIT_INTEGRITY' AND ACKNOWLEDGED_AT IS NULL")
        detected = (isinstance(data, dict) and data.get("valid") is False
                    and data.get("reasonCode") == "AUDIT_EVENT_HASH_MISMATCH"
                    and int(alert_count or "0") > 0)
    finally:
        runtime.mysql(
            "UPDATE FOOL_SECURITY_AUDIT_EVENT SET DECISION=" + sql_quote(original_decision) +
            " WHERE AUDIT_EVENT_ID=" + sql_quote(event_id))
    restored = runtime.json_request("GET", "/api/v1/authz/audit-integrity")
    restored_data = restored.get("data") if isinstance(restored, dict) else None
    ok = detected and isinstance(restored_data, dict) and restored_data.get("valid") is True
    return Check(
        "audit-tamper-detection", "PASS" if ok else "FAIL",
        "tamper raised a CRITICAL alert and exact restoration revalidated the chain"
        if ok else "tamper detection or restoration check failed")


def audit_concurrency(runtime: Runtime, concurrency: int) -> Check:
    def verify(_: int) -> bool:
        payload = runtime.json_request("GET", "/api/v1/authz/audit-integrity")
        return payload.get("data", {}).get("valid") is True

    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        results = list(executor.map(verify, range(concurrency)))
    counts = runtime.mysql(
        "SELECT COUNT(*),COUNT(DISTINCT CHAIN_SEQUENCE),"
        "MAX(CHAIN_SEQUENCE)-MIN(CHAIN_SEQUENCE)+1 "
        "FROM FOOL_SECURITY_AUDIT_EVENT WHERE EVENT_HASH IS NOT NULL")
    total, distinct, span = (int(value) for value in counts.split("\t"))
    final_report = runtime.json_request("GET", "/api/v1/authz/audit-integrity")
    valid = final_report.get("data", {}).get("valid") is True
    ok = all(results) and total == distinct == span and valid
    return Check(
        "audit-concurrency", "PASS" if ok else "FAIL",
        f"{concurrency} concurrent verifications; events={total}, distinctSequence={distinct}, span={span}")


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8081")
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--concurrency", type=int, default=16)
    parser.add_argument("--report-json", type=Path)
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv)
    runtime = Runtime(args.base_url, args.timeout)
    checks: list[Check] = []
    try:
        runtime.login()
        checks.append(policy_invalidation(runtime))
        checks.append(audit_tamper_detection(runtime))
        checks.append(audit_concurrency(runtime, max(2, args.concurrency)))
    except (KeyError, ValueError, RuntimeError, OSError, subprocess.CalledProcessError,
            error.URLError, json.JSONDecodeError) as exc:
        checks.append(Check("runtime", "FAIL", str(exc)))
    payload = {
        "status": "PASS" if checks and all(item.status == "PASS" for item in checks) else "FAIL",
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "checks": [asdict(item) for item in checks],
    }
    rendered = json.dumps(payload, indent=2, ensure_ascii=False) + "\n"
    if args.report_json:
        args.report_json.parent.mkdir(parents=True, exist_ok=True)
        args.report_json.write_text(rendered, encoding="utf-8")
    print(rendered, end="")
    return 0 if payload["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
