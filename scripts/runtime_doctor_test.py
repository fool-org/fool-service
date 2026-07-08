#!/usr/bin/env python3
"""Focused checks for the runtime doctor helpers."""

from __future__ import annotations

import unittest

from runtime_doctor import (
    common_response_list,
    common_true_ok,
    common_void_ok,
    compose_checks,
    detail_view_id,
    legacy_app_alias_ok,
    legacy_app_default_view_id,
    legacy_login_token,
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


if __name__ == "__main__":
    unittest.main()
