#!/usr/bin/env python3
"""Focused checks for the repository harness."""

from __future__ import annotations

from pathlib import Path
import tempfile
import unittest

from check_repo_harness import HarnessReport, check_source_file_sizes


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


if __name__ == "__main__":
    unittest.main()
