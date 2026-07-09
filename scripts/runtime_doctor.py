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
LEGACY_CORE_SCHEMA_COLUMNS = (
    ("auth_user", "id"),
    ("auth_user", "mobile"),
    ("auth_user", "name"),
    ("auth_user", "password"),
    ("auth_user", "created_at"),
    ("auth_user", "last_login"),
    ("auth_item", "id"),
    ("auth_item", "name"),
    ("auth_item", "auth_type"),
    ("auth_item", "auth_name"),
    ("auth_user_role", "user_id"),
    ("auth_user_role", "role_id"),
    ("auth_role_auth", "role_id"),
    ("auth_role_auth", "auth_id"),
    ("SW_AUTH_USER", "USER_UID"),
    ("SW_AUTH_USER", "USER_UUID"),
    ("SW_AUTH_USER", "USER_LOGINNAME"),
    ("SW_AUTH_USER", "USER_PHONE"),
    ("SW_AUTH_USER", "USER_MAIL"),
    ("SW_AUTH_USER", "USER_FIRSTNAME"),
    ("SW_AUTH_USER", "USER_LASTNAME"),
    ("SW_AUTH_USER", "USER_SHOWNAME"),
    ("SW_AUTH_USER", "USER_TITLE"),
    ("SW_AUTH_USER", "USER_AVTAR"),
    ("SW_AUTH_USER", "USER_PWD"),
    ("SW_AUTH_USER", "USER_REGTIME"),
    ("SW_AUTH_USER", "USER_LASTLOGINTIME"),
    ("SW_AUTH_USER", "USER_LASTMODIFYTIME"),
    ("SW_AUTH_USER", "USER_SEX"),
    ("SW_AUTH_USER", "USER_DEFAULTVIEW"),
    ("SW_APPLICATION", "SW_APP_APPLICATIONID"),
    ("SW_APPLICATION", "SW_APP_KEY"),
    ("SW_APPLICATION", "SW_APP_TYPE"),
    ("SW_APPLICATION", "SW_APP_AVATAR"),
    ("SW_APPLICATION", "SW_APP_COMPANY"),
    ("SW_APPLICATION", "SW_APP_CREATEIME"),
    ("SW_APPLICATION", "SW_APP_CREATOR"),
    ("SW_APPLICATION", "SW_APP_INITPIC"),
    ("SW_APPLICATION", "SW_APP_NAME"),
    ("SW_APPLICATION", "SW_APP_NOTE"),
    ("SW_APPLICATION", "SW_APP_OWNER"),
    ("SW_APPLICATION", "SW_APP_RELEASETIME"),
    ("SW_APPLICATION", "SW_APP_UPDATETIME"),
    ("SW_APPLICATION", "SW_APP_URL"),
    ("SW_APPLICATION", "SW_APP_VERSION"),
    ("SW_APPLICATION", "SW_APP_CON"),
    ("SW_APPLICATION", "SW_APP_VIEW"),
    ("SW_STOREDB", "SW_STORE_STOREID"),
    ("SW_STOREDB", "SW_STORE_NAME"),
    ("SW_STOREDB", "SW_STORE_CON"),
    ("SW_STOREDB", "SW_STORE_Note"),
    ("SW_APPLICATION_SW_STOREDB", "SW_APPLICATION_ID"),
    ("SW_APPLICATION_SW_STOREDB", "SW_STOREDB_ID"),
    ("SW_APP_AUTH_USER", "APP_AUTH_ID"),
    ("SW_APP_AUTH_USER", "APP_AUTH_USERID"),
    ("SW_APP_AUTH_USER", "APP_AUTH_USERLOGINNAME"),
    ("SW_APP_AUTH_USER", "APP_AUTH_DEP"),
    ("SW_APP_AUTH_COMPANY", "APP_COR_ID"),
    ("SW_APP_AUTH_COMPANY", "APP_COR_NAME"),
    ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_ID"),
    ("SW_APP_AUTH_DEPARTMENT", "SW_APP_AUTH_COMPANY_DepsAPP_COR_ID"),
    ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_NAME"),
    ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_DEFAULTVIEW"),
    ("SW_APP_AUTH_DEPARTMENT_SubDepartments", "SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID"),
    ("SW_APP_AUTH_DEPARTMENT_SubDepartments", "SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_ID"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_TEXT"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_SHORTCUTKEY"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_IMAGE"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_VISIABLE"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_ENABLE"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_VIEWID"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_TEMPLATEFILE"),
    ("SW_APP_AUTH_MENU", "AUTH_MENU_INDEX"),
    ("SW_APP_AUTH_MENU_SubItems", "SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID"),
    ("SW_APP_AUTH_MENU_SubItems", "SW_APP_AUTH_MENU_SUBITEMS_ITEM"),
    ("SW_APP_AUTH_ROLE", "AUTH_ROLE_ID"),
    ("SW_APP_AUTH_ROLE", "AUTH_ROLE_NAME"),
    ("SW_APP_AUTH_ROLE_SW_APP_AUTH_USER", "SW_APP_AUTH_ROLE_ID"),
    ("SW_APP_AUTH_ROLE_SW_APP_AUTH_USER", "SW_APP_AUTH_USER_ID"),
    ("SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE", "SW_APP_AUTH_DEPARTMENT_ID"),
    ("SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE", "SW_APP_AUTH_ROLE_ID"),
    ("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE", "SW_APP_AUTH_MENU_ID"),
    ("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE", "SW_APP_AUTH_ROLE_ID"),
    ("SW_SYS_MODULE", "MODULE_NAME"),
    ("SW_SYS_MODULE", "MODULE_REMARK"),
    ("SW_SYS_MODULE", "MODULE_ASSEMBLY"),
    ("SW_SYS_MODULE", "MODULE_FILENAME"),
    ("SW_SYS_MODULE", "MODULE_VERSION"),
    ("SW_SYS_MODULE", "MODULE_GENERATIONCODE"),
    ("SW_SYS_MODULE", "MODULE_CON"),
    ("SW_SYS_MODEL", "MODEL_ID"),
    ("SW_SYS_MODEL", "MODEL_NAME"),
    ("SW_SYS_MODEL", "MODEL_CLASS"),
    ("SW_SYS_MODEL", "MODEL_CONTYPE"),
    ("SW_SYS_MODEL", "MODEL_DATABASETABLE"),
    ("SW_SYS_MODEL", "MODEL_MODULE"),
    ("SW_SYS_MODEL", "MODEL_AUTOID"),
    ("SW_SYS_MODEL", "MODEL_PARENT"),
    ("SW_SYS_MODEL", "MODEL_IDPROPERTY"),
    ("SW_SYS_MODEL", "MODEL_DEFAULTFORMAT"),
    ("SW_SYS_MODEL", "MODEL_TYPE"),
    ("SW_SYS_MODEL", "MODEL_ISVIEW"),
    ("SW_SYS_MODEL", "MODEL_CON"),
    ("SW_SYS_MODEL", "MODEL_DEFAULTOWNER"),
    ("SW_SYS_MODEL", "MODEL_DEFAULTLISTVIEW"),
    ("SW_SYS_MODEL", "MODEL_DEFAULTITEMVIEW"),
    ("SW_SYS_EMUNVALUE", "EMUN_STR"),
    ("SW_SYS_EMUNVALUE", "EMUN_VALUE"),
    ("SW_SYS_EMUNVALUE", "SW_SYS_MODEL_EnumValuesMODEL_ID"),
    ("SW_SYS_PROPERTY", "SysId"),
    ("SW_SYS_PROPERTY", "PROPERTY_NAME"),
    ("SW_SYS_PROPERTY", "PROPERTY_TYPE"),
    ("SW_SYS_PROPERTY", "PROPERTY_CONTYPE"),
    ("SW_SYS_PROPERTY", "PROPERTY_MODEL"),
    ("SW_SYS_PROPERTY", "PROPERTY_ISARRAY"),
    ("SW_SYS_PROPERTY", "PROPERTY_COLNAME"),
    ("SW_SYS_PROPERTY", "PROPERTY_PROPERTYNAME"),
    ("SW_SYS_PROPERTY", "PROPERTY_MULTIMAP"),
    ("SW_SYS_PROPERTY", "PROPERTY_IXGRPOUP"),
    ("SW_SYS_PROPERTY", "PROPERTY_ISCHECK"),
    ("SW_SYS_PROPERTY", "PROPERTY_GENERATIONTYPE"),
    ("SW_SYS_PROPERTY", "PROPERTY_ALLOWDBNULL"),
    ("SW_SYS_PROPERTY", "PROPERTY_CANGET"),
    ("SW_SYS_PROPERTY", "PROPERTY_CANSET"),
    ("SW_SYS_PROPERTY", "PROPERTY_FILTER"),
    ("SW_SYS_PROPERTY", "PROPERTY_SOURCE"),
    ("SW_SYS_PROPERTY", "PROPERTY_FORMAT"),
    ("SW_SYS_PROPERTY", "PROPERTY_SQLCON"),
    ("SW_SYS_PROPERTY", "SW_SYS_MODEL_PropertiesSysId"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_TYPE"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_SOURCEPROPERTY"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_TARGETPROPERTY"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_TABLE"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_SOURCECOL"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_TARGETCOL"),
    ("SW_SYS_RELATION", "SW_SYS_RELATION_CANBENULL"),
    ("SW_SYS_MULTIMAP", "SysId"),
    ("SW_SYS_MULTIMAP", "MAP_NAME"),
    ("SW_SYS_MULTIMAP", "MAP_COLNAME"),
    ("SW_SYS_MULTIMAP", "SW_SYS_PROPERTY_DBMapsSysId"),
    ("SW_SYS_VIEW", "VIEW_ID"),
    ("SW_SYS_VIEW", "VIEW_MODEL"),
    ("SW_SYS_VIEW", "VIEW_NAME"),
    ("SW_SYS_VIEW", "VIEW_FILTER"),
    ("SW_SYS_VIEW", "VIEW_DEFAULT"),
    ("SW_SYS_VIEW", "VIEW_TYPE"),
    ("SW_SYS_VIEW", "VIEW_CONTYPE"),
    ("SW_SYS_VIEW", "VIEW_FILE"),
    ("SW_SYS_VIEW", "VIEW_CHECKAUTH"),
    ("SW_SYS_VIEW", "VIEW_AUTOFRESHINTERVAL"),
    ("SW_SYS_VIEW", "VIEW_CANEDIT"),
    ("SW_SYS_VIEW_FILE", "VIEW_FILE_ID"),
    ("SW_SYS_VIEW_FILE", "VIEW_FILE_NAME"),
    ("SW_SYS_VIEW_FILE", "VIEW_FILE_VIEWTYPE"),
    ("SW_SYS_VIEW_FILE", "VIEW_FILE_FILENAME"),
    ("SW_SYS_VIEW_FILE", "VIEW_FILE_FILECONTENT"),
    ("SW_SYS_VIEW_ITEM", "SysId"),
    ("SW_SYS_VIEW_ITEM", "SW_SYS_VIEW_ItemsVIEW_ID"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_NAME"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_NOTE"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_FORMAT"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_PROPERTY"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_PROPERTY_SHOW"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_PROPERTY_VALUE"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_READONLY"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_INDEX"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_SUBVIEW"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_EDITVIEW"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_SELECTVIEW"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_WIDTH"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_ISSHOW"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_FILE"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_EDITTYPE"),
    ("SW_SYS_VIEW_ITEM", "VIEW_ITEM_SOURCEEXP"),
    ("SW_SYS_VIEW_OPERATION", "SysId"),
    ("SW_SYS_VIEW_OPERATION", "SW_SYS_VIEW_OperationsVIEW_ID"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_NAME"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_MODELOPERATION"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_RESULTVIEW"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_SHOWPROCESS"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_INDEX"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_REQUIRESELECTB"),
    ("SW_SYS_VIEW_OPERATION", "SW_VIEW_OPERATION_IMAGE"),
    ("SW_SYS_OPERATIONVIEW", "SysId"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_NAME"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_RESULT"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_OPREATION"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_SUCCESMSG"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_ERRORMSG"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_MSG"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_SHOW"),
    ("SW_SYS_OPERATIONVIEW", "SW_SYS_OPVIEW_ConfirmMSG"),
    ("SW_SYS_OPERATIONVIEW_ITEM", "SysId"),
    ("SW_SYS_OPERATIONVIEW_ITEM", "SW_SYS_OPERATIONVIEW_ParamsSysId"),
    ("SW_SYS_OPERATIONVIEW_ITEM", "SW_SYS_OPVIEWITEM_NAME"),
    ("SW_SYS_OPERATIONVIEW_ITEM", "SW_SYS_OPVIEWITEM_INDEX"),
    ("SW_SYS_OPERATIONVIEW_ITEM", "SW_SYS_OPVIEWITEM_PARAM"),
    ("SW_SYS_OPERATION", "SysId"),
    ("SW_SYS_OPERATION", "SW_SYS_MODEL_OperationsMODEL_ID"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_NAME"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_FILTER"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_BASETYPE"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_ARGMODEL"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_ARGFILTER"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_INVOKEDLL"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_INVOKECLASS"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_INVOKEMETHOD"),
    ("SW_SYS_OPERATION", "SW_MODEL_OPERATION_RETURNMODEL"),
    ("SW_SYS_OPERATION_PARAM", "SysId"),
    ("SW_SYS_OPERATION_PARAM", "SW_SYS_OPERATION_ParamsSysId"),
    ("SW_SYS_OPERATION_PARAM", "SW_SYS_OPERATION_PARAM_NAME"),
    ("SW_SYS_OPERATION_PARAM", "SW_SYS_OPERATION_PARAM_VIEW"),
    ("SW_SYS_OPERATION_PARAM", "SW_SYS_OPERATION_PARAM_FILTER"),
    ("SW_SYS_OPERATION_PARAM", "SW_SYS_OPERATION_PARAM_VALUE"),
    ("SW_SYS_COMMANDS", "SysId"),
    ("SW_SYS_COMMANDS", "SW_SYS_OPERATION_CommandsSysId"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_TYPE"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_PROPERTY"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_EXP"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_ARGMODEL"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_ARGEXP"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_ARGID"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_INDEX"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_PROPERTY_EXP"),
    ("SW_SYS_COMMANDS", "SW_SYS_COMMAND_TEMPVALUE"),
    ("SW_SYS_MODEL_TRIGGER", "SysId"),
    ("SW_SYS_MODEL_TRIGGER", "SW_SYS_MODEL_TriggersMODEL_ID"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_ARGMODEL"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_TYPE"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_FILTER"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_ARGFILTER"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_OPERATIONTYPE"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_INVOKEDLL"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_INVOKECLASS"),
    ("SW_SYS_MODEL_TRIGGER", "SW_MODEL_TRIGGER_INVOKEMETHOD"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SysId"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_MODEL_TRIGGER_CommandsSysId"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_TYPE"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_PROPERTY"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_EXP"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGMODEL"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGEXP"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGID"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_Index"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_PROPERTY_EXP"),
    ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SW_SYS_COMMAND_TEMPVALUE"),
    ("SW_SYS_PROPERTY_TRIGGER", "SysId"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_SYS_PROPERTY_TriggersSysId"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_ARGFILTER"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_ARGMODEL"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_FILTER"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_TYPE"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_NAME"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_PROPERTY"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_PROPERTY_TRIGGER_BASETYPE"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_MODEL_TRIGGER_INVOKEDLL"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_MODEL_TRIGGER_INVOKECLASS"),
    ("SW_SYS_PROPERTY_TRIGGER", "SW_MODEL_TRIGGER_INVOKEMETHOD"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SysId"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_PROPERTY_TRIGGER_CommandsSysId"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_TYPE"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_PROPERTY"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_EXP"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGMODEL"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGEXP"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_ARGID"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_INDEX"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_PROPERTY_EXP"),
    ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SW_SYS_COMMAND_TEMPVALUE"),
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
        "View-first legacy auth/app/model/view/operation schema is present",
    ))
    return results


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
