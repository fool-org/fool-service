#!/usr/bin/env python3
"""Focused checks for the runtime doctor helpers."""

from __future__ import annotations

import unittest

import runtime_doctor
import runtime_schema
from runtime_doctor import (
    api_checks,
    common_response_list,
    common_true_ok,
    common_void_ok,
    compose_checks,
    detail_view_id,
    enum_view_model_id,
    legacy_app_alias_ok,
    legacy_app_default_view_id,
    legacy_login_token,
    market_symbols_schema_ok,
    legacy_response_list,
    list_view_operation_labels_ok,
    list_rows,
    lookup_view_item_id,
    parse_compose_ps,
    query_rows_match_view,
    report_grid_ok,
    report_model_columns,
    response_list_field_present,
    row_object_id,
    runoperation_result_aliases_ok,
    runtime_report_cols,
    view_column_key,
    view_columns,
)


class RuntimeDoctorTest(unittest.TestCase):
    def test_runtime_doctor_reuses_schema_catalog_module(self) -> None:
        self.assertIs(runtime_doctor.MARKET_SYMBOLS_COLUMNS, runtime_schema.MARKET_SYMBOLS_COLUMNS)
        self.assertIs(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS, runtime_schema.LEGACY_CORE_SCHEMA_COLUMNS)

    def test_parses_compose_ndjson(self) -> None:
        rows = parse_compose_ps(
            '{"Service":"backend","State":"running","Status":"Up"}\n'
            '{"Service":"frontend","State":"running","Status":"Up"}\n'
        )

        self.assertEqual(["backend", "frontend"], [row["Service"] for row in rows])

    def test_compose_checks_report_missing_and_stopped_services(self) -> None:
        results = compose_checks([
            {"Service": "backend", "State": "running", "Status": "Up"},
            {"Service": "frontend", "State": "exited", "Status": "Exited"},
            {"Service": "mysql", "State": "running", "Health": "healthy", "Status": "Up"},
            {"Service": "redis", "State": "running", "Health": "unhealthy", "Status": "Up"},
        ])

        self.assertEqual(
            {
                "compose:backend": True,
                "compose:frontend": False,
                "compose:mysql": True,
                "compose:redis": False,
            },
            {result.name: result.ok for result in results},
        )

    def test_market_symbols_schema_requires_fh_java_columns(self) -> None:
        raw = "\n".join([
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
        ])

        self.assertTrue(market_symbols_schema_ok(raw))
        self.assertFalse(market_symbols_schema_ok(raw.replace("exchange_type", "")))

    def test_legacy_core_schema_requires_view_first_and_auth_shell_columns(self) -> None:
        schema_ok = getattr(runtime_doctor, "legacy_core_schema_ok", lambda _raw: False)
        view_render_columns = (
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
        )
        auth_shell_columns = (
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
            ("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE", "SW_APP_AUTH_MENU_ID"),
            ("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE", "SW_APP_AUTH_ROLE_ID"),
        )
        app_manage_columns = (
            ("SW_APP_AUTH_COMPANY", "APP_COR_ID"),
            ("SW_APP_AUTH_COMPANY", "APP_COR_NAME"),
            ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_ID"),
            ("SW_APP_AUTH_DEPARTMENT", "SW_APP_AUTH_COMPANY_DepsAPP_COR_ID"),
            ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_NAME"),
            ("SW_APP_AUTH_DEPARTMENT", "APP_DEP_DEFAULTVIEW"),
            ("SW_APP_AUTH_DEPARTMENT_SubDepartments", "SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID"),
            ("SW_APP_AUTH_DEPARTMENT_SubDepartments", "SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM"),
            ("SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE", "SW_APP_AUTH_DEPARTMENT_ID"),
            ("SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE", "SW_APP_AUTH_ROLE_ID"),
            ("SW_SYS_MODULE", "MODULE_NAME"),
            ("SW_SYS_MODULE", "MODULE_REMARK"),
            ("SW_SYS_MODULE", "MODULE_ASSEMBLY"),
            ("SW_SYS_MODULE", "MODULE_FILENAME"),
            ("SW_SYS_MODULE", "MODULE_VERSION"),
            ("SW_SYS_MODULE", "MODULE_GENERATIONCODE"),
            ("SW_SYS_MODULE", "MODULE_CON"),
            ("SW_SYS_MODEL", "MODEL_CLASS"),
            ("SW_SYS_MODEL", "MODEL_CONTYPE"),
            ("SW_SYS_MODEL", "MODEL_MODULE"),
            ("SW_SYS_MODEL", "MODEL_AUTOID"),
            ("SW_SYS_MODEL", "MODEL_CON"),
            ("SW_SYS_MODEL", "MODEL_DEFAULTOWNER"),
            ("SW_SYS_MODEL_TRIGGER", "SysId"),
            ("SW_SYS_MODEL_TRIGGER_COMMANDS", "SysId"),
            ("SW_SYS_PROPERTY_TRIGGER", "SysId"),
            ("SW_SYS_PROPERTY_TRIGGER_COMMANDS", "SysId"),
            ("SW_SYS_OPERATION", "SysId"),
            ("SW_SYS_COMMANDS", "SysId"),
            ("SW_SYS_OPERATION_PARAM", "SysId"),
            ("SW_SYS_EMUNVALUE", "EMUN_STR"),
            ("SW_SYS_EMUNVALUE", "EMUN_VALUE"),
            ("SW_SYS_EMUNVALUE", "SW_SYS_MODEL_EnumValuesMODEL_ID"),
        )
        event_message_columns = (
            ("SW_EVT_DEF", "EVTDEF_ID"),
            ("SW_EVT_DEF", "EVTDEF_FILTER"),
            ("SW_EVT_DEF", "EVTDEF_VIEW"),
            ("SW_EVT_DEF", "EVTDEF_OPERATION"),
            ("SW_EVT_DEF", "EVTDEF_MSGFMT"),
            ("SW_EVT_DEF", "EVTDEF_TIMEOUTSECS"),
            ("SW_EVT_DEF", "EVTDEF_MODEL"),
            ("SW_EVT_DEF", "EVTDEF_MODELREF"),
            ("SW_EVT_DEF", "EVTDEF_STATE"),
            ("SW_EVT_EVENT", "EVT_ID"),
            ("SW_EVT_EVENT", "EVT_CREATETIME"),
            ("SW_EVT_EVENT", "EVT_MSG"),
            ("SW_EVT_EVENT", "EVT_DEALMSG"),
            ("SW_EVT_EVENT", "EVT_DEALTIME"),
            ("SW_EVT_EVENT", "EVT_DEALUSER"),
            ("SW_EVT_EVENT", "EVT_VIEW"),
            ("SW_EVT_EVENT", "EVT_DEF"),
            ("SW_EVT_EVENT", "EVT_Defination"),
            ("SW_SYS_MSG", "MSG_ID"),
            ("SW_SYS_MSG", "MSG_EVT"),
            ("SW_SYS_MSG", "MSG_VIEW"),
            ("SW_SYS_MSG", "MSG_OBJ"),
            ("SW_SYS_MSG", "MSG_MSG"),
            ("SW_SYS_MSG", "MSG_CREATETIME"),
            ("SW_SYS_MSG", "MSG_READTIME"),
            ("SW_SYS_MSG", "MSG_PUSHTIME"),
            ("SW_SYS_MSG", "MSG_ENDLINETIME"),
            ("SW_SYS_MSG", "MSG_STATE"),
            ("SW_SYS_MSG", "MSG_READOPERATION"),
            ("SW_SYS_MSG", "MSG_USERID"),
            ("SW_SYS_MSG", "MSG_MSGTYPE"),
        )
        event_recipient_relation_columns = (
            ("SW_APP_AUTH_USER_SW_EVT_DEF", "SW_APP_AUTH_USER_ID"),
            ("SW_APP_AUTH_USER_SW_EVT_DEF", "SW_EVT_DEF_ID"),
            ("SW_APP_AUTH_ROLE_SW_EVT_DEF", "SW_APP_AUTH_ROLE_ID"),
            ("SW_APP_AUTH_ROLE_SW_EVT_DEF", "SW_EVT_DEF_ID"),
            ("SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF", "SW_APP_AUTH_DEPARTMENT_ID"),
            ("SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF", "SW_EVT_DEF_ID"),
            ("SW_APP_AUTH_COMPANY_SW_EVT_DEF", "SW_APP_AUTH_COMPANY_ID"),
            ("SW_APP_AUTH_COMPANY_SW_EVT_DEF", "SW_EVT_DEF_ID"),
        )
        query_catalog_columns = (
            ("SE_COMPARETYPE", "SysID"),
            ("SE_COMPARETYPE", "SE_COMPARESHOW"),
            ("SE_COMPARETYPE", "SE_COMPAREEXP"),
            ("SE_COMPARETYPE_PROPERTYINDEX", "COMPARETYPE_ID"),
            ("SE_COMPARETYPE_PROPERTYINDEX", "PROPERTYTYPE_VALUE"),
            ("SE_SELECTEDTYPE", "SysID"),
            ("SE_SELECTEDTYPE", "SE_SELECTEDSHOW"),
            ("SE_SELECTEDTYPE", "SE_SELECTEDEXP"),
            ("SE_SELECTEDTYPE", "SE_REQUIREGROUP"),
            ("SE_SELECTEDTYPE_PROPERTYINDEX", "SELECTEDTYPE_ID"),
            ("SE_SELECTEDTYPE_PROPERTYINDEX", "PROPERTYTYPE_VALUE"),
        )
        runtime_enum_columns = (
            ("fool_sys_model_enum", "name"),
            ("fool_sys_model_enum", "value"),
            ("fool_sys_model_enum", "remark"),
            ("fool_sys_model_enum", "owner"),
        )
        legacy_connection_columns = (
            ("SW_SYS_CON", "SW_SYS_CON_DATASOURCE"),
            ("SW_SYS_CON", "SW_SYS_CON_INITALCATALOG"),
            ("SW_SYS_CON", "SW_SYS_CON_USERNAME"),
            ("SW_SYS_CON", "SW_SYS_CON_PASSWORD"),
            ("SW_SYS_CON", "SW_SYS_CON_INTEGRATEDSECURITY"),
            ("SW_SYS_CON", "SW_SYS_CON_ISLOACL"),
        )
        raw = "\n".join(
            f"{table}\t{column}" for table, column in runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS
        )

        self.assertTrue(set(view_render_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(auth_shell_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(app_manage_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(event_message_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(event_recipient_relation_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(query_catalog_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(runtime_enum_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(set(legacy_connection_columns).issubset(set(runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS)))
        self.assertTrue(schema_ok(raw))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_VIEW_ITEM\tVIEW_ITEM_PROPERTY\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_VIEW_ITEM\tVIEW_ITEM_NAME\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_OPERATIONVIEW\tSW_SYS_OPVIEW_ConfirmMSG\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MODEL\tMODEL_PARENT\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_PROPERTY\tPROPERTY_CONTYPE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_PROPERTY\tPROPERTY_SOURCE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_RELATION\tSW_SYS_RELATION_TABLE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MULTIMAP\tMAP_COLNAME\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_OPERATION\tSW_MODEL_OPERATION_INVOKECLASS\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MODEL_TRIGGER_COMMANDS\tSW_SYS_COMMAND_Index\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_PROPERTY_TRIGGER\tSW_PROPERTY_TRIGGER_BASETYPE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_APPLICATION\tSW_APP_VIEW\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_APP_AUTH_MENU\tAUTH_MENU_VIEWID\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MODULE\tMODULE_NAME\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_EMUNVALUE\tEMUN_STR\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_EVT_DEF\tEVTDEF_STATE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_EVT_EVENT\tEVT_Defination\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MSG\tMSG_STATE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_APP_AUTH_USER_SW_EVT_DEF\tSW_EVT_DEF_ID\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_APP_AUTH_COMPANY_SW_EVT_DEF\tSW_APP_AUTH_COMPANY_ID\n", "")))
        self.assertFalse(schema_ok(raw.replace("SE_COMPARETYPE\tSE_COMPAREEXP\n", "")))
        self.assertFalse(schema_ok(raw.replace("SE_SELECTEDTYPE_PROPERTYINDEX\tPROPERTYTYPE_VALUE", "")))
        self.assertFalse(schema_ok(raw.replace("fool_sys_model_enum\towner", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_CON\tSW_SYS_CON_INITALCATALOG\n", "")))

    def test_common_response_list_requires_success_and_nonempty_list(self) -> None:
        self.assertTrue(common_response_list({"code": 0, "data": {"items": [1]}}, "items"))
        self.assertFalse(common_response_list({"code": 1, "data": {"items": [1]}}, "items"))
        self.assertFalse(common_response_list({"code": 0, "data": {"items": []}}, "items"))

    def test_legacy_response_list_requires_pascal_alias(self) -> None:
        self.assertEqual([1], legacy_response_list({"code": 0, "data": {"Items": [1]}}, "Items"))
        self.assertEqual([], legacy_response_list({"code": 0, "data": {"items": [1]}}, "Items"))

    def test_response_list_field_present_allows_empty_legacy_lists(self) -> None:
        self.assertTrue(response_list_field_present({"code": 0, "data": {"Messages": []}}, "Messages"))
        self.assertFalse(response_list_field_present({"code": 0, "data": {"messages": []}}, "Messages"))

    def test_runoperation_result_aliases_require_legacy_result_fields(self) -> None:
        self.assertTrue(runoperation_result_aliases_ok({"code": 0, "data": {
            "Value": None,
            "IsSuccess": False,
            "ReturnObjId": None,
            "ReturnViewId": 0,
            "ReturnMsg": "",
        }}))
        self.assertFalse(runoperation_result_aliases_ok({"code": 0, "data": {"success": False}}))

    def test_common_void_ok_accepts_legacy_no_data_success(self) -> None:
        self.assertTrue(common_void_ok({"code": 0, "data": None}))
        self.assertFalse(common_void_ok({"code": 1, "data": None}))

    def test_common_true_ok_accepts_checkcode_success(self) -> None:
        self.assertTrue(common_true_ok({"code": 0, "data": True}))
        self.assertFalse(common_true_ok({"code": 0, "data": False}))

    def test_legacy_login_token_requires_success_and_token(self) -> None:
        self.assertEqual("t1", legacy_login_token({"code": 0, "data": {"loginSucess": True, "token": "t1"}}))
        self.assertEqual("t2", legacy_login_token({"code": 0, "data": {"LoginSucess": True, "Token": "t2"}}))
        self.assertEqual("", legacy_login_token({"code": 0, "data": {"loginSucess": False, "token": "t1"}}))

    def test_legacy_app_default_view_id_accepts_legacy_aliases(self) -> None:
        self.assertEqual(100, legacy_app_default_view_id({"code": 0, "data": {"app": {"defaultViewId": 100}}}))
        self.assertEqual(101, legacy_app_default_view_id({"code": 0, "data": {"App": {"DefaultViewId": 101}}}))
        self.assertEqual(0, legacy_app_default_view_id({"code": 0, "data": {"App": {"DefaultViewId": 0}}}))

    def test_legacy_app_alias_ok_requires_pascal_default_view(self) -> None:
        self.assertTrue(legacy_app_alias_ok({"code": 0, "data": {"App": {"DefaultViewId": 101}}}))
        self.assertFalse(legacy_app_alias_ok({"code": 0, "data": {"app": {"defaultViewId": 101}}}))

    def test_detail_view_id_reads_loaded_view_metadata(self) -> None:
        self.assertEqual(102, detail_view_id({"code": 0, "data": {"detailViewId": 102}}))
        self.assertEqual(0, detail_view_id({"code": 0, "data": {"detailViewId": 0}}))
        self.assertEqual(0, detail_view_id({"code": 1, "data": {"detailViewId": 102}}))

    def test_list_view_operation_labels_rejects_mojibake(self) -> None:
        self.assertTrue(list_view_operation_labels_ok({"code": 0, "data": {"Operations": [
            {"Name": "\u5220\u9664"},
            {"Name": "\u4fdd\u5b58"},
        ]}}))
        self.assertFalse(list_view_operation_labels_ok({"code": 0, "data": {"Operations": [
            {"Name": "\u00e5\u02c6\u00a0\u00e9\u2122\u00a4"},
            {"Name": "\u00e4\u00bf\u009d\u00e5\u00ad\u02dc"},
        ]}}))

    def test_query_rows_and_object_id_accept_legacy_aliases(self) -> None:
        row = {"Items": [{"PrpId": "recordId", "ObjId": "9001"}]}

        self.assertEqual([row], list_rows({"Data": [row]}))
        self.assertEqual("9001", row_object_id(row))
        self.assertEqual("1001", row_object_id({"id": "1001", "Items": [{"ObjId": "9001"}]}))

    def test_query_rows_match_loaded_view_columns(self) -> None:
        columns = view_columns({"code": 0, "data": {"Items": [
            {"PropertyName": "recordId", "Name": "Record ID"},
            {"PropertyName": "status", "Name": "Status"},
        ]}})
        rows = [
            {"Items": [
                {"PrpId": "recordId", "ObjId": "9001"},
                {"PrpId": "status", "ObjId": "ready"},
            ]},
            {"Items": [
                {"PrpId": "recordId", "ObjId": "9002"},
                {"PrpId": "status", "ObjId": "done"},
            ]},
        ]

        self.assertEqual("recordId", view_column_key(columns[0]))
        self.assertTrue(query_rows_match_view(rows, columns))
        self.assertFalse(query_rows_match_view(rows, columns + [{"PropertyName": "missing"}]))
        self.assertFalse(query_rows_match_view([{"values": {"recordId": "dto-only", "status": "ready"}}], columns))

    def test_lookup_view_item_id_uses_business_object_view_metadata(self) -> None:
        self.assertEqual("owner", lookup_view_item_id([
            {"PropertyName": "name", "PropertyType": "String", "PropertyModel": 0},
            {"PropertyName": "owner", "PropertyType": "BusinessObject", "PropertyModel": 103},
        ]))
        self.assertEqual("", lookup_view_item_id([{"PropertyName": "name", "PropertyType": "String"}]))

    def test_enum_view_model_id_uses_loaded_view_metadata(self) -> None:
        self.assertEqual(102, enum_view_model_id([
            {"PropertyName": "name", "PropertyType": "String", "PropertyModel": 0},
            {"PropertyName": "state", "PropertyType": "Enum", "PropertyModel": 102},
        ]))
        self.assertEqual(103, enum_view_model_id([
            {"propertyName": "state", "propertyType": "15", "propertyModel": "103"},
        ]))
        self.assertEqual(0, enum_view_model_id([{"PropertyName": "name", "PropertyType": "String"}]))

    def test_runtime_report_cols_come_from_report_model(self) -> None:
        columns = report_model_columns({"code": 0, "data": {"cols": [
            {"id": "recordId", "name": "Record ID", "queryTypes": [{"id": "1", "name": "原值"}]},
        ]}})

        self.assertEqual([
            {"ColName": "Record ID", "ColId": "recordId", "SelectedTypeId": "1", "Index": 1, "OrderType": "2"}
        ], runtime_report_cols(columns))

    def test_report_grid_ok_requires_expected_headers_and_data_row(self) -> None:
        self.assertTrue(report_grid_ok({"code": 0, "data": {"cells": [
            {"col": 0, "row": 0, "fmtValue": "Record ID"},
            {"col": 1, "row": 0, "fmtValue": "Status"},
            {"col": 0, "row": 1, "fmtValue": "9001"},
            {"col": 1, "row": 1, "fmtValue": "ready"},
        ]}}, ["Record ID", "Status"]))
        self.assertFalse(report_grid_ok({"code": 0, "data": {"cells": [
            {"col": 0, "row": 0, "fmtValue": "Record ID"},
            {"col": 1, "row": 0, "fmtValue": "Status"},
        ]}}, ["Record ID", "Status"]))

    def test_api_checks_do_not_fallback_to_seeded_view_id(self) -> None:
        calls: list[tuple[str, object]] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            if url.endswith("/auth/initapp"):
                return {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}}
            if url.endswith("/auth/getcheckcode"):
                return {"code": 0, "data": {"Key": "k", "Code": "c"}}
            if url.endswith("/auth/checkcode"):
                return {"code": 0, "data": True}
            if url.endswith("/auth/loginv2"):
                return {"code": 0, "data": {"LoginSucess": True, "Token": "t"}}
            if url.endswith("/auth/getuserinfo"):
                return {"code": 0, "data": {"user": {"id": "admin"}}}
            if url.endswith("/auth/getapp"):
                return {"code": 0, "data": {"App": {}}}
            if url.endswith("/auth/getmain"):
                return {"code": 0, "data": {"App": {}, "TopMenu": [{"AuthNo": "0101"}]}}
            if url.endswith("/auth/getsubmenu"):
                return {"code": 0, "data": {"Items": [{}]}}
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 102,
                    "Items": [{"PropertyName": "recordId"}],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/data/querydata"):
                return {"code": 0, "data": {"Data": [{"Items": [{"PrpId": "recordId", "ObjId": "9001"}]}]}}
            if url.endswith("/data/runoperation"):
                return {"code": 0, "data": {
                    "Value": None,
                    "IsSuccess": False,
                    "ReturnObjId": None,
                    "ReturnViewId": 0,
                    "ReturnMsg": "",
                }}
            if url.endswith("/data/querydatadetail"):
                return {"code": 0, "data": {"data": {"simpleData": [{}]}}}
            if url.endswith("/view/getreaditemview"):
                return {"code": 0, "data": {"DetailViews": [{"Items": [{"PrpId": "recordId"}]}]}}
            if url.endswith("/data/inputquery"):
                return {"code": 0, "data": {"items": [{}], "Items": [{}]}}
            if url.endswith("/report/getmkqview"):
                return {"code": 0, "data": {"Cols": [{"ID": "recordId", "Name": "Record ID"}]}}
            if url.endswith("/report/getrpt"):
                return {"code": 0, "data": {"Cells": [
                    {"Col": 0, "Row": 0, "FmtValue": "Record ID"},
                    {"Col": 0, "Row": 1, "FmtValue": "9001"},
                ]}}
            if url.endswith("/message/getmsg"):
                return {"code": 0, "data": {"Messages": []}}
            if url.endswith("/message/getnotify"):
                return {"code": 0, "data": {"Notifies": []}}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json

        by_name = {result.name: result for result in results}
        self.assertFalse(by_name["view:getlistview"].ok)
        self.assertFalse(any(url.endswith("/view/getlistview") for url, _payload in calls))

    def test_api_checks_getenums_uses_loaded_view_enum_model(self) -> None:
        calls: list[tuple[str, object]] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            if url.endswith("/auth/initapp"):
                return {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}}
            if url.endswith("/auth/getcheckcode"):
                return {"code": 0, "data": {"Key": "k", "Code": "c"}}
            if url.endswith("/auth/checkcode"):
                return {"code": 0, "data": True}
            if url.endswith("/auth/loginv2"):
                return {"code": 0, "data": {"LoginSucess": True, "Token": "t"}}
            if url.endswith("/auth/getuserinfo"):
                return {"code": 0, "data": {"user": {"id": "admin"}}}
            if url.endswith("/auth/getapp"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}}}
            if url.endswith("/auth/getmain"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}, "TopMenu": [{"AuthNo": "0101"}]}}
            if url.endswith("/auth/getsubmenu"):
                return {"code": 0, "data": {"Items": [{}]}}
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 202,
                    "Items": [
                        {"PropertyName": "recordId", "PropertyType": "String", "PropertyModel": 0},
                        {"PropertyName": "state", "PropertyType": "Enum", "PropertyModel": 300},
                    ],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/data/getenums"):
                return {"code": 0, "data": {"EnumValues": [{"Name": "Open", "Value": 0}]}}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json

        by_name = {result.name: result for result in results}
        self.assertIn("data:getenums", by_name)
        self.assertTrue(by_name["data:getenums"].ok)
        self.assertIn(("http://frontend/api/v1/data/getenums", {"ModelId": "300"}), calls)

    def test_api_checks_initnew_uses_loaded_detail_view(self) -> None:
        calls: list[tuple[str, object]] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            if url.endswith("/auth/initapp"):
                return {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}}
            if url.endswith("/auth/getcheckcode"):
                return {"code": 0, "data": {"Key": "k", "Code": "c"}}
            if url.endswith("/auth/checkcode"):
                return {"code": 0, "data": True}
            if url.endswith("/auth/loginv2"):
                return {"code": 0, "data": {"LoginSucess": True, "Token": "t"}}
            if url.endswith("/auth/getuserinfo"):
                return {"code": 0, "data": {"user": {"id": "admin"}}}
            if url.endswith("/auth/getapp"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}}}
            if url.endswith("/auth/getmain"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}, "TopMenu": [{"AuthNo": "0101"}]}}
            if url.endswith("/auth/getsubmenu"):
                return {"code": 0, "data": {"Items": [{}]}}
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 202,
                    "Items": [{"PropertyName": "recordId"}],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/view/getreaditemview"):
                return {"code": 0, "data": {"DetailViews": [{"Items": [{"PrpId": "recordId"}]}]}}
            if url.endswith("/data/initnew"):
                return {"code": 0, "data": {"Data": {"SimpleData": [{"PrpId": "recordId"}]}}}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json

        by_name = {result.name: result for result in results}
        self.assertIn("data:initnew", by_name)
        self.assertTrue(by_name["data:initnew"].ok)
        self.assertIn(("http://frontend/api/v1/data/initnew", {"ViewId": 202}), calls)

    def test_api_checks_querydatadetail_idexp_uses_loaded_detail_view_and_row_id(self) -> None:
        calls: list[tuple[str, object]] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            if url.endswith("/auth/initapp"):
                return {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}}
            if url.endswith("/auth/getcheckcode"):
                return {"code": 0, "data": {"Key": "k", "Code": "c"}}
            if url.endswith("/auth/checkcode"):
                return {"code": 0, "data": True}
            if url.endswith("/auth/loginv2"):
                return {"code": 0, "data": {"LoginSucess": True, "Token": "t"}}
            if url.endswith("/auth/getuserinfo"):
                return {"code": 0, "data": {"user": {"id": "admin"}}}
            if url.endswith("/auth/getapp"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}}}
            if url.endswith("/auth/getmain"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}, "TopMenu": [{"AuthNo": "0101"}]}}
            if url.endswith("/auth/getsubmenu"):
                return {"code": 0, "data": {"Items": [{}]}}
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 202,
                    "Items": [{"PropertyName": "recordId"}],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/data/querydata"):
                return {"code": 0, "data": {"Data": [{"Items": [{"PrpId": "recordId", "ObjId": "9001"}]}]}}
            if url.endswith("/data/querydatadetail"):
                return {"code": 0, "data": {"Data": {"SimpleData": [{"PrpId": "recordId"}]}}}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json

        by_name = {result.name: result for result in results}
        self.assertIn("data:querydatadetail-idexp", by_name)
        self.assertTrue(by_name["data:querydatadetail-idexp"].ok)
        self.assertIn(
            ("http://frontend/api/v1/data/querydatadetail", {"ViewId": 202, "ObjId": "", "IdExp": "$9001"}),
            calls,
        )

    def test_api_checks_savenewobj_uses_loaded_detail_fields(self) -> None:
        calls: list[tuple[str, object]] = []
        cleanup_ids: list[str] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json
        original_cleanup = getattr(runtime_doctor, "cleanup_runtime_smoke_order", None)

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_cleanup(object_id: str) -> bool:
            cleanup_ids.append(object_id)
            return True

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            if url.endswith("/auth/initapp"):
                return {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}}
            if url.endswith("/auth/getcheckcode"):
                return {"code": 0, "data": {"Key": "k", "Code": "c"}}
            if url.endswith("/auth/checkcode"):
                return {"code": 0, "data": True}
            if url.endswith("/auth/loginv2"):
                return {"code": 0, "data": {"LoginSucess": True, "Token": "t"}}
            if url.endswith("/auth/getuserinfo"):
                return {"code": 0, "data": {"user": {"id": "admin"}}}
            if url.endswith("/auth/getapp"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}}}
            if url.endswith("/auth/getmain"):
                return {"code": 0, "data": {"App": {"DefaultViewId": 200}, "TopMenu": [{"AuthNo": "0101"}]}}
            if url.endswith("/auth/getsubmenu"):
                return {"code": 0, "data": {"Items": [{}]}}
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 202,
                    "Items": [{"PropertyName": "recordId"}],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/data/querydata"):
                return {"code": 0, "data": {"Data": [{"Items": [{"PrpId": "recordId", "ObjId": "9001"}]}]}}
            if url.endswith("/data/querydatadetail"):
                return {"code": 0, "data": {"Data": {"SimpleData": [{"PrpId": "recordId"}]}}}
            if url.endswith("/view/getreaditemview"):
                return {"code": 0, "data": {"DetailViews": [{"Items": [{"PrpId": "itemId"}]}]}}
            if url.endswith("/data/initnew"):
                return {"code": 0, "data": {"Data": {"SimpleData": [
                    {"PrpId": "orderId", "PrpType": "Long", "ReadOnly": True},
                    {"PrpId": "symbol", "PrpType": "String", "ReadOnly": False},
                    {"PrpId": "customer", "PrpType": "BusinessObject", "ReadOnly": False},
                    {"PrpId": "state", "PrpType": "Enum", "ReadOnly": False},
                ]}}}
            if url.endswith("/data/savenewobj"):
                return {"code": 0, "data": None}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            runtime_doctor.cleanup_runtime_smoke_order = fake_cleanup
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json
            if original_cleanup is None:
                delattr(runtime_doctor, "cleanup_runtime_smoke_order")
            else:
                runtime_doctor.cleanup_runtime_smoke_order = original_cleanup

        by_name = {result.name: result for result in results}
        self.assertIn("data:savenewobj", by_name)
        self.assertTrue(by_name["data:savenewobj"].ok)
        self.assertEqual(["989902", "989902"], [object_id for object_id in cleanup_ids if object_id == "989902"])
        self.assertIn(
            ("http://frontend/api/v1/data/savenewobj", {
                "SaveObj": {
                    "Id": "989902",
                    "ViewID": "202",
                    "Propertyies": [
                        {"Key": "symbol", "Value": "RUNTIME-989902"},
                        {"Key": "state", "Value": "0"},
                    ],
                    "Itemproperties": [],
                },
            }),
            calls,
        )

    def test_api_checks_saveobj_updates_created_detail_object(self) -> None:
        calls: list[tuple[str, object]] = []
        cleanup_ids: list[str] = []
        original_get_json = runtime_doctor.get_json
        original_post_json = runtime_doctor.post_json
        original_cleanup = runtime_doctor.cleanup_runtime_smoke_order

        def fake_get_json(_url: str, _timeout: float) -> object:
            return []

        def fake_cleanup(object_id: str) -> bool:
            cleanup_ids.append(object_id)
            return True

        def fake_post_json(url: str, payload: object, _timeout: float) -> dict[str, object]:
            calls.append((url, payload))
            suffixes: dict[str, dict[str, object]] = {
                "/auth/initapp": {"code": 0, "data": {"Dbs": [{}], "CheckCode": {"Key": "k", "Code": "c"}}},
                "/auth/getcheckcode": {"code": 0, "data": {"Key": "k", "Code": "c"}},
                "/auth/checkcode": {"code": 0, "data": True},
                "/auth/loginv2": {"code": 0, "data": {"LoginSucess": True, "Token": "t"}},
                "/auth/getuserinfo": {"code": 0, "data": {"user": {"id": "admin"}}},
                "/auth/getapp": {"code": 0, "data": {"App": {"DefaultViewId": 200}}},
                "/auth/getmain": {"code": 0, "data": {"App": {"DefaultViewId": 200}, "TopMenu": [{"AuthNo": "0101"}]}},
                "/auth/getsubmenu": {"code": 0, "data": {"Items": [{}]}},
                "/view/getreaditemview": {"code": 0, "data": {"DetailViews": [{"Items": [{"PrpId": "itemId"}]}]}},
                "/data/savenewobj": {"code": 0, "data": None},
                "/data/saveobj": {"code": 0, "data": None},
            }
            for suffix, response in suffixes.items():
                if url.endswith(suffix):
                    return response
            if url.endswith("/view/getlistview"):
                return {"code": 0, "data": {
                    "DetailViewId": 202,
                    "Items": [{"PropertyName": "recordId"}],
                    "Operations": [{"Name": "\u5220\u9664"}, {"Name": "\u4fdd\u5b58"}],
                }}
            if url.endswith("/data/querydata"):
                return {"code": 0, "data": {"Data": [{"Items": [{"PrpId": "recordId", "ObjId": "9001"}]}]}}
            if url.endswith("/data/initnew"):
                return {"code": 0, "data": {"Data": {"SimpleData": [
                    {"PrpId": "orderId", "PrpType": "Long", "ReadOnly": True},
                    {"PrpId": "symbol", "PrpType": "String", "ReadOnly": False},
                    {"PrpId": "customer", "PrpType": "BusinessObject", "ReadOnly": False},
                    {"PrpId": "state", "PrpType": "Enum", "ReadOnly": False},
                ]}}}
            if url.endswith("/data/querydatadetail"):
                return {"code": 0, "data": {"Data": {"SimpleData": [
                    {"PrpId": "symbol", "FmtValue": "RUNTIME-989903-UPDATE"},
                ]}}}
            return {"code": 0, "data": None}

        try:
            runtime_doctor.get_json = fake_get_json
            runtime_doctor.post_json = fake_post_json
            runtime_doctor.cleanup_runtime_smoke_order = fake_cleanup
            results = api_checks("http://backend", "http://frontend", 1.0)
        finally:
            runtime_doctor.get_json = original_get_json
            runtime_doctor.post_json = original_post_json
            runtime_doctor.cleanup_runtime_smoke_order = original_cleanup

        by_name = {result.name: result for result in results}
        self.assertIn("data:saveobj", by_name)
        self.assertTrue(by_name["data:saveobj"].ok)
        self.assertIn("989903", cleanup_ids)
        self.assertIn(
            ("http://frontend/api/v1/data/saveobj", {
                "SaveObj": {
                    "Id": "989903",
                    "ViewID": "202",
                    "Propertyies": [
                        {"Key": "symbol", "Value": "RUNTIME-989903-UPDATE"},
                        {"Key": "state", "Value": "0"},
                    ],
                    "Itemproperties": [],
                },
            }),
            calls,
        )


if __name__ == "__main__":
    unittest.main()
