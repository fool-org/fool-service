#!/usr/bin/env python3
"""Validate the tracked FoolFrame migration completion contract."""

from __future__ import annotations

import argparse
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parent.parent
DEFAULT_LEGACY_ROOT = ROOT.parent / "FoolFrame"

LEGACY_WEB_ROUTES = frozenset(
    (
        ("get", "/"),
        ("get", "/about"),
        ("get", "/contact"),
        ("post", "/user/login"),
        ("post", "/user/logout"),
        ("post", "/user/getmenu"),
        ("post", "/user/getchk"),
        ("get", "/main"),
        ("get", "/view:id"),
        ("post", "/view"),
        ("get", "/view:id/:objid"),
        ("get", "/itemview:id"),
        ("post", "/itemview"),
        ("post", "/data/querylist"),
        ("post", "/data/inputquery"),
        ("post", "/data/save"),
        ("post", "/data/new"),
        ("post", "/data/exoperation"),
        ("post", "/model/getenum"),
        ("get", "/new:id"),
        ("get", "/new:id/:objid&:ownerviewid&:prpid"),
        ("post", "/report/mkqview"),
        ("post", "/report/mkrpt"),
        ("post", "/report/saverpt"),
        ("post", "/getmsg"),
    )
)

LEGACY_SERVICE_OPERATIONS = frozenset(
    (
        "getlistview",
        "querydata",
        "querydatadetail",
        "runoperation",
        "initapp",
        "login",
        "loginv2",
        "getmain",
        "getsubmenu",
        "getcheckcode",
        "checkcode",
        "getapp",
        "getuserinfo",
        "getreaditemview",
        "getenums",
        "inputquery",
        "saveobj",
        "initnew",
        "savenewobj",
        "logout",
        "getmkqview",
        "getrpt",
        "saverpt",
        "getmsg",
        "getnotify",
    )
)

LEGACY_PROJECT_SOURCE_COUNTS = {
    "SCPB01-Soway.Data/SCPB01-Soway.Data.csproj": 45,
    "SCPB02-Soway.DB/SCPB02-Soway.DB.csproj": 24,
    "SCPB03 -Soway.DB.Manage/SCPB03 -Soway.DB.Manage.csproj": 15,
    "SCPB05-Soway.Model/SCPB05-Soway.Model.csproj": 115,
    "SCPB07-Soway.AppManage/SCPB07-Soway.AppManage.csproj": 5,
    "SCPB08-Soway.AppManage/SCPB08-Soway.AppManage.csproj": 2,
    "SCPB09-SOWAY.EVENT/SCPB09-SOWAY.EVENT.csproj": 20,
    "SWDQ01-Soway.Query/SWDQ01-Soway.Query.csproj": 46,
    "SWRPT01-Soway.Report/SWRPT01-Soway.Report.csproj": 31,
    "SWUA01-SOWAY.ORM.AUTH/SWUA01-SOWAY.ORM.AUTH.csproj": 5,
    "SWUA02-SOWAY.ORM.AUTH/SWUA02-SOWAY.ORM.AUTH.csproj": 13,
    "Soway.Server/Soway.Server.csproj": 148,
}

SERVICE_TARGET_PATHS = {
    "getlistview": "/api/v1/view/getlistview",
    "querydata": "/api/v1/data/querydata",
    "querydatadetail": "/api/v1/data/querydatadetail",
    "runoperation": "/api/v1/data/runoperation",
    "initapp": "/api/v1/auth/initapp",
    "login": "/api/v1/auth/login",
    "loginv2": "/api/v1/auth/loginv2",
    "getmain": "/api/v1/auth/getmain",
    "getsubmenu": "/api/v1/auth/getsubmenu",
    "getcheckcode": "/api/v1/auth/getcheckcode",
    "checkcode": "/api/v1/auth/checkcode",
    "getapp": "/api/v1/auth/getapp",
    "getuserinfo": "/api/v1/auth/getuserinfo",
    "getreaditemview": "/api/v1/view/getreaditemview",
    "getenums": "/api/v1/data/getenums",
    "inputquery": "/api/v1/data/inputquery",
    "saveobj": "/api/v1/data/saveobj",
    "initnew": "/api/v1/data/initnew",
    "savenewobj": "/api/v1/data/savenewobj",
    "logout": "/api/v1/auth/logout",
    "getmkqview": "/api/v1/report/getmkqview",
    "getrpt": "/api/v1/report/getrpt",
    "saverpt": "/api/v1/report/saverpt",
    "getmsg": "/api/v1/message/getmsg",
    "getnotify": "/api/v1/message/getnotify",
}

WEB_POST_TARGET_PATHS = {
    "/user/login": "/api/v1/auth/loginv2",
    "/user/logout": "/api/v1/auth/logout",
    "/user/getmenu": "/api/v1/auth/getmenu",
    "/user/getchk": "/api/v1/auth/getchk",
    "/view": "/api/v1/view/getlistview",
    "/itemview": "/api/v1/data/itemview",
    "/data/querylist": "/api/v1/data/querylist",
    "/data/inputquery": "/api/v1/data/inputquery",
    "/data/save": "/api/v1/data/save",
    "/data/new": "/api/v1/data/new",
    "/data/exoperation": "/api/v1/data/exoperation",
    "/model/getenum": "/api/v1/data/getenum",
    "/report/mkqview": "/api/v1/report/mkqview",
    "/report/mkrpt": "/api/v1/report/mkrpt",
    "/report/saverpt": "/api/v1/report/saverpt",
    "/getmsg": "/api/v1/getmsg",
}

WEB_PAGE_RUNTIME_CHECKS = {
    "/": "frontend:root-path",
    "/about": "frontend:about-path",
    "/contact": "frontend:contact-path",
    "/main": "frontend:main-path",
    "/view:id": "frontend:view-path",
    "/view:id/:objid": "frontend:view-detail-path",
    "/itemview:id": "frontend:itemview-path",
    "/new:id": "frontend:new-path",
    "/new:id/:objid&:ownerviewid&:prpid": "frontend:new-owner-path",
}

MIGRATION_DOC_MARKERS = (
    "Status: complete",
    "## Migration Completion Contract",
    "No known required migration work remains",
    "docs/migration/foolframe-server-assets.md",
)
SERVER_AUDIT_MARKERS = (
    "469 compiled C# source entries",
    "25 `IDataService` operations",
    "25 registered Express routes",
    "Scope complete",
)
VIEW_MATRIX_MARKER = "Runtime metadata/data: 118/118 passed."
FRONTEND_AUDIT_MARKERS = (
    "Root Jade pages | 12",
    "Jade includes | 8",
    "Application JavaScript modules | 23",
)


def java_api_paths(root: Path) -> set[str]:
    paths: set[str] = set()
    annotation = re.compile(
        r"@(?:Request|Post|Get|Put|Delete)Mapping\s*\(([^)]*)\)",
        re.DOTALL,
    )
    quoted = re.compile(r'"([^"]+)"')
    for source in root.glob("*/src/main/java/**/*.java"):
        text = source.read_text(encoding="utf-8")
        class_match = re.search(
            r"@RequestMapping\s*\(([^)]*)\).*?\bclass\s+",
            text,
            re.DOTALL,
        )
        base = ""
        if class_match:
            values = quoted.findall(class_match.group(1))
            base = values[0] if values else ""
        for match in annotation.finditer(text):
            if match.group(0).startswith("@RequestMapping"):
                continue
            for value in quoted.findall(match.group(1)):
                if value.startswith("/api/"):
                    paths.add(value)
                else:
                    paths.add(f"{base}{value}")
    return paths


def legacy_web_routes(legacy_root: Path) -> set[tuple[str, str]]:
    text = (legacy_root / "src/Web/app.js").read_text(encoding="utf-8")
    return set(
        re.findall(r"^\s*app\.(get|post)\('([^']+)'", text, re.MULTILINE)
    )


def legacy_service_operations(legacy_root: Path) -> set[str]:
    path = legacy_root / "src/Server/Soway.Server/IDataService.cs"
    text = path.read_text(encoding="utf-8-sig")
    return set(re.findall(r'UriTemplate\s*=\s*"([^"]+)"', text))


def legacy_project_source_counts(legacy_root: Path) -> dict[str, int]:
    server_root = legacy_root / "src/Server"
    counts: dict[str, int] = {}
    for relative_path in LEGACY_PROJECT_SOURCE_COUNTS:
        text = (server_root / relative_path).read_text(encoding="utf-8-sig")
        counts[relative_path] = len(re.findall(r"<Compile\s+Include=", text))
    return counts


def marker_errors(path: Path, markers: tuple[str, ...], label: str) -> list[str]:
    if not path.is_file():
        return [f"Missing {label}: {path}"]
    text = path.read_text(encoding="utf-8")
    return [f"{label} missing marker: {marker}" for marker in markers if marker not in text]


def validate_current(root: Path) -> list[str]:
    errors: list[str] = []
    current_paths = java_api_paths(root)
    for operation, target in sorted(SERVICE_TARGET_PATHS.items()):
        if target not in current_paths:
            errors.append(f"Legacy IDataService operation '{operation}' missing target route: {target}")
    for route, target in sorted(WEB_POST_TARGET_PATHS.items()):
        if target not in current_paths:
            errors.append(f"Legacy Web route '{route}' missing target route: {target}")

    runtime_doctor = root / "scripts/runtime_doctor.py"
    if not runtime_doctor.is_file():
        errors.append(f"Missing runtime doctor: {runtime_doctor}")
    else:
        text = runtime_doctor.read_text(encoding="utf-8")
        for route, check_name in WEB_PAGE_RUNTIME_CHECKS.items():
            if check_name not in text:
                errors.append(
                    f"Legacy Web page '{route}' missing runtime-doctor check: {check_name}"
                )

    errors.extend(marker_errors(
        root / "docs/migration/foolframe-parity.md",
        MIGRATION_DOC_MARKERS,
        "migration parity doc",
    ))
    errors.extend(marker_errors(
        root / "docs/migration/foolframe-server-assets.md",
        SERVER_AUDIT_MARKERS,
        "server coverage audit",
    ))
    errors.extend(marker_errors(
        root / "docs/migration/foolframe-frontend-assets.md",
        FRONTEND_AUDIT_MARKERS,
        "frontend coverage audit",
    ))
    errors.extend(marker_errors(
        root / "docs/migration/foolframe-view-matrix.md",
        (VIEW_MATRIX_MARKER,),
        "View route matrix",
    ))
    return errors


def validate_legacy(legacy_root: Path) -> list[str]:
    if not legacy_root.is_dir():
        return [f"Legacy FoolFrame checkout is missing: {legacy_root}"]
    errors: list[str] = []
    routes = legacy_web_routes(legacy_root)
    if routes != LEGACY_WEB_ROUTES:
        errors.append(
            "Legacy Web route snapshot drift: "
            f"missing={sorted(LEGACY_WEB_ROUTES - routes)}, "
            f"unexpected={sorted(routes - LEGACY_WEB_ROUTES)}"
        )
    operations = legacy_service_operations(legacy_root)
    if operations != LEGACY_SERVICE_OPERATIONS:
        errors.append(
            "Legacy IDataService snapshot drift: "
            f"missing={sorted(LEGACY_SERVICE_OPERATIONS - operations)}, "
            f"unexpected={sorted(operations - LEGACY_SERVICE_OPERATIONS)}"
        )
    counts = legacy_project_source_counts(legacy_root)
    for project, expected in LEGACY_PROJECT_SOURCE_COUNTS.items():
        actual = counts.get(project)
        if actual != expected:
            errors.append(
                f"Legacy Server project source-count drift: {project} expected {expected}, got {actual}"
            )
    return errors


def validate_contract(
    root: Path | str = ROOT,
    legacy_root: Path | str | None = None,
    require_legacy: bool = False,
) -> list[str]:
    repo_root = Path(root).resolve()
    errors = validate_current(repo_root)
    resolved_legacy = Path(legacy_root).resolve() if legacy_root else DEFAULT_LEGACY_ROOT
    if require_legacy or resolved_legacy.is_dir():
        errors.extend(validate_legacy(resolved_legacy))
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=str(ROOT))
    parser.add_argument("--legacy-root", default=str(DEFAULT_LEGACY_ROOT))
    parser.add_argument(
        "--require-legacy",
        action="store_true",
        help="fail when the adjacent FoolFrame checkout is unavailable",
    )
    args = parser.parse_args()
    errors = validate_contract(args.root, args.legacy_root, args.require_legacy)
    if errors:
        for error in errors:
            print(f"[FAIL] {error}")
        return 1
    print(
        "Migration completion contract passed: "
        "469 C# sources, 25 Web routes, 25 IDataService operations, 118 Views."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
