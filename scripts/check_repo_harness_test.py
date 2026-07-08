#!/usr/bin/env python3
"""Focused checks for the repository harness."""

from __future__ import annotations

from pathlib import Path
import tempfile
import unittest

from check_repo_harness import (
    HarnessReport,
    check_java_package_boundaries,
    check_migration_parity_contract,
    check_source_file_sizes,
)


class SourceFileSizeContractTest(unittest.TestCase):
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


if __name__ == "__main__":
    unittest.main()
