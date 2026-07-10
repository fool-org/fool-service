#!/usr/bin/env python3
"""Focused checks for the repository harness."""

from __future__ import annotations

from pathlib import Path
import tempfile
import unittest

import check_repo_harness as harness
from check_repo_harness import (
    HarnessReport,
    check_java_package_boundaries,
    check_migration_parity_contract,
    check_source_file_sizes,
    check_vue_view_data_boundaries,
)


class SourceFileSizeContractTest(unittest.TestCase):
    def test_parses_docker_init_create_and_repair_columns(self) -> None:
        columns = harness.docker_init_schema_columns(
            """
            CREATE TABLE IF NOT EXISTS `SE_COMPARETYPE` (
              `SysID` bigint NOT NULL,
              `SE_COMPARESHOW` varchar(64) NOT NULL,
              PRIMARY KEY (`SysID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            SET @ddl = 'ALTER TABLE `SW_SYS_MODEL` ADD COLUMN `MODEL_PARENT` bigint DEFAULT NULL';
            """
        )

        self.assertIn(("SE_COMPARETYPE", "SysID"), columns)
        self.assertIn(("SE_COMPARETYPE", "SE_COMPARESHOW"), columns)
        self.assertIn(("SW_SYS_MODEL", "MODEL_PARENT"), columns)

    def test_reports_docker_init_schema_drift_from_runtime_doctor_catalog(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            init_dir = root / "docker" / "mysql" / "init"
            init_dir.mkdir(parents=True)
            for file_name in harness.REQUIRED_DOCKER_INIT_SQL_FILES:
                (init_dir / file_name).write_text("", encoding="utf-8")
            (init_dir / "005-model.sql").write_text(
                "CREATE TABLE IF NOT EXISTS `SW_SYS_MODEL` (`MODEL_ID` bigint NOT NULL);\n",
                encoding="utf-8",
            )
            report = HarnessReport(root=root)
            original_legacy = harness.LEGACY_CORE_SCHEMA_COLUMNS
            original_market = harness.MARKET_SYMBOLS_COLUMNS
            original_markers = harness.REQUIRED_DOCKER_INIT_SQL_MARKERS

            try:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = (
                    ("SW_SYS_MODEL", "MODEL_ID"),
                    ("SW_SYS_MODEL", "MODEL_PARENT"),
                )
                harness.MARKET_SYMBOLS_COLUMNS = ()
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = ()
                harness.check_docker_init_schema_contract(root, report)
            finally:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = original_legacy
                harness.MARKET_SYMBOLS_COLUMNS = original_market
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = original_markers

            self.assertEqual(
                [
                    "Docker init schema missing runtime-doctor column: "
                    "SW_SYS_MODEL.MODEL_PARENT"
                ],
                report.errors,
            )

    def test_reports_missing_required_docker_init_sql_file(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            init_dir = root / "docker" / "mysql" / "init"
            init_dir.mkdir(parents=True)
            (init_dir / "001-market-order.sql").write_text("SELECT 1;\n", encoding="utf-8")
            report = HarnessReport(root=root)
            original_legacy = harness.LEGACY_CORE_SCHEMA_COLUMNS
            original_market = harness.MARKET_SYMBOLS_COLUMNS
            original_markers = harness.REQUIRED_DOCKER_INIT_SQL_MARKERS

            try:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = ()
                harness.MARKET_SYMBOLS_COLUMNS = ()
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = ()
                harness.check_docker_init_schema_contract(root, report)
            finally:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = original_legacy
                harness.MARKET_SYMBOLS_COLUMNS = original_market
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = original_markers

            self.assertIn(
                "Missing Docker init SQL file: docker/mysql/init/010-query.sql",
                report.errors,
            )

    def test_reports_missing_required_docker_init_seed_marker(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            init_dir = root / "docker" / "mysql" / "init"
            init_dir.mkdir(parents=True)
            for file_name in harness.REQUIRED_DOCKER_INIT_SQL_FILES:
                (init_dir / file_name).write_text("", encoding="utf-8")
            report = HarnessReport(root=root)
            original_legacy = harness.LEGACY_CORE_SCHEMA_COLUMNS
            original_market = harness.MARKET_SYMBOLS_COLUMNS
            original_markers = harness.REQUIRED_DOCKER_INIT_SQL_MARKERS

            try:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = ()
                harness.MARKET_SYMBOLS_COLUMNS = ()
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = ("'OrderSudoku'",)
                harness.check_docker_init_schema_contract(root, report)
            finally:
                harness.LEGACY_CORE_SCHEMA_COLUMNS = original_legacy
                harness.MARKET_SYMBOLS_COLUMNS = original_market
                harness.REQUIRED_DOCKER_INIT_SQL_MARKERS = original_markers

            self.assertEqual(
                ["Docker init SQL missing required seed marker: 'OrderSudoku'"],
                report.errors,
            )

    def test_reports_oversized_source_files(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "frontend" / "src" / "Big.vue"
            source.parent.mkdir(parents=True)
            source.write_text("x\n" * 2101, encoding="utf-8")
            report = HarnessReport(root=root)

            check_source_file_sizes(root, report)

            self.assertEqual(
                [
                    "Oversized source file: frontend/src/Big.vue has 2101 lines "
                    "(limit 2100)"
                ],
                report.errors,
            )

    def test_reports_oversized_frontend_root_component(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "frontend" / "src" / "App.vue"
            source.parent.mkdir(parents=True)
            source.write_text("x\n" * 2001, encoding="utf-8")
            report = HarnessReport(root=root)

            check_source_file_sizes(root, report)

            self.assertEqual(
                [
                    "Oversized frontend root component: frontend/src/App.vue has 2001 lines "
                    "(limit 2000)"
                ],
                report.errors,
            )

    def test_reports_java_package_boundary_violations(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "fool-view" / "src/main/java/org/fool/framework/model/Leak.java"
            source.parent.mkdir(parents=True)
            source.write_text("package org.fool.framework.model;\npublic class Leak {}\n", encoding="utf-8")
            report = HarnessReport(root=root)

            check_java_package_boundaries(root, report)

            self.assertEqual(
                [
                    "Java package boundary violation: "
                    "fool-view/src/main/java/org/fool/framework/model/Leak.java "
                    "declares org.fool.framework.model, expected org.fool.framework.view.*"
                ],
                report.errors,
            )

    def test_reports_missing_migration_parity_contract_markers(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            doc = root / "docs/migration/foolframe-parity.md"
            doc.parent.mkdir(parents=True)
            doc.write_text("## Remaining Migration Work\n../FoolFrame\n", encoding="utf-8")
            report = HarnessReport(root=root)

            check_migration_parity_contract(root, report)

            self.assertIn(
                "Migration parity doc missing required marker 'python scripts/runtime_doctor.py'",
                report.errors,
            )

    def test_reports_vue_rendering_business_dto_bindings(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "frontend" / "src" / "App.vue"
            source.parent.mkdir(parents=True)
            source.write_text(
                "const columns = Object.keys(first);\nconst cell = row.values.symbol;\n",
                encoding="utf-8",
            )
            report = HarnessReport(root=root)

            check_vue_view_data_boundaries(root, report)

            self.assertEqual(
                [
                    (
                        "Vue View/data boundary violation: frontend/src/App.vue uses "
                        "'row.values'; render from loaded View metadata and row Items instead"
                    ),
                    (
                        "Vue View/data boundary violation: frontend/src/App.vue uses "
                        "'Object.keys(first)'; render from loaded View metadata and row Items instead"
                    ),
                ],
                report.errors,
            )


if __name__ == "__main__":
    unittest.main()
