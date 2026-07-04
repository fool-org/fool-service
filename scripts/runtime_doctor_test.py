#!/usr/bin/env python3
"""Focused checks for the runtime doctor helpers."""

from __future__ import annotations

import unittest

from runtime_doctor import common_response_list, compose_checks, detail_view_id, parse_compose_ps, report_grid_ok


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

    def test_detail_view_id_reads_loaded_view_metadata(self) -> None:
        self.assertEqual(102, detail_view_id({"code": 0, "data": {"detailViewId": 102}}))
        self.assertEqual(0, detail_view_id({"code": 0, "data": {"detailViewId": 0}}))
        self.assertEqual(0, detail_view_id({"code": 1, "data": {"detailViewId": 102}}))

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
