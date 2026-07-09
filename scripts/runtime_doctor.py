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

from runtime_schema import LEGACY_CORE_SCHEMA_COLUMNS, MARKET_SYMBOLS_COLUMNS


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


def market_symbols_schema_ok(raw: str) -> bool:
    columns = {line.strip() for line in raw.splitlines() if line.strip()}
    return set(MARKET_SYMBOLS_COLUMNS).issubset(columns)


def legacy_core_schema_ok(raw: str) -> bool:
    present = set()
    for line in raw.splitlines():
        parts = line.rstrip("\n").split("\t", 1)
        if len(parts) == 2:
            present.add((parts[0], parts[1]))
    return set(LEGACY_CORE_SCHEMA_COLUMNS).issubset(present)


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
    results = [CheckResult(
        "mysql:market_symbols",
        market_symbols_schema_ok(completed.stdout),
        "FH_JAVA legacy market_symbols schema is present",
    )]
    legacy_tables = ",".join(f"'{table}'" for table in dict.fromkeys(
        table for table, _column in LEGACY_CORE_SCHEMA_COLUMNS))
    legacy_query = (
        "SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS "
        f"WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ({legacy_tables}) "
        "ORDER BY TABLE_NAME, ORDINAL_POSITION"
    )
    try:
        completed = subprocess.run(
            [
                "docker", "compose", "exec", "-T", "mysql",
                "mysql", "-uroot", "-pPa88word", "-N", "-B", "car_wash",
                "-e", legacy_query,
            ],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except (OSError, subprocess.CalledProcessError) as exc:
        results.append(CheckResult("mysql:legacy-core-schema", False, f"schema check failed: {exc}"))
        return results
    results.append(CheckResult(
        "mysql:legacy-core-schema",
        legacy_core_schema_ok(completed.stdout),
        "View-first legacy auth/app/model/view/operation/event/message/query/enum/connection schema is present",
    ))
    return results


def cleanup_runtime_smoke_order(object_id: str) -> bool:
    safe_id = object_id.replace("'", "''")
    sql = (
        f"DELETE FROM market_order_item WHERE order_id='{safe_id}'; "
        f"DELETE FROM market_order WHERE order_id='{safe_id}';"
    )
    try:
        subprocess.run(
            [
                "docker", "compose", "exec", "-T", "mysql",
                "mysql", "-uroot", "-pPa88word", "car_wash",
                "-e", sql,
            ],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except (OSError, subprocess.CalledProcessError):
        return False
    return True


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
    return bool(detail_simple_fields(payload))


def detail_simple_fields(payload: dict[str, Any]) -> list[dict[str, Any]]:
    if not common_response_ok(payload):
        return []
    detail = payload["data"].get("data") or payload["data"].get("Data")
    if not isinstance(detail, dict):
        return []
    simple_data = detail.get("simpleData") or detail.get("SimpleData")
    return [field for field in simple_data if isinstance(field, dict)] if isinstance(simple_data, list) else []


def detail_field_key(field: dict[str, Any]) -> str:
    return str(field.get("prpId") or field.get("PrpId") or "")


def detail_field_type(field: dict[str, Any]) -> str:
    return str(field.get("prpType") or field.get("PrpType") or "").lower()


def detail_field_readonly(field: dict[str, Any]) -> bool:
    return field.get("readOnly") is True or field.get("ReadOnly") is True


def runtime_save_properties(fields: list[dict[str, Any]], object_id: str) -> list[dict[str, str]]:
    properties: list[dict[str, str]] = []
    for field in fields:
        key = detail_field_key(field)
        field_type = detail_field_type(field)
        if not key or detail_field_readonly(field) or field_type in {"businessobject", "16"}:
            continue
        if field_type in {"enum", "15"}:
            value = "0"
        elif field_type in {"boolean", "bool", "2"}:
            value = "false"
        elif field_type in {"datetime", "date", "13"}:
            value = "2026-07-09"
        elif field_type in {"decimal", "double", "float", "int", "integer", "long", "ulong", "identifyid"}:
            value = "0"
        else:
            value = f"RUNTIME-{object_id}"
        properties.append({"Key": key, "Value": value})
    return properties


def detail_field_fmt_value(payload: dict[str, Any], key: str) -> str:
    for field in detail_simple_fields(payload):
        if detail_field_key(field) != key:
            continue
        value = field.get("fmtValue")
        if value is None:
            value = field.get("FmtValue")
        return "" if value is None else str(value)
    return ""


def detail_item_groups(payload: dict[str, Any]) -> list[dict[str, Any]]:
    if not common_response_ok(payload):
        return []
    detail = payload["data"].get("data") or payload["data"].get("Data")
    if not isinstance(detail, dict):
        return []
    groups = detail.get("items") or detail.get("Items")
    return [group for group in groups if isinstance(group, dict)] if isinstance(groups, list) else []


def detail_group_key(group: dict[str, Any]) -> str:
    return str(group.get("prpId") or group.get("PrpId") or group.get("key") or group.get("Key") or "")


def detail_group_columns(group: dict[str, Any]) -> list[dict[str, Any]]:
    columns = group.get("properties") or group.get("Properties")
    return [column for column in columns if isinstance(column, dict)] if isinstance(columns, list) else []


def runtime_child_properties(
        columns: list[dict[str, Any]],
        item_id: str,
        child_value: str = "Runtime child",
) -> list[dict[str, str]]:
    properties: list[dict[str, str]] = []
    for column in columns:
        key = detail_field_key(column)
        if not key:
            continue
        field_type = detail_field_type(column)
        value = child_value
        if "id" in key.lower() or field_type in {"long", "ulong", "identifyid", "int", "integer"}:
            value = item_id
        properties.append({"Key": key, "Value": value})
    return properties


def detail_child_field_fmt_value(payload: dict[str, Any], group_key: str, item_id: str, field_key: str) -> str:
    for group in detail_item_groups(payload):
        if detail_group_key(group) != group_key:
            continue
        rows = group.get("items") or group.get("Items")
        if not isinstance(rows, list):
            continue
        for row in rows:
            if not isinstance(row, dict):
                continue
            data_id = row.get("dataId") or row.get("DataId") or row.get("itemId") or row.get("ItemId")
            if str(data_id or "") != item_id:
                continue
            values = row.get("values") or row.get("Values")
            if not isinstance(values, list):
                continue
            for value in values:
                if isinstance(value, dict) and detail_field_key(value) == field_key:
                    fmt = value.get("fmtValue")
                    if fmt is None:
                        fmt = value.get("FmtValue")
                    return "" if fmt is None else str(fmt)
    return ""


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


def view_template_metadata_ok(payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    temp_file = str(payload["data"].get("TempFile") or "").strip()
    return bool(temp_file) and any(str(column.get("ViewFile") or "").strip() for column in view_columns(payload))


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


def view_column_property_type(column: dict[str, Any]) -> str:
    return str(column.get("PropertyType") or column.get("propertyType") or "").lower()


def view_column_property_model(column: dict[str, Any]) -> int:
    try:
        return int(column.get("PropertyModel") or column.get("propertyModel") or 0)
    except (TypeError, ValueError):
        return 0


def lookup_view_item_id(columns: list[dict[str, Any]]) -> str:
    for column in columns:
        property_type = view_column_property_type(column)
        model_id = view_column_property_model(column)
        if property_type in {"businessobject", "16"} and model_id > 0:
            return view_column_key(column)
    return ""


def enum_view_model_id(columns: list[dict[str, Any]]) -> int:
    for column in columns:
        if view_column_property_type(column) in {"enum", "15"}:
            model_id = view_column_property_model(column)
            if model_id > 0:
                return model_id
    return 0


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
        return (
            loaded_detail_view_id > 0
            and bool(columns)
            and list_view_operation_labels_ok(payload)
            and view_template_metadata_ok(payload)
        )

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

    def query_detail_idexp_from_loaded_view() -> bool:
        view_id = view_state.get("detailViewId")
        object_id = view_state.get("objectId")
        if not view_id or not object_id:
            return False
        return detail_response_ok(post_json(
            f"{frontend_url}/api/v1/data/querydatadetail",
            {"ViewId": view_id, "ObjId": "", "IdExp": f"${object_id}"},
            timeout,
        ))

    def init_new_from_loaded_detail_view() -> bool:
        view_id = view_state.get("detailViewId")
        if not view_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/initnew",
            {"ViewId": view_id},
            timeout,
        )
        fields = detail_simple_fields(payload)
        if fields:
            view_state["newFields"] = fields
        groups = detail_item_groups(payload)
        if groups:
            view_state["newGroups"] = groups
        return bool(fields)

    def save_new_object_from_loaded_detail_view() -> bool:
        view_id = view_state.get("detailViewId")
        fields = view_state.get("newFields")
        if not view_id or not isinstance(fields, list):
            return False
        object_id = "989902"
        properties = runtime_save_properties(fields, object_id)
        if not properties or not cleanup_runtime_smoke_order(object_id):
            return False
        ok = False
        try:
            ok = common_void_ok(post_json(
                f"{frontend_url}/api/v1/data/savenewobj",
                {
                    "SaveObj": {
                        "Id": object_id,
                        "ViewID": str(view_id),
                        "Propertyies": properties,
                        "Itemproperties": [],
                    },
                },
                timeout,
            ))
            if ok:
                ok = detail_response_ok(post_json(
                    f"{frontend_url}/api/v1/data/querydatadetail",
                    {"ViewId": view_id, "ObjId": object_id},
                    timeout,
                ))
        finally:
            cleaned = cleanup_runtime_smoke_order(object_id)
        return ok and cleaned

    def save_existing_object_from_loaded_detail_view() -> bool:
        view_id = view_state.get("detailViewId")
        fields = view_state.get("newFields")
        if not view_id or not isinstance(fields, list):
            return False
        object_id = "989903"
        create_properties = runtime_save_properties(fields, object_id)
        update_properties = runtime_save_properties(fields, f"{object_id}-UPDATE")
        expected = next((item for item in update_properties if item["Value"].startswith("RUNTIME-")), None)
        if not create_properties or not update_properties or expected is None or not cleanup_runtime_smoke_order(object_id):
            return False
        ok = False
        try:
            ok = common_void_ok(post_json(
                f"{frontend_url}/api/v1/data/savenewobj",
                {
                    "SaveObj": {
                        "Id": object_id,
                        "ViewID": str(view_id),
                        "Propertyies": create_properties,
                        "Itemproperties": [],
                    },
                },
                timeout,
            ))
            if ok:
                ok = common_void_ok(post_json(
                    f"{frontend_url}/api/v1/data/saveobj",
                    {
                        "SaveObj": {
                            "Id": object_id,
                            "ViewID": str(view_id),
                            "Propertyies": update_properties,
                            "Itemproperties": [],
                        },
                    },
                    timeout,
                ))
            if ok:
                detail = post_json(
                    f"{frontend_url}/api/v1/data/querydatadetail",
                    {"ViewId": view_id, "ObjId": object_id},
                    timeout,
                )
                ok = detail_field_fmt_value(detail, expected["Key"]) == expected["Value"]
        finally:
            cleaned = cleanup_runtime_smoke_order(object_id)
        return ok and cleaned

    def save_added_item_from_loaded_detail_view() -> bool:
        view_id = view_state.get("detailViewId")
        fields = view_state.get("newFields")
        groups = view_state.get("newGroups")
        if not view_id or not isinstance(fields, list) or not isinstance(groups, list) or not groups:
            return False
        object_id = "989904"
        group = groups[0]
        group_key = detail_group_key(group)
        child_properties = runtime_child_properties(detail_group_columns(group), object_id)
        expected = next((item for item in child_properties if item["Value"] == "Runtime child"), None)
        create_properties = runtime_save_properties(fields, object_id)
        if not group_key or not create_properties or not child_properties or expected is None:
            return False
        if not cleanup_runtime_smoke_order(object_id):
            return False
        ok = False
        try:
            ok = common_void_ok(post_json(
                f"{frontend_url}/api/v1/data/savenewobj",
                {
                    "SaveObj": {
                        "Id": object_id,
                        "ViewID": str(view_id),
                        "Propertyies": create_properties,
                        "Itemproperties": [],
                    },
                },
                timeout,
            ))
            if ok:
                ok = common_void_ok(post_json(
                    f"{frontend_url}/api/v1/data/saveobj",
                    {
                        "SaveObj": {
                            "Id": object_id,
                            "ViewID": str(view_id),
                            "Propertyies": create_properties,
                            "Itemproperties": [{
                                "Key": group_key,
                                "AddedItems": [{
                                    "ItemId": object_id,
                                    "IsExist": False,
                                    "Propertyies": child_properties,
                                }],
                            }],
                        },
                    },
                    timeout,
                ))
            if ok:
                detail = post_json(
                    f"{frontend_url}/api/v1/data/querydatadetail",
                    {"ViewId": view_id, "ObjId": object_id},
                    timeout,
                )
                ok = detail_child_field_fmt_value(detail, group_key, object_id, expected["Key"]) == expected["Value"]
        finally:
            cleaned = cleanup_runtime_smoke_order(object_id)
        return ok and cleaned

    def save_updated_deleted_item_from_loaded_detail_view() -> bool:
        view_id = view_state.get("detailViewId")
        fields = view_state.get("newFields")
        groups = view_state.get("newGroups")
        if not view_id or not isinstance(fields, list) or not isinstance(groups, list) or not groups:
            return False
        object_id = "989905"
        group = groups[0]
        group_key = detail_group_key(group)
        columns = detail_group_columns(group)
        create_properties = runtime_save_properties(fields, object_id)
        added_properties = runtime_child_properties(columns, object_id)
        updated_properties = runtime_child_properties(columns, object_id, "Runtime child updated")
        expected = next((item for item in updated_properties if item["Value"] == "Runtime child updated"), None)
        if not group_key or not create_properties or not added_properties or expected is None:
            return False
        if not cleanup_runtime_smoke_order(object_id):
            return False
        ok = False
        try:
            ok = common_void_ok(post_json(
                f"{frontend_url}/api/v1/data/savenewobj",
                {
                    "SaveObj": {
                        "Id": object_id,
                        "ViewID": str(view_id),
                        "Propertyies": create_properties,
                        "Itemproperties": [],
                    },
                },
                timeout,
            ))
            if ok:
                ok = common_void_ok(post_json(
                    f"{frontend_url}/api/v1/data/saveobj",
                    {
                        "SaveObj": {
                            "Id": object_id,
                            "ViewID": str(view_id),
                            "Propertyies": create_properties,
                            "Itemproperties": [{
                                "Key": group_key,
                                "AddedItems": [{
                                    "ItemId": object_id,
                                    "IsExist": False,
                                    "Propertyies": added_properties,
                                }],
                            }],
                        },
                    },
                    timeout,
                ))
            if ok:
                ok = common_void_ok(post_json(
                    f"{frontend_url}/api/v1/data/saveobj",
                    {
                        "SaveObj": {
                            "Id": object_id,
                            "ViewID": str(view_id),
                            "Propertyies": create_properties,
                            "Itemproperties": [{
                                "Key": group_key,
                                "Items": [{
                                    "ItemId": object_id,
                                    "IsExist": True,
                                    "Propertyies": updated_properties,
                                }],
                            }],
                        },
                    },
                    timeout,
                ))
            if ok:
                detail = post_json(
                    f"{frontend_url}/api/v1/data/querydatadetail",
                    {"ViewId": view_id, "ObjId": object_id},
                    timeout,
                )
                ok = detail_child_field_fmt_value(detail, group_key, object_id, expected["Key"]) == expected["Value"]
            if ok:
                ok = common_void_ok(post_json(
                    f"{frontend_url}/api/v1/data/saveobj",
                    {
                        "SaveObj": {
                            "Id": object_id,
                            "ViewID": str(view_id),
                            "Propertyies": create_properties,
                            "Itemproperties": [{
                                "Key": group_key,
                                "DelteItems": [{
                                    "ItemId": object_id,
                                    "IsExist": True,
                                    "Propertyies": [],
                                }],
                            }],
                        },
                    },
                    timeout,
                ))
            if ok:
                detail = post_json(
                    f"{frontend_url}/api/v1/data/querydatadetail",
                    {"ViewId": view_id, "ObjId": object_id},
                    timeout,
                )
                ok = detail_child_field_fmt_value(detail, group_key, object_id, expected["Key"]) == ""
        finally:
            cleaned = cleanup_runtime_smoke_order(object_id)
        return ok and cleaned

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

    def get_enums_ok() -> bool:
        columns = view_state.get("columns")
        if not isinstance(columns, list):
            return False
        model_id = enum_view_model_id(columns)
        if not model_id:
            return False
        payload = post_json(
            f"{frontend_url}/api/v1/data/getenums",
            {"ModelId": str(model_id)},
            timeout,
        )
        values = legacy_response_list(payload, "EnumValues")
        return any(isinstance(value, dict) and value.get("Name") and value.get("Value") is not None for value in values)

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
            "data:getenums",
            get_enums_ok,
            "POST /api/v1/data/getenums uses an enum model from the loaded View metadata",
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
            "data:querydatadetail-idexp",
            query_detail_idexp_from_loaded_view,
            "POST /api/v1/data/querydatadetail resolves legacy IdExp with loaded DetailViewId",
        ),
        (
            "view:getreaditemview-detailviews",
            read_item_view_from_loaded_view,
            "POST /api/v1/view/getreaditemview returns DetailViews child metadata",
        ),
        (
            "data:initnew",
            init_new_from_loaded_detail_view,
            "POST /api/v1/data/initnew uses loaded DetailViewId",
        ),
        (
            "data:savenewobj",
            save_new_object_from_loaded_detail_view,
            "POST /api/v1/data/savenewobj uses loaded detail View fields",
        ),
        (
            "data:saveobj",
            save_existing_object_from_loaded_detail_view,
            "POST /api/v1/data/saveobj updates a detail View object",
        ),
        (
            "data:saveobj-addeditems",
            save_added_item_from_loaded_detail_view,
            "POST /api/v1/data/saveobj writes AddedItems child rows",
        ),
        (
            "data:saveobj-items-delete",
            save_updated_deleted_item_from_loaded_detail_view,
            "POST /api/v1/data/saveobj updates and deletes child rows",
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
