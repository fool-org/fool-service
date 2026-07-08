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
LIST_VIEW_ID = 100


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


def common_void_ok(payload: dict[str, Any]) -> bool:
    return payload.get("code") == 0


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
    if not isinstance(value, int):
        value = payload["data"].get("DetailViewId")
    return value if isinstance(value, int) else 0


def read_item_detail_views_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    data = payload["data"]
    detail_views = data.get("DetailViews") or data.get("detailViews")
    if not isinstance(detail_views, list) or not detail_views:
        return False
    first_detail = detail_views[0]
    if not isinstance(first_detail, dict):
        return False
    nested_items = first_detail.get("Items") or first_detail.get("items")
    if not isinstance(nested_items, list) or not nested_items:
        return False
    first_nested = nested_items[0]
    return isinstance(first_nested, dict) and bool(first_nested.get("PrpId") or first_nested.get("prpId"))


def report_grid_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    cells = payload["data"].get("cells")
    if not isinstance(cells, list):
        return False
    by_position: dict[tuple[int, int], str] = {}
    for cell in cells:
        if isinstance(cell, dict) and isinstance(cell.get("row"), int) and isinstance(cell.get("col"), int):
            by_position[(cell["row"], cell["col"])] = str(cell.get("fmtValue") or "")
    if by_position.get((0, 0)) != "Symbol" or by_position.get((0, 1)) != "State":
        return False
    max_row = max((row for row, _col in by_position), default=0)
    return any(by_position.get((row, 0)) and by_position.get((row, 1)) == "Open" for row in range(1, max_row + 1))


def list_row_item(row: dict[str, Any], key: str) -> dict[str, Any] | None:
    items = row.get("items") or row.get("Items")
    if not isinstance(items, list):
        return None
    return next((
        item for item in items
        if isinstance(item, dict) and (item.get("prpId") == key or item.get("PrpId") == key)
    ), None)


def list_rows(data: dict[str, Any]) -> list[Any]:
    rows = data.get("items")
    if not isinstance(rows, list):
        rows = data.get("Data")
    return rows if isinstance(rows, list) else []


def row_object_id(row: dict[str, Any]) -> str:
    direct = row.get("id") or row.get("Id")
    if direct:
        return str(direct)
    items = row.get("items") or row.get("Items")
    if not isinstance(items, list):
        return ""
    for item in items:
        if isinstance(item, dict):
            value = item.get("objId") or item.get("ObjId")
            if value:
                return str(value)
    return ""


def api_checks(backend_url: str, frontend_url: str, timeout: float) -> list[CheckResult]:
    view_state: dict[str, Any] = {}

    def get_list_view() -> bool:
        payload = post_json(
            f"{frontend_url}/api/v1/view/getlistview",
            {"ViewId": LIST_VIEW_ID},
            timeout,
        )
        loaded_detail_view_id = detail_view_id(payload)
        if loaded_detail_view_id:
            view_state["listViewId"] = LIST_VIEW_ID
            view_state["detailViewId"] = loaded_detail_view_id
        return loaded_detail_view_id > 0

    def loaded_list_view_id() -> int:
        view_id = view_state.get("listViewId")
        return view_id if isinstance(view_id, int) else 0

    def query_detail_from_loaded_view() -> bool:
        view_id = view_state.get("detailViewId")
        object_id = view_state.get("objectId")
        if not view_id or not object_id:
            return False
        return detail_response_ok(post_json(
            f"{frontend_url}/api/v1/data/querydatadetail",
            {"ViewId": view_id, "ObjId": object_id},
            timeout,
        ))

    def read_item_view_from_loaded_view() -> bool:
        view_id = view_state.get("detailViewId")
        if not view_id:
            return False
        return read_item_detail_views_ok(post_json(
            f"{frontend_url}/api/v1/view/getreaditemview",
            {"ViewId": view_id},
            timeout,
        ))

    def querydata_filter_ok() -> bool:
        view_id = loaded_list_view_id()
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/querydata",
            {"ViewId": view_id, "PageSize": 5, "PageIndex": 1, "QueryFilter": "order_state=\"0\""},
            timeout,
        )
        if not common_response_ok(payload):
            return False
        rows = list_rows(payload["data"])
        if not rows:
            return False
        for row in rows:
            if not isinstance(row, dict):
                return False
            state = list_row_item(row, "state") or {}
            if str(state.get("objId") or state.get("ObjId") or "") != "0":
                return False
        return True

    def querydata_ok() -> bool:
        view_id = loaded_list_view_id()
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/querydata",
            {"ViewId": view_id, "PageSize": 2, "PageIndex": 1},
            timeout,
        )
        if not common_response_ok(payload):
            return False
        rows = list_rows(payload["data"])
        if not rows or not isinstance(rows[0], dict):
            return False
        object_id = row_object_id(rows[0])
        if object_id:
            view_state["objectId"] = object_id
        return bool(object_id)

    checks = (
        (
            "backend:test",
            lambda: isinstance(get_json(f"{backend_url}/test", timeout), list),
            f"{backend_url}/test",
        ),
        (
            "view:getlistview",
            get_list_view,
            f"POST /api/v1/view/getlistview ViewId={LIST_VIEW_ID} returns DetailViewId",
        ),
        (
            "data:querydata",
            querydata_ok,
            "POST /api/v1/data/querydata uses loaded list view id and returns a row object id",
        ),
        (
            "data:querydata-filter",
            querydata_filter_ok,
            "POST /api/v1/data/querydata uses loaded list view id and keeps State=0 rows",
        ),
        (
            "data:querydatadetail",
            query_detail_from_loaded_view,
            "POST /api/v1/data/querydatadetail uses loaded DetailViewId and querydata row id",
        ),
        (
            "view:getreaditemview-detailviews",
            read_item_view_from_loaded_view,
            "POST /api/v1/view/getreaditemview returns DetailViews child metadata",
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
        (
            "report:getrpt",
            lambda: report_grid_ok(post_json(
                f"{frontend_url}/api/v1/report/getrpt",
                {
                    "ViewId": 100,
                    "CurrentPage": 1,
                    "PageSize": 10,
                    "QueryFilter": "order_state=\"0\"",
                    "ReportCols": [
                        {"ColName": "Symbol", "Index": 1},
                        {"ColName": "State", "Index": 2},
                    ],
                },
                timeout,
            )),
            "POST /api/v1/report/getrpt returns Symbol/State cells",
        ),
        (
            "report:saverpt",
            lambda: common_void_ok(post_json(
                f"{frontend_url}/api/v1/report/saverpt",
                {
                    "ViewId": 100,
                    "ReportName": "Order Daily",
                    "ReportCols": [{"ColName": "Symbol", "Index": 1}],
                    "FilterExp": {
                        "Col": {"Name": "order_state"},
                        "CompareOp": {"ID": "1", "Name": "等于"},
                        "ValueExp": "0",
                        "ValueFmt": "Open",
                    },
                },
                timeout,
            )),
            "POST /api/v1/report/saverpt keeps legacy no-op success surface",
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
