#!/usr/bin/env python3
"""Validate the repository's harness and standard-engine layout."""

from __future__ import annotations

import argparse
from dataclasses import dataclass, field
import json
from pathlib import Path
import re
import sys
from xml.sax.saxutils import escape


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from standard_engine import STANDARDS, standard_catalog_payload  # noqa: E402
from runtime_schema import LEGACY_CORE_SCHEMA_COLUMNS, MARKET_SYMBOLS_COLUMNS  # noqa: E402


ROOT = SCRIPT_DIR.parent
REQUIRED_STANDARD_IDS = tuple(sorted(STANDARDS))
STANDARD_HEADINGS = (
    "## Intent",
    "## Scope",
    "## Automatic Enforcement",
    "## Evidence",
    "## Repair Path",
    "## Revision Trigger",
)
SOURCE_FILE_LINE_LIMIT = 2100
FRONTEND_APP_LINE_LIMIT = 2000
SOURCE_FILE_EXTENSIONS = frozenset((".java", ".ts", ".vue"))
SOURCE_FILE_SKIP_DIRS = frozenset((".git", "node_modules", "target", "dist"))
FRONTEND_VIEW_DATA_BOUNDARY_FILES = (
    "frontend/src/App.vue",
    "frontend/src/ListDataTable.vue",
    "frontend/src/ResultsPanel.vue",
)
FRONTEND_BUSINESS_DTO_BINDING_MARKERS = (
    "row.values",
    "row?.values",
    "Object.keys(first)",
)
JAVA_MODULE_PACKAGE_PREFIXES = {
    "fool-app-manage": "org.fool.framework.app",
    "fool-auth": "org.fool.framework.auth",
    "fool-common": "org.fool.framework.common",
    "fool-dao": "org.fool.framework.dao",
    "fool-db-manage": "org.fool.framework.dbmanage",
    "fool-dto": "org.fool.framework.dto",
    "fool-error-handler": "org.fool.framework.error",
    "fool-event": "org.fool.framework.event",
    "fool-log": "org.fool.framework.log",
    "fool-model": "org.fool.framework.model",
    "fool-query": "org.fool.framework.query",
    "fool-report": "org.fool.framework.report",
    "fool-view": "org.fool.framework.view",
}
MIGRATION_PARITY_MARKERS = (
    "../FoolFrame",
    "python scripts/runtime_doctor.py",
    "## Remaining Migration Work",
    "AppInstallGateway",
    "SCPB05-Soway.Model",
    "SWDQ01-Soway.Query",
    "SCPB09-SOWAY.EVENT",
    "SWRPT01-Soway.Report",
)
REQUIRED_DOCKER_INIT_SQL_FILES = (
    "001-market-order.sql",
    "002-app-manage.sql",
    "003-db-manage.sql",
    "004-event.sql",
    "005-model.sql",
    "006-view.sql",
    "007-auth.sql",
    "008-fh-java-market-symbols.sql",
    "010-query.sql",
)
REQUIRED_DOCKER_INIT_SQL_MARKERS = (
    "BTC-USDT",
    "'fool-service', 'fool-service', 0",
    "'00000000-0000-0000-0000-000000000100'",
    "VALUES ('admin', '', 'Admin'",
    "('0101', 'OrderList', 1, 'OrderList')",
    "'OrderSudoku'",
    "INSERT INTO `SE_COMPARETYPE`",
    "INSERT INTO `SE_SELECTEDTYPE`",
)
CREATE_TABLE_RE = re.compile(
    r"CREATE TABLE IF NOT EXISTS\s+`([^`]+)`\s*\((.*?)\)\s*(?:ENGINE\b[^;]*)?;",
    re.IGNORECASE | re.DOTALL,
)
ALTER_ADD_COLUMN_RE = re.compile(
    r"ALTER TABLE\s+`([^`]+)`\s+ADD COLUMN\s+`([^`]+)`",
    re.IGNORECASE,
)


@dataclass(frozen=True)
class FileRequirement:
    relative_path: str
    markers: tuple[str, ...]


REQUIRED_FILES = (
    FileRequirement(
        "AGENTS.md",
        (
            "## Project Shape",
            "## Source Of Truth",
            "## Validation",
            "## Delivery Evidence",
        ),
    ),
    FileRequirement(
        "docs/validation.md",
        (
            "## Change-Type Matrix",
            "## Command Surface",
            "## Report Outputs",
            "## CI Gates",
            "## Fallback / Skip Policy",
        ),
    ),
    FileRequirement(
        "docs/standards/README.md",
        (
            "## Standard Object Shape",
            "## Active Standards",
        ),
    ),
    FileRequirement(
        "scripts/check_repo_harness.py",
        (
            "validate_repo",
            "REQUIRED_STANDARD_IDS",
        ),
    ),
    FileRequirement(
        "scripts/standard_engine.py",
        (
            "STANDARDS",
            "standard_catalog_payload",
        ),
    ),
    FileRequirement(
        ".github/workflows/repo-harness.yml",
        (
            "python scripts/check_repo_harness.py",
        ),
    ),
    FileRequirement(
        "tasks.md",
        (
            "## Current Focus",
            "## Backlog",
        ),
    ),
    FileRequirement(
        "agent_chats/README.md",
        (
            "Prompt",
            "Validation",
            "Risks",
        ),
    ),
)


@dataclass
class HarnessReport:
    root: Path
    checked_files: list[str] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)

    def add_checked(self, relative_path: str) -> None:
        if relative_path not in self.checked_files:
            self.checked_files.append(relative_path)

    def to_payload(self) -> dict[str, object]:
        return {
            "status": "failed" if self.errors else "passed",
            "root": str(self.root),
            "checked_files": sorted(self.checked_files),
            "standards": standard_catalog_payload(),
            "errors": self.errors,
            "warnings": self.warnings,
        }


def read_required_text(
    root: Path,
    relative_path: str,
    report: HarnessReport,
) -> str | None:
    path = root / relative_path
    if not path.exists():
        report.errors.append(f"Missing required file: {relative_path}")
        return None
    if not path.is_file():
        report.errors.append(f"Required path is not a file: {relative_path}")
        return None
    report.add_checked(relative_path)
    return path.read_text(encoding="utf-8")


def require_markers(
    root: Path,
    requirement: FileRequirement,
    report: HarnessReport,
) -> None:
    text = read_required_text(root, requirement.relative_path, report)
    if text is None:
        return
    for marker in requirement.markers:
        if marker not in text:
            report.errors.append(
                f"Missing marker '{marker}' in {requirement.relative_path}"
            )


def check_required_files(root: Path, report: HarnessReport) -> None:
    for requirement in REQUIRED_FILES:
        require_markers(root, requirement, report)


def check_standard_docs(root: Path, report: HarnessReport) -> None:
    standards_readme = root / "docs/standards/README.md"
    readme_text = ""
    if standards_readme.exists():
        readme_text = standards_readme.read_text(encoding="utf-8")

    for standard_id, standard in sorted(STANDARDS.items()):
        if standard_id not in readme_text:
            report.errors.append(
                f"Missing standard id '{standard_id}' in docs/standards/README.md"
            )

        text = read_required_text(root, standard.owner_doc, report)
        if text is None:
            continue
        for heading in STANDARD_HEADINGS:
            if heading not in text:
                report.errors.append(f"Missing heading '{heading}' in {standard.owner_doc}")
        for marker in (
            standard.id,
            standard.enforcement,
        ):
            if marker and marker not in text:
                report.errors.append(
                    f"Missing standard marker '{marker}' in {standard.owner_doc}"
                )


def check_existing_command_surfaces(root: Path, report: HarnessReport) -> None:
    root_pom = root / "pom.xml"
    if root_pom.exists():
        report.add_checked("pom.xml")
        text = root_pom.read_text(encoding="utf-8")
        for marker in ("<packaging>pom</packaging>", "<modules>", "maven-surefire-plugin"):
            if marker not in text:
                report.errors.append(f"Missing Maven marker '{marker}' in pom.xml")

    frontend_package = root / "frontend/package.json"
    if frontend_package.exists():
        report.add_checked("frontend/package.json")
        payload = json.loads(frontend_package.read_text(encoding="utf-8"))
        scripts = payload.get("scripts", {})
        for script_name in ("test", "build"):
            if script_name not in scripts:
                report.errors.append(
                    f"Missing frontend npm script '{script_name}' in frontend/package.json"
                )

    migration_doc = root / "docs/migration/foolframe-parity.md"
    if migration_doc.exists():
        report.add_checked("docs/migration/foolframe-parity.md")
        text = migration_doc.read_text(encoding="utf-8")
        for marker in ("docker compose", "npm test", "mvn"):
            if marker not in text:
                report.warnings.append(
                    f"Migration parity doc does not mention '{marker}'"
                )


def check_readme_discovery(root: Path, report: HarnessReport) -> None:
    readme = root / "README.md"
    if not readme.exists():
        report.warnings.append("README.md is missing; AGENTS.md is the primary entrypoint")
        return
    report.add_checked("README.md")
    text = readme.read_text(encoding="utf-8")
    for marker in ("AGENTS.md", "docs/validation.md", "scripts/check_repo_harness.py"):
        if marker not in text:
            report.errors.append(f"README.md does not link harness marker '{marker}'")


def check_source_file_sizes(root: Path, report: HarnessReport) -> None:
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix not in SOURCE_FILE_EXTENSIONS:
            continue
        relative = path.relative_to(root)
        if any(part in SOURCE_FILE_SKIP_DIRS for part in relative.parts):
            continue
        with path.open(encoding="utf-8", errors="ignore") as source:
            line_count = sum(1 for _ in source)
        relative_path = relative.as_posix()
        report.add_checked(relative_path)
        if line_count > SOURCE_FILE_LINE_LIMIT:
            report.errors.append(
                f"Oversized source file: {relative_path} has {line_count} lines "
                f"(limit {SOURCE_FILE_LINE_LIMIT})"
            )
        if relative_path == "frontend/src/App.vue" and line_count > FRONTEND_APP_LINE_LIMIT:
            report.errors.append(
                f"Oversized frontend root component: {relative_path} has {line_count} lines "
                f"(limit {FRONTEND_APP_LINE_LIMIT})"
            )


def check_java_package_boundaries(root: Path, report: HarnessReport) -> None:
    for module, package_prefix in sorted(JAVA_MODULE_PACKAGE_PREFIXES.items()):
        source_root = root / module / "src/main/java"
        if not source_root.exists():
            continue
        for path in source_root.rglob("*.java"):
            relative_path = path.relative_to(root).as_posix()
            report.add_checked(relative_path)
            package_name = java_package(path)
            if not package_name:
                report.errors.append(f"Missing Java package declaration: {relative_path}")
            elif package_name != package_prefix and not package_name.startswith(f"{package_prefix}."):
                report.errors.append(
                    f"Java package boundary violation: {relative_path} declares "
                    f"{package_name}, expected {package_prefix}.*"
                )


def java_package(path: Path) -> str:
    with path.open(encoding="utf-8", errors="ignore") as source:
        for line in source:
            stripped = line.strip()
            if stripped.startswith("package ") and stripped.endswith(";"):
                return stripped.removeprefix("package ").removesuffix(";").strip()
    return ""


def check_vue_view_data_boundaries(root: Path, report: HarnessReport) -> None:
    for relative_path in FRONTEND_VIEW_DATA_BOUNDARY_FILES:
        path = root / relative_path
        if not path.exists():
            continue
        report.add_checked(relative_path)
        text = path.read_text(encoding="utf-8", errors="ignore")
        for marker in FRONTEND_BUSINESS_DTO_BINDING_MARKERS:
            if marker in text:
                report.errors.append(
                    "Vue View/data boundary violation: "
                    f"{relative_path} uses '{marker}'; "
                    "render from loaded View metadata and row Items instead"
                )


def check_migration_parity_contract(root: Path, report: HarnessReport) -> None:
    text = read_required_text(root, "docs/migration/foolframe-parity.md", report)
    if text is None:
        return
    for marker in MIGRATION_PARITY_MARKERS:
        if marker not in text:
            report.errors.append(
                f"Migration parity doc missing required marker '{marker}'"
            )


def docker_init_schema_columns(sql_text: str) -> set[tuple[str, str]]:
    columns: set[tuple[str, str]] = set()
    for table_match in CREATE_TABLE_RE.finditer(sql_text):
        table_name = table_match.group(1)
        for line in table_match.group(2).splitlines():
            column_match = re.match(r"\s*`([^`]+)`\s+", line)
            if column_match:
                columns.add((table_name, column_match.group(1)))
    for alter_match in ALTER_ADD_COLUMN_RE.finditer(sql_text):
        columns.add((alter_match.group(1), alter_match.group(2)))
    return columns


def check_docker_init_schema_contract(root: Path, report: HarnessReport) -> None:
    init_dir = root / "docker/mysql/init"
    sql_files = sorted(init_dir.glob("*.sql"))
    if not sql_files:
        report.errors.append("Missing Docker init SQL files under docker/mysql/init")
        return
    existing_names = {path.name for path in sql_files}
    for file_name in REQUIRED_DOCKER_INIT_SQL_FILES:
        if file_name not in existing_names:
            report.errors.append(f"Missing Docker init SQL file: docker/mysql/init/{file_name}")

    sql_text = ""
    for path in sql_files:
        relative_path = path.relative_to(root).as_posix()
        report.add_checked(relative_path)
        sql_text += path.read_text(encoding="utf-8", errors="ignore") + "\n"
    for marker in REQUIRED_DOCKER_INIT_SQL_MARKERS:
        if marker not in sql_text:
            report.errors.append(f"Docker init SQL missing required seed marker: {marker}")

    columns = docker_init_schema_columns(sql_text)
    required_columns = LEGACY_CORE_SCHEMA_COLUMNS + tuple(
        ("market_symbols", column) for column in MARKET_SYMBOLS_COLUMNS
    )
    for table_name, column_name in required_columns:
        if (table_name, column_name) not in columns:
            report.errors.append(
                "Docker init schema missing runtime-doctor column: "
                f"{table_name}.{column_name}"
            )


def validate_repo(root: Path | str = ROOT) -> HarnessReport:
    repo_root = Path(root).resolve()
    report = HarnessReport(root=repo_root)
    check_required_files(repo_root, report)
    check_standard_docs(repo_root, report)
    check_existing_command_surfaces(repo_root, report)
    check_readme_discovery(repo_root, report)
    check_source_file_sizes(repo_root, report)
    check_java_package_boundaries(repo_root, report)
    check_vue_view_data_boundaries(repo_root, report)
    check_migration_parity_contract(repo_root, report)
    check_docker_init_schema_contract(repo_root, report)
    return report


def write_json_report(path: Path, report: HarnessReport) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(report.to_payload(), indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def write_junit_report(path: Path, report: HarnessReport) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    failures = len(report.errors)
    lines = [
        f'<testsuite name="repo-harness" tests="1" failures="{failures}">',
        '  <testcase classname="repo" name="check_repo_harness">',
    ]
    if report.errors:
        message = escape("; ".join(report.errors))
        body = escape("\n".join(report.errors))
        lines.append(f'    <failure message="{message}">{body}</failure>')
    lines.extend(["  </testcase>", "</testsuite>"])
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--root",
        default=str(ROOT),
        help="Repository root to validate. Defaults to the current script parent.",
    )
    parser.add_argument("--report-json", help="Optional path for stable JSON output.")
    parser.add_argument("--junit-out", help="Optional path for JUnit XML output.")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    report = validate_repo(Path(args.root))

    if args.report_json:
        write_json_report(Path(args.report_json), report)
    if args.junit_out:
        write_junit_report(Path(args.junit_out), report)

    if report.errors:
        print("Repository harness validation failed:", file=sys.stderr)
        for error in report.errors:
            print(f" - {error}", file=sys.stderr)
        return 1

    print("Repository harness validation passed.")
    if report.warnings:
        print("Warnings:")
        for warning in report.warnings:
            print(f" - {warning}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
