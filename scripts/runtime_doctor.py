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
MARKET_SYMBOLS_COLUMNS = (
    "base_currency",
    "quote_currency",
    "price_precision",
    "amount_precision",
    "symbol_partition",
    "symbol",
    "value_precision",
    "exchange_type",
    "price_tick_size",
    "lot_size_qty_step_size",
    "market_lot_size_qty_step_size",
    "max_iceberg_orders_num",
)


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


def market_symbols_schema_ok(raw: str) -> bool:
    columns = {line.strip() for line in raw.splitlines() if line.strip()}
    return set(MARKET_SYMBOLS_COLUMNS).issubset(columns)


def run_mysql_schema_checks() -> list[CheckResult]:
    query = (
        "SELECT COLUMN_NAME FROM information_schema.COLUMNS "
        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'market_symbols' "
        "ORDER BY ORDINAL_POSITION"
    )
    try:
        completed = subprocess.run(
            [
                "docker", "compose", "exec", "-T", "mysql",
                "mysql", "-uroot", "-pPa88word", "-N", "-B", "car_wash",
                "-e", query,
            ],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except (OSError, subprocess.CalledProcessError) as exc:
        return [CheckResult("mysql:market_symbols", False, f"schema check failed: {exc}")]
    return [CheckResult(
        "mysql:market_symbols",
        market_symbols_schema_ok(completed.stdout),
        "FH_JAVA legacy market_symbols schema is present",
    )]


def post_json(url: str, payload: Any, timeout: float) -> dict[str, Any]:
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


def common_true_ok(payload: dict[str, Any]) -> bool:
    return payload.get("code") == 0 and payload.get("data") is True


def common_response_list(payload: dict[str, Any], key: str) -> bool:
    if not common_response_ok(payload):
        return False
    data = payload["data"]
    return isinstance(data.get(key), list) and bool(data[key])


def legacy_response_list(payload: dict[str, Any], key: str) -> list[Any]:
    if not common_response_ok(payload):
        return []
    value = payload["data"].get(key)
    return value if isinstance(value, list) else []


def response_list_field_present(payload: dict[str, Any], key: str) -> bool:
    return common_response_ok(payload) and isinstance(payload["data"].get(key), list)


def runoperation_result_aliases_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    data = payload["data"]
    return all(key in data for key in ("Value", "IsSuccess", "ReturnObjId", "ReturnViewId", "ReturnMsg"))


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


def list_view_operation_labels_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    operations = payload["data"].get("Operations") or payload["data"].get("operations")
    if not isinstance(operations, list):
        return False
    labels = {
        str(operation.get("Name") or operation.get("name") or "")
        for operation in operations
        if isinstance(operation, dict)
    }
    return {"\u5220\u9664", "\u4fdd\u5b58"}.issubset(labels)


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


def report_grid_ok(payload: dict[str, Any], expected_headers: list[str] | None = None) -> bool:
    if not common_response_ok(payload):
        return False
    cells = payload["data"].get("cells") or payload["data"].get("Cells")
    if not isinstance(cells, list):
        return False
    by_position: dict[tuple[int, int], str] = {}
    for cell in cells:
        if not isinstance(cell, dict):
            continue
        row = cell.get("row") if cell.get("row") is not None else cell.get("Row")
        col = cell.get("col") if cell.get("col") is not None else cell.get("Col")
        if isinstance(row, int) and isinstance(col, int):
            by_position[(row, col)] = str(cell.get("fmtValue") or cell.get("FmtValue") or "")
    headers = [by_position.get((0, index), "") for index in range(len(expected_headers or []))]
    if expected_headers and headers != expected_headers:
        return False
    if not expected_headers and not by_position.get((0, 0)):
        return False
    max_row = max((row for row, _col in by_position), default=0)
    max_col = max((col for _row, col in by_position), default=0)
    return any(by_position.get((row, col)) for row in range(1, max_row + 1) for col in range(0, max_col + 1))


def view_columns(payload: dict[str, Any]) -> list[dict[str, Any]]:
    if not common_response_ok(payload):
        return []
    data = payload["data"]
    columns = data.get("Items") or data.get("tableColumn")
    return [column for column in columns if isinstance(column, dict)] if isinstance(columns, list) else []


def view_column_key(column: dict[str, Any]) -> str:
    keys = (
        "PropertyName",
        "propertyName",
        "property",
        "Name",
        "name",
        "ID",
        "id",
    )
    return next((str(column.get(key)) for key in keys if column.get(key) not in (None, "")), "")


def query_rows_match_view(rows: list[Any], columns: list[dict[str, Any]]) -> bool:
    keys = [view_column_key(column) for column in columns if view_column_key(column)]
    if not rows or not keys:
        return False
    for row in rows:
        if not isinstance(row, dict):
            return False
        if not all(list_row_item(row, key) is not None for key in keys):
            return False
    return True


def lookup_view_item_id(columns: list[dict[str, Any]]) -> str:
    for column in columns:
        property_type = str(column.get("PropertyType") or column.get("propertyType") or "").lower()
        property_model = column.get("PropertyModel") or column.get("propertyModel") or 0
        try:
            model_id = int(property_model or 0)
        except (TypeError, ValueError):
            model_id = 0
        if property_type in {"businessobject", "16"} and model_id > 0:
            return view_column_key(column)
    return ""


def report_model_columns(payload: dict[str, Any]) -> list[dict[str, Any]]:
    if not common_response_ok(payload):
        return []
    columns = payload["data"].get("cols") or payload["data"].get("Cols")
    return [column for column in columns if isinstance(column, dict)] if isinstance(columns, list) else []


def runtime_report_cols(columns: list[dict[str, Any]]) -> list[dict[str, Any]]:
    result: list[dict[str, Any]] = []
    for index, column in enumerate(columns[:2], start=1):
        name = str(column.get("name") or column.get("Name") or column.get("id") or column.get("ID") or "")
        column_id = str(column.get("id") or column.get("ID") or name)
        query_types = column.get("queryTypes") or column.get("QueryTypes") or []
        selected = query_types[0] if query_types and isinstance(query_types[0], dict) else {}
        result.append({
            "ColName": name,
            "ColId": column_id,
            "SelectedTypeId": selected.get("id") or selected.get("ID"),
            "Index": index,
            "OrderType": "2",
        })
    return [column for column in result if column["ColName"]]


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


def legacy_login_token(payload: dict[str, Any]) -> str:
    if not common_response_ok(payload):
        return ""
    data = payload["data"]
    success = data.get("loginSucess")
    if success is None:
        success = data.get("LoginSucess")
    token = data.get("token") or data.get("Token")
    return str(token) if success is True and token else ""


def legacy_app_default_view_id(payload: dict[str, Any]) -> int:
    if not common_response_ok(payload):
        return 0
    data = payload["data"]
    app = data.get("app") or data.get("App")
    if not isinstance(app, dict):
        return 0
    value = app.get("defaultViewId")
    if not isinstance(value, int):
        value = app.get("DefaultViewId")
    return value if isinstance(value, int) else 0


def legacy_app_alias_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    app = payload["data"].get("App")
    return isinstance(app, dict) and isinstance(app.get("DefaultViewId"), int) and app["DefaultViewId"] > 0


def api_checks(backend_url: str, frontend_url: str, timeout: float) -> list[CheckResult]:
    view_state: dict[str, Any] = {}
    auth_state: dict[str, str] = {}

    def init_app_ok() -> bool:
        payload = post_json(
            f"{frontend_url}/api/v1/auth/initapp",
            {"AppId": "fool-service", "AppKey": "fool-service"},
            timeout,
        )
        if not common_response_ok(payload):
            return False
        data = payload["data"]
        check_code = data.get("CheckCode")
        return (
            isinstance(data.get("Dbs"), list)
            and bool(data["Dbs"])
            and isinstance(check_code, dict)
            and bool(check_code.get("Key"))
            and bool(check_code.get("Code"))
        )

    def check_code_ok() -> bool:
        payload = post_json(f"{frontend_url}/api/v1/auth/getcheckcode", {}, timeout)
        if not common_response_ok(payload):
            return False
        key = str(payload["data"].get("Key") or "")
        code = str(payload["data"].get("Code") or "")
        if not key or not code:
            return False
        auth_state["checkCodeKey"] = key
        auth_state["checkCode"] = code
        return common_true_ok(post_json(
            f"{frontend_url}/api/v1/auth/checkcode",
            {"Key": key, "Code": code},
            timeout,
        ))

    def login_v2_ok() -> bool:
        key = auth_state.get("checkCodeKey")
        code = auth_state.get("checkCode")
        if not key or not code:
            return False
        token = legacy_login_token(post_json(
            f"{frontend_url}/api/v1/auth/loginv2",
            {
                "UserId": "admin",
                "PassWord": "admin",
                "DbId": "car_wash",
                "CheckCode": code,
                "AppId": "fool-service",
                "AppKey": "fool-service",
                "CheckCodeKey": key,
            },
            timeout,
        ))
        if token:
            auth_state["token"] = token
        return bool(token)

    def get_userinfo_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        payload = post_json(f"{frontend_url}/api/v1/auth/getuserinfo", {"Token": token}, timeout)
        return common_response_ok(payload) and payload["data"].get("user") is not None

    def get_app_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        payload = post_json(f"{frontend_url}/api/v1/auth/getapp", {"Token": token}, timeout)
        default_view_id = legacy_app_default_view_id(payload)
        if default_view_id:
            view_state["listViewId"] = default_view_id
        return legacy_app_alias_ok(payload)

    def get_main_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        payload = post_json(f"{frontend_url}/api/v1/auth/getmain", token, timeout)
        top_menu = legacy_response_list(payload, "TopMenu")
        if not top_menu:
            return False
        default_view_id = legacy_app_default_view_id(payload)
        if default_view_id:
            view_state["listViewId"] = default_view_id
        first = top_menu[0]
        if isinstance(first, dict):
            auth_no = first.get("authNo") or first.get("AuthNo")
            if auth_no:
                auth_state["parentAuthCode"] = str(auth_no)
        return legacy_app_alias_ok(payload)

    def get_submenu_ok() -> bool:
        token = auth_state.get("token")
        parent = auth_state.get("parentAuthCode")
        if not token or not parent:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/auth/getsubmenu",
            {"Token": token, "ParentAuthCode": parent},
            timeout,
        )
        return bool(legacy_response_list(payload, "Items"))

    def logout_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        return common_void_ok(post_json(f"{frontend_url}/api/v1/auth/logout", {"Token": token}, timeout))

    def get_messages_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        return response_list_field_present(
            post_json(f"{frontend_url}/api/v1/message/getmsg", {"Token": token}, timeout),
            "Messages",
        )

    def get_notify_ok() -> bool:
        token = auth_state.get("token")
        if not token:
            return False
        return response_list_field_present(
            post_json(f"{frontend_url}/api/v1/message/getnotify", {"Token": token}, timeout),
            "Notifies",
        )

    def get_list_view() -> bool:
        view_id = loaded_list_view_id()
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/view/getlistview",
            {"ViewId": view_id},
            timeout,
        )
        loaded_detail_view_id = detail_view_id(payload)
        columns = view_columns(payload)
        if loaded_detail_view_id:
            view_state["listViewId"] = view_id
            view_state["detailViewId"] = loaded_detail_view_id
        if columns:
            view_state["columns"] = columns
        return loaded_detail_view_id > 0 and bool(columns) and list_view_operation_labels_ok(payload)

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

    def querydata_view_items_ok() -> bool:
        view_id = loaded_list_view_id()
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/querydata",
            {"ViewId": view_id, "PageSize": 5, "PageIndex": 1},
            timeout,
        )
        if not common_response_ok(payload):
            return False
        rows = list_rows(payload["data"])
        columns = view_state.get("columns")
        return isinstance(columns, list) and query_rows_match_view(rows, columns)

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
        columns = view_state.get("columns")
        return bool(object_id) and isinstance(columns, list) and query_rows_match_view(rows, columns)

    def runoperation_aliases_ok() -> bool:
        view_id = loaded_list_view_id()
        object_id = view_state.get("objectId")
        if not view_id or not object_id:
            return False
        return runoperation_result_aliases_ok(post_json(
            f"{frontend_url}/api/v1/data/runoperation",
            {"ViewId": view_id, "ObjectId": object_id, "OperationId": 0},
            timeout,
        ))

    def inputquery_ok() -> bool:
        view_id = loaded_list_view_id()
        columns = view_state.get("columns")
        if not view_id or not isinstance(columns, list):
            return False
        item_id = lookup_view_item_id(columns)
        if not item_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/inputquery",
            {"ViewId": view_id, "ViewItemId": item_id, "Text": "", "IsAdded": False},
            timeout,
        )
        return common_response_list(payload, "items") and common_response_list(payload, "Items")

    def get_report_model_ok() -> bool:
        view_id = loaded_list_view_id()
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/report/getmkqview",
            {"ViewId": view_id},
            timeout,
        )
        columns = report_model_columns(payload)
        if columns:
            view_state["reportColumns"] = columns
        return bool(columns) and response_list_field_present(payload, "Cols")

    def get_report_ok() -> bool:
        view_id = loaded_list_view_id()
        columns = view_state.get("reportColumns")
        if not view_id or not isinstance(columns, list):
            return False
        report_cols = runtime_report_cols(columns)
        if not report_cols:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/report/getrpt",
            {
                "ViewId": view_id,
                "CurrentPage": 1,
                "PageSize": 10,
                "ReportCols": report_cols,
            },
            timeout,
        )
        return report_grid_ok(payload, [str(column["ColName"]) for column in report_cols]) and response_list_field_present(payload, "Cells")

    def save_report_ok() -> bool:
        view_id = loaded_list_view_id()
        columns = view_state.get("reportColumns")
        if not view_id or not isinstance(columns, list):
            return False
        report_cols = runtime_report_cols(columns[:1])
        if not report_cols:
            return False
        return common_void_ok(post_json(
            f"{frontend_url}/api/v1/report/saverpt",
            {
                "ViewId": view_id,
                "ReportName": f"View {view_id} Runtime",
                "ReportCols": report_cols,
            },
            timeout,
        ))

    checks = (
        (
            "backend:test",
            lambda: isinstance(get_json(f"{backend_url}/test", timeout), list),
            f"{backend_url}/test",
        ),
        (
            "auth:initapp",
            init_app_ok,
            "POST /api/v1/auth/initapp returns seeded app database list",
        ),
        (
            "auth:checkcode",
            check_code_ok,
            "POST /api/v1/auth/getcheckcode then /checkcode validates the legacy code",
        ),
        (
            "auth:loginv2",
            login_v2_ok,
            "POST /api/v1/auth/loginv2 returns a legacy token for Docker admin",
        ),
        (
            "auth:getuserinfo",
            get_userinfo_ok,
            "POST /api/v1/auth/getuserinfo accepts the loginv2 token",
        ),
        (
            "auth:getapp",
            get_app_ok,
            "POST /api/v1/auth/getapp accepts the loginv2 token",
        ),
        (
            "auth:getmain",
            get_main_ok,
            "POST /api/v1/auth/getmain returns the legacy top menu",
        ),
        (
            "auth:getsubmenu",
            get_submenu_ok,
            "POST /api/v1/auth/getsubmenu follows the top-menu auth code",
        ),
        (
            "view:getlistview",
            get_list_view,
            "POST /api/v1/view/getlistview uses the App default ViewId and returns DetailViewId",
        ),
        (
            "data:querydata",
            querydata_ok,
            "POST /api/v1/data/querydata uses loaded list view id and returns a row object id",
        ),
        (
            "data:querydata-items",
            querydata_view_items_ok,
            "POST /api/v1/data/querydata rows expose Items matching the loaded View columns",
        ),
        (
            "data:runoperation-aliases",
            runoperation_aliases_ok,
            "POST /api/v1/data/runoperation returns legacy result aliases on a no-op operation",
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
            inputquery_ok,
            "POST /api/v1/data/inputquery uses a BusinessObject field from the loaded View",
        ),
        (
            "report:getmkqview",
            get_report_model_ok,
            "POST /api/v1/report/getmkqview uses the loaded ViewId",
        ),
        (
            "report:getrpt",
            get_report_ok,
            "POST /api/v1/report/getrpt uses getmkqview columns from the loaded View",
        ),
        (
            "report:saverpt",
            save_report_ok,
            "POST /api/v1/report/saverpt keeps legacy no-op success surface",
        ),
        (
            "message:getmsg",
            get_messages_ok,
            "POST /api/v1/message/getmsg returns legacy Messages list",
        ),
        (
            "message:getnotify",
            get_notify_ok,
            "POST /api/v1/message/getnotify returns legacy Notifies list",
        ),
        (
            "auth:logout",
            logout_ok,
            "POST /api/v1/auth/logout invalidates the runtime login token",
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
    results.extend(run_mysql_schema_checks())
    results.extend(api_checks(args.backend_url.rstrip("/"), args.frontend_url.rstrip("/"), args.timeout))

    for result in results:
        status = "PASS" if result.ok else "FAIL"
        print(f"[{status}] {result.name}: {result.detail}")
    return 0 if all(result.ok for result in results) else 1


if __name__ == "__main__":
    sys.exit(main())
