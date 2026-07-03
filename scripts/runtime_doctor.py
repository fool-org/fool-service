#!/usr/bin/env python3
"""Runtime smoke checks for the Docker-backed Vue/View workflow."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import json
import subprocess
import sys
from typing import Any
from urllib import error, request


REQUIRED_SERVICES = ("backend", "frontend", "mysql", "redis")


@dataclass(frozen=True)
class CheckResult:
    name: str
    ok: bool
    detail: str


def parse_compose_ps(raw: str) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    text = raw.strip()
    if not text:
        return rows
    if text.startswith("["):
        payload = json.loads(text)
        return payload if isinstance(payload, list) else []
    for line in text.splitlines():
        if line.strip():
            rows.append(json.loads(line))
    return rows


def compose_checks(rows: list[dict[str, Any]]) -> list[CheckResult]:
    by_service = {str(row.get("Service", "")): row for row in rows}
    results: list[CheckResult] = []
    for service in REQUIRED_SERVICES:
        row = by_service.get(service)
        if row is None:
            results.append(CheckResult(f"compose:{service}", False, "service missing"))
            continue
        state = str(row.get("State", "")).lower()
        health = str(row.get("Health", "")).lower()
        ok = state == "running" and health not in {"unhealthy", "starting"}
        detail = str(row.get("Status") or f"state={state} health={health}")
        results.append(CheckResult(f"compose:{service}", ok, detail))
    return results


def run_compose_ps() -> list[CheckResult]:
    try:
        completed = subprocess.run(
            ["docker", "compose", "ps", "--format", "json"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except (OSError, subprocess.CalledProcessError) as exc:
        return [CheckResult("compose", False, f"docker compose ps failed: {exc}")]
    return compose_checks(parse_compose_ps(completed.stdout))


def post_json(url: str, payload: dict[str, Any], timeout: float) -> dict[str, Any]:
    body = json.dumps(payload).encode("utf-8")
    req = request.Request(
        url,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with request.urlopen(req, timeout=timeout) as response:
        return json.loads(response.read().decode("utf-8"))


def get_json(url: str, timeout: float) -> Any:
    with request.urlopen(url, timeout=timeout) as response:
        return json.loads(response.read().decode("utf-8"))


def common_response_ok(payload: dict[str, Any]) -> bool:
    return payload.get("code") == 0 and isinstance(payload.get("data"), dict)


def common_response_list(payload: dict[str, Any], key: str) -> bool:
    if not common_response_ok(payload):
        return False
    data = payload["data"]
    return isinstance(data.get(key), list) and bool(data[key])


def detail_response_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    detail = payload["data"].get("data")
    return isinstance(detail, dict) and bool(detail.get("simpleData"))


def detail_view_id(payload: dict[str, Any]) -> int:
    if not common_response_ok(payload):
        return 0
    value = payload["data"].get("detailViewId")
    return value if isinstance(value, int) else 0


def api_checks(backend_url: str, frontend_url: str, timeout: float) -> list[CheckResult]:
    view_state: dict[str, int] = {}

    def get_list_view() -> bool:
        payload = post_json(
            f"{frontend_url}/api/v1/view/getlistview",
            {"ViewId": 100},
            timeout,
        )
        view_state["detailViewId"] = detail_view_id(payload)
        return view_state["detailViewId"] > 0

    def query_detail_from_loaded_view() -> bool:
        view_id = view_state.get("detailViewId")
        if not view_id:
            return False
        return detail_response_ok(post_json(
            f"{frontend_url}/api/v1/data/querydatadetail",
            {"ViewId": view_id, "ObjId": "1001"},
            timeout,
        ))

    checks = (
        (
            "backend:test",
            lambda: isinstance(get_json(f"{backend_url}/test", timeout), list),
            f"{backend_url}/test",
        ),
        (
            "view:getlistview",
            get_list_view,
            "POST /api/v1/view/getlistview ViewId=100 returns DetailViewId",
        ),
        (
            "data:querydata",
            lambda: common_response_ok(post_json(
                f"{frontend_url}/api/v1/data/querydata",
                {"ViewId": 100, "PageSize": 2, "PageIndex": 1},
                timeout,
            )),
            "POST /api/v1/data/querydata ViewId=100",
        ),
        (
            "data:querydatadetail",
            query_detail_from_loaded_view,
            "POST /api/v1/data/querydatadetail uses loaded DetailViewId",
        ),
        (
            "data:inputquery",
            lambda: common_response_list(post_json(
                f"{frontend_url}/api/v1/data/inputquery",
                {"ViewId": 100, "ViewItemId": "Customer", "Text": "Ada", "IsAdded": False},
                timeout,
            ), "items"),
            "POST /api/v1/data/inputquery Customer contains Ada",
        ),
        (
            "report:getmkqview",
            lambda: common_response_list(post_json(
                f"{frontend_url}/api/v1/report/getmkqview",
                {"ViewId": 100},
                timeout,
            ), "cols"),
            "POST /api/v1/report/getmkqview ViewId=100",
        ),
    )
    results: list[CheckResult] = []
    for name, check, detail in checks:
        try:
            results.append(CheckResult(name, bool(check()), detail))
        except (error.URLError, TimeoutError, json.JSONDecodeError, OSError) as exc:
            results.append(CheckResult(name, False, str(exc)))
    return results


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--backend-url", default="http://localhost:8080")
    parser.add_argument("--frontend-url", default="http://localhost:8081")
    parser.add_argument("--timeout", type=float, default=5.0)
    parser.add_argument("--skip-compose", action="store_true")
    args = parser.parse_args()

    results: list[CheckResult] = []
    if not args.skip_compose:
        results.extend(run_compose_ps())
    results.extend(api_checks(args.backend_url.rstrip("/"), args.frontend_url.rstrip("/"), args.timeout))

    for result in results:
        status = "PASS" if result.ok else "FAIL"
        print(f"[{status}] {result.name}: {result.detail}")
    return 0 if all(result.ok for result in results) else 1


if __name__ == "__main__":
    sys.exit(main())
