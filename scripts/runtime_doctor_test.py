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
    parse_compose_ps,
    report_grid_ok,
    row_object_id,
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

    def test_report_grid_ok_requires_headers_and_open_row(self) -> None:
        self.assertTrue(report_grid_ok({"code": 0, "data": {"cells": [
            {"col": 0, "row": 0, "fmtValue": "Symbol"},
            {"col": 1, "row": 0, "fmtValue": "State"},
            {"col": 0, "row": 1, "fmtValue": "BTC-USDT"},
            {"col": 1, "row": 1, "fmtValue": "Open"},
        ]}}))
        self.assertFalse(report_grid_ok({"code": 0, "data": {"cells": [
            {"col": 0, "row": 0, "fmtValue": "Symbol"},
            {"col": 1, "row": 0, "fmtValue": "State"},
        ]}}))


if __name__ == "__main__":
    unittest.main()
