#!/usr/bin/env python3
"""Focused checks for the runtime doctor helpers."""

from __future__ import annotations

import unittest

import runtime_doctor
from runtime_doctor import (
    api_checks,
    common_response_list,
    common_true_ok,
    common_void_ok,
    compose_checks,
    detail_view_id,
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

    def test_legacy_core_schema_requires_view_first_columns(self) -> None:
        schema_ok = getattr(runtime_doctor, "legacy_core_schema_ok", lambda _raw: False)
        raw = "\n".join([
            "SW_SYS_MODEL\tMODEL_ID",
            "SW_SYS_MODEL\tMODEL_NAME",
            "SW_SYS_MODEL\tMODEL_DATABASETABLE",
            "SW_SYS_MODEL\tMODEL_PARENT",
            "SW_SYS_MODEL\tMODEL_IDPROPERTY",
            "SW_SYS_MODEL\tMODEL_DEFAULTFORMAT",
            "SW_SYS_MODEL\tMODEL_TYPE",
            "SW_SYS_MODEL\tMODEL_ISVIEW",
            "SW_SYS_MODEL\tMODEL_DEFAULTLISTVIEW",
            "SW_SYS_MODEL\tMODEL_DEFAULTITEMVIEW",
            "SW_SYS_PROPERTY\tSysId",
            "SW_SYS_PROPERTY\tPROPERTY_NAME",
            "SW_SYS_PROPERTY\tPROPERTY_TYPE",
            "SW_SYS_PROPERTY\tPROPERTY_CONTYPE",
            "SW_SYS_PROPERTY\tPROPERTY_MODEL",
            "SW_SYS_PROPERTY\tPROPERTY_ISARRAY",
            "SW_SYS_PROPERTY\tPROPERTY_COLNAME",
            "SW_SYS_PROPERTY\tPROPERTY_PROPERTYNAME",
            "SW_SYS_PROPERTY\tPROPERTY_MULTIMAP",
            "SW_SYS_PROPERTY\tPROPERTY_IXGRPOUP",
            "SW_SYS_PROPERTY\tPROPERTY_ISCHECK",
            "SW_SYS_PROPERTY\tPROPERTY_GENERATIONTYPE",
            "SW_SYS_PROPERTY\tPROPERTY_ALLOWDBNULL",
            "SW_SYS_PROPERTY\tPROPERTY_CANGET",
            "SW_SYS_PROPERTY\tPROPERTY_CANSET",
            "SW_SYS_PROPERTY\tPROPERTY_FILTER",
            "SW_SYS_PROPERTY\tPROPERTY_SOURCE",
            "SW_SYS_PROPERTY\tPROPERTY_FORMAT",
            "SW_SYS_PROPERTY\tPROPERTY_SQLCON",
            "SW_SYS_PROPERTY\tSW_SYS_MODEL_PropertiesSysId",
            "SW_SYS_VIEW\tVIEW_ID",
            "SW_SYS_VIEW\tVIEW_MODEL",
            "SW_SYS_VIEW\tVIEW_DEFAULT",
            "SW_SYS_VIEW_ITEM\tSW_SYS_VIEW_ItemsVIEW_ID",
            "SW_SYS_VIEW_ITEM\tVIEW_ITEM_PROPERTY",
            "SW_SYS_VIEW_ITEM\tVIEW_ITEM_EDITTYPE",
            "SW_SYS_VIEW_OPERATION\tSW_SYS_VIEW_OperationsVIEW_ID",
            "SW_SYS_VIEW_OPERATION\tSW_VIEW_OPERATION_MODELOPERATION",
            "SW_SYS_OPERATION\tSW_SYS_MODEL_OperationsMODEL_ID",
            "SW_SYS_OPERATION\tSW_MODEL_OPERATION_BASETYPE",
            "SW_SYS_COMMANDS\tSW_SYS_OPERATION_CommandsSysId",
            "SW_SYS_COMMANDS\tSW_SYS_COMMAND_TYPE",
            "SW_SYS_OPERATIONVIEW\tSW_SYS_OPVIEW_OPREATION",
            "SW_SYS_OPERATIONVIEW_ITEM\tSW_SYS_OPERATIONVIEW_ParamsSysId",
        ])

        self.assertTrue(schema_ok(raw))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_VIEW_ITEM\tVIEW_ITEM_PROPERTY\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_MODEL\tMODEL_PARENT\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_PROPERTY\tPROPERTY_CONTYPE\n", "")))
        self.assertFalse(schema_ok(raw.replace("SW_SYS_PROPERTY\tPROPERTY_SOURCE\n", "")))

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


if __name__ == "__main__":
    unittest.main()
