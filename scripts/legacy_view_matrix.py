#!/usr/bin/env python3
"""Audit legacy View routes, renderers, and read-only runtime coverage."""

from __future__ import annotations

import argparse
import subprocess
from dataclasses import dataclass, replace
from pathlib import Path
from typing import Any

from runtime_doctor import common_response_ok, legacy_login_token, post_json


VIEW_MATRIX_QUERY = r"""
SELECT
  v.VIEW_ID,
  v.VIEW_NAME,
  v.VIEW_TYPE,
  COALESCE(vf.VIEW_FILE_FILENAME, ''),
  COALESCE(v.VIEW_MODEL, 0),
  COALESCE(v.VIEW_DEFAULT, 0),
  v.VIEW_CANEDIT,
  v.VIEW_AUTOFRESHINTERVAL,
  (SELECT COUNT(*) FROM SW_APP_AUTH_MENU m WHERE m.AUTH_MENU_VIEWID = v.VIEW_ID),
  (SELECT COUNT(*) FROM SW_APPLICATION a WHERE a.SW_APP_VIEW = v.VIEW_ID),
  (SELECT COUNT(*) FROM SW_SYS_VIEW p WHERE p.VIEW_DEFAULT = v.VIEW_ID),
  COALESCE((
    SELECT GROUP_CONCAT(DISTINCT incoming_file.VIEW_FILE_FILENAME
      ORDER BY incoming_file.VIEW_FILE_FILENAME SEPARATOR ',')
    FROM SW_SYS_VIEW_ITEM incoming
    LEFT JOIN SW_SYS_VIEW_FILE incoming_file ON incoming_file.VIEW_FILE_ID = incoming.VIEW_ITEM_FILE
    WHERE incoming.VIEW_ITEM_SUBVIEW = v.VIEW_ID
  ), ''),
  COALESCE((
    SELECT GROUP_CONCAT(DISTINCT outgoing_file.VIEW_FILE_FILENAME
      ORDER BY outgoing_file.VIEW_FILE_FILENAME SEPARATOR ',')
    FROM SW_SYS_VIEW_ITEM outgoing
    LEFT JOIN SW_SYS_VIEW_FILE outgoing_file ON outgoing_file.VIEW_FILE_ID = outgoing.VIEW_ITEM_FILE
    WHERE outgoing.SW_SYS_VIEW_ItemsVIEW_ID = v.VIEW_ID
      AND outgoing.VIEW_ITEM_FILE IS NOT NULL
  ), ''),
  (SELECT COUNT(*) FROM SW_SYS_VIEW_OPERATION op
    WHERE op.SW_SYS_VIEW_OperationsVIEW_ID = v.VIEW_ID
      AND op.SW_VIEW_OPERATION_REQUIRESELECTB <> 0),
  (SELECT COUNT(*) FROM SW_SYS_VIEW_OPERATION op
    WHERE op.SW_SYS_VIEW_OperationsVIEW_ID = v.VIEW_ID
      AND op.SW_VIEW_OPERATION_REQUIRESELECTB = 0)
FROM SW_SYS_VIEW v
LEFT JOIN SW_SYS_VIEW_FILE vf ON vf.VIEW_FILE_ID = v.VIEW_FILE
ORDER BY v.VIEW_TYPE, v.VIEW_ID
""".strip()


@dataclass(frozen=True)
class LegacyView:
    view_id: int
    name: str
    view_type: int
    template: str
    model_id: int
    detail_view_id: int
    can_edit: bool
    auto_refresh: int
    menu_count: int
    app_default_count: int
    detail_target_count: int
    incoming_panels: tuple[str, ...]
    outgoing_panels: tuple[str, ...]
    row_operations: int
    create_operations: int
    metadata_ok: bool = False
    data_ok: bool = False


def parse_views(raw: str) -> list[LegacyView]:
    views: list[LegacyView] = []
    for line in raw.splitlines():
        if not line.strip():
            continue
        values = line.split("\t")
        if len(values) != 15:
            raise ValueError(f"expected 15 columns, got {len(values)}")
        views.append(LegacyView(
            view_id=int(values[0]),
            name=values[1],
            view_type=int(values[2]),
            template=values[3],
            model_id=int(values[4]),
            detail_view_id=int(values[5]),
            can_edit=values[6] == "1",
            auto_refresh=int(values[7]),
            menu_count=int(values[8]),
            app_default_count=int(values[9]),
            detail_target_count=int(values[10]),
            incoming_panels=split_csv(values[11]),
            outgoing_panels=split_csv(values[12]),
            row_operations=int(values[13]),
            create_operations=int(values[14]),
        ))
    return views


def split_csv(value: str) -> tuple[str, ...]:
    return tuple(item for item in value.split(",") if item)


def view_kind(view: LegacyView) -> str:
    if view.view_type == 1:
        return "detail"
    if view.view_type == 3 and "./includes/Map" in view.incoming_panels:
        return "map-panel"
    template = normalized_template(view.template)
    return {"viewWithChart": "chart", "Sudoku": "sudoku"}.get(template, "list")


def normalized_template(template: str) -> str:
    return template.rsplit("/", 1)[-1].removesuffix(".jade") if template else "view"


def legacy_renderer(view: LegacyView) -> str:
    kind = view_kind(view)
    if kind == "detail":
        return "detailView/item"
    if kind == "map-panel":
        return "includes/Map"
    return normalized_template(view.template)


def route_surface(view: LegacyView) -> str:
    if view_kind(view) == "detail":
        return f"/view{view.view_id}/:id; /new{view.view_id}; /itemview{view.view_id}"
    if view_kind(view) == "map-panel":
        return "parent Sudoku/Group"
    return f"/view{view.view_id}"


def entry_refs(view: LegacyView) -> str:
    refs: list[str] = []
    if view.app_default_count:
        refs.append("app-default")
    if view.menu_count:
        refs.append("menu")
    if view.detail_target_count:
        refs.append(f"detail:{view.detail_target_count}")
    refs.extend(f"panel:{normalized_template(panel)}" for panel in view.incoming_panels)
    refs.extend(f"contains:{normalized_template(panel)}" for panel in view.outgoing_panels)
    return ",".join(refs) or "direct"


def run_mysql() -> list[LegacyView]:
    completed = subprocess.run(
        [
            "docker", "compose", "exec", "-T", "mysql", "mysql",
            "--default-character-set=utf8mb4", "-uroot", "-pPa88word",
            "-N", "-B", "car_wash", "-e", VIEW_MATRIX_QUERY,
        ],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return parse_views(completed.stdout)


def login(frontend_url: str, timeout: float) -> str:
    check = post_json(f"{frontend_url}/api/v1/auth/getcheckcode", {}, timeout)
    if not common_response_ok(check):
        raise RuntimeError("could not read local check code")
    data = check["data"]
    token = legacy_login_token(post_json(
        f"{frontend_url}/api/v1/auth/loginv2",
        {
            "UserId": "admin",
            "PassWord": "admin",
            "DbId": "car_wash",
            "CheckCode": data.get("Code"),
            "CheckCodeKey": data.get("Key"),
            "AppId": "fool-service",
            "AppKey": "fool-service",
        },
        timeout,
    ))
    if not token:
        raise RuntimeError("admin login failed")
    return token


def audit_view(view: LegacyView, frontend_url: str, token: str, timeout: float) -> LegacyView:
    detail = view_kind(view) == "detail"
    metadata_path = "getreaditemview" if detail else "getlistview"
    metadata = post_json(
        f"{frontend_url}/api/v1/view/{metadata_path}",
        {"Token": token, "ViewId": view.view_id},
        timeout,
    )
    metadata_ok = metadata_matches(view, metadata)
    data_path = "initnew" if detail else "querydata"
    data_payload: dict[str, Any] = {"Token": token, "ViewId": view.view_id}
    if detail:
        data_payload["ParentObjId"] = ""
    else:
        data_payload.update({"PageSize": 1, "PageIndex": 1, "OrderByItem": 0, "OrderByType": 0})
    data = post_json(f"{frontend_url}/api/v1/data/{data_path}", data_payload, timeout)
    return replace(view, metadata_ok=metadata_ok, data_ok=common_response_ok(data))


def metadata_matches(view: LegacyView, payload: dict[str, Any]) -> bool:
    if not common_response_ok(payload):
        return False
    data = payload["data"]
    actual_id = data.get("viewId", data.get("ViewId", data.get("id", data.get("ID"))))
    if str(actual_id) != str(view.view_id):
        return False
    if view_kind(view) == "detail":
        return bool(data.get("ViewName") or data.get("viewName"))
    actual = str(data.get("TempFile") or data.get("tempFile") or "")
    actual_type = str(data.get("ShowType", data.get("showType")) or "")
    return (
        normalized_template(actual) == normalized_template(view.template)
        and actual_type == {0: "ListView", 3: "MapView"}.get(view.view_type, "")
    )


def render_markdown(views: list[LegacyView]) -> str:
    counts: dict[str, int] = {}
    for view in views:
        counts[view_kind(view)] = counts.get(view_kind(view), 0) + 1
    summary = ", ".join(f"{kind}={count}" for kind, count in sorted(counts.items()))
    passed = sum(view.metadata_ok and view.data_ok for view in views)
    lines = [
        "# FoolFrame View Route Matrix",
        "",
        "Generated from the Docker `SW_SYS_VIEW` catalog by",
        "`python scripts/legacy_view_matrix.py --output docs/migration/foolframe-view-matrix.md`.",
        "",
        "Classification follows `../FoolFrame/src/Web/routes/index.js`: list routes",
        "select `view`, `viewWithChart`, or `Sudoku` from `TempFile`; object, new,",
        "and schema routes use `detailView.jade` and `item.jade`; Map is embedded",
        "through `Sudoku.jade` and `includes/Map.jade`.",
        "",
        f"Catalog: {len(views)} Views ({summary}). Runtime metadata/data: {passed}/{len(views)} passed.",
        "",
        "`Entry` records application/menu/default-detail/panel references. `Ops` is",
        "`create/row`. Runtime checks pair `getlistview + querydata` for list-like",
        "Views and `getreaditemview + initnew` for detail Views without persisting data.",
        "",
        "| ID | Name | Kind | Renderer | Entry | Routes | Ops | Runtime |",
        "|---:|---|---|---|---|---|---:|---|",
    ]
    for view in views:
        runtime = "pass" if view.metadata_ok and view.data_ok else "FAIL"
        ops = f"{view.create_operations}/{view.row_operations}"
        lines.append(
            f"| {view.view_id} | {escape(view.name)} | {view_kind(view)} | "
            f"{legacy_renderer(view)} | {entry_refs(view)} | {route_surface(view)} | {ops} | {runtime} |"
        )
    lines.extend([
        "",
        "## Interaction Contract",
        "",
        "- `list`: find, report, paging, and metadata-declared create/row operations.",
        "- `chart`: find, data/chart tabs, paging, and metadata-declared row operations.",
        "- `sudoku`: List/Group/Map/Item/line-chart panels and List refresh/paging.",
        "- `detail`: schema-only item metadata plus read/new/edit/save/child interactions.",
        "- `map-panel`: map content and the old passive timestamp/refresh text.",
    ])
    return "\n".join(lines) + "\n"


def escape(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--frontend-url", default="http://localhost:8081")
    parser.add_argument("--output", type=Path)
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--skip-api", action="store_true")
    args = parser.parse_args()
    views = run_mysql()
    if not args.skip_api:
        token = login(args.frontend_url, args.timeout)
        try:
            views = [audit_view(view, args.frontend_url, token, args.timeout) for view in views]
        finally:
            post_json(f"{args.frontend_url}/api/v1/auth/logout", {"Token": token}, args.timeout)
    document = render_markdown(views)
    if args.output:
        args.output.write_text(document, encoding="utf-8")
    print(next(line for line in document.splitlines() if line.startswith("Catalog:")))
    return 0 if all(view.metadata_ok and view.data_ok for view in views) or args.skip_api else 1


if __name__ == "__main__":
    raise SystemExit(main())
