#!/usr/bin/env python3
"""Focused tests for the FoolFrame migration completion contract."""

from __future__ import annotations

from pathlib import Path
import tempfile
import unittest

import legacy_migration_contract as contract


class LegacyMigrationContractTest(unittest.TestCase):
    def test_java_api_paths_combines_class_and_method_mappings(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "fool-test/src/main/java/example/TestController.java"
            source.parent.mkdir(parents=True)
            source.write_text(
                """
                @RequestMapping("/api/v1/test")
                public class TestController {
                    @PostMapping({"/first", "/second"})
                    void run() {}
                }
                """,
                encoding="utf-8",
            )

            self.assertEqual(
                {"/api/v1/test/first", "/api/v1/test/second"},
                contract.java_api_paths(root),
            )

    def test_validate_current_reports_removed_legacy_target_route(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            errors = contract.validate_current(Path(temp_dir))

            self.assertIn(
                "Legacy IDataService operation 'getlistview' missing target route: "
                "/api/v1/view/getlistview",
                errors,
            )

    def test_current_repository_satisfies_closed_contract(self) -> None:
        self.assertEqual([], contract.validate_current(contract.ROOT))


if __name__ == "__main__":
    unittest.main()
