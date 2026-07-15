#!/usr/bin/env python3
"""Focused tests for the legacy View route matrix."""

from __future__ import annotations

import unittest

from legacy_view_matrix import (
    LegacyView,
    entry_refs,
    legacy_renderer,
    metadata_matches,
    parse_views,
    route_surface,
    view_kind,
)


def view(**changes: object) -> LegacyView:
    values: dict[str, object] = {
        "view_id": 100,
        "name": "Orders",
        "view_type": 0,
        "template": "",
        "model_id": 10,
        "detail_view_id": 0,
        "can_edit": False,
        "auto_refresh": 0,
        "menu_count": 0,
        "app_default_count": 0,
        "detail_target_count": 0,
        "incoming_panels": (),
        "outgoing_panels": (),
        "row_operations": 0,
        "create_operations": 0,
    }
    values.update(changes)
    return LegacyView(**values)  # type: ignore[arg-type]


class LegacyViewMatrixTest(unittest.TestCase):
    def test_parses_mysql_tsv(self) -> None:
        rows = parse_views(
            "100\tOrderList\t0\tviewWithChart\t10\t102\t0\t5\t1\t1\t0\t./includes/List\t./includes/Item\t2\t0\n"
        )

        self.assertEqual(1, len(rows))
        self.assertEqual(("./includes/List",), rows[0].incoming_panels)
        self.assertEqual(2, rows[0].row_operations)

    def test_classifies_route_roles_from_old_render_contract(self) -> None:
        chart = view(template="viewWithChart")
        detail = view(view_id=102, view_type=1, can_edit=True)
        map_panel = view(view_id=105, view_type=3, incoming_panels=("./includes/Map",))

        self.assertEqual(("chart", "viewWithChart", "/view100"),
                         (view_kind(chart), legacy_renderer(chart), route_surface(chart)))
        self.assertEqual(("detail", "detailView/item", "/view102/:id; /new102; /itemview102"),
                         (view_kind(detail), legacy_renderer(detail), route_surface(detail)))
        self.assertEqual(("map-panel", "includes/Map", "parent Sudoku/Group"),
                         (view_kind(map_panel), legacy_renderer(map_panel), route_surface(map_panel)))

    def test_records_all_entry_reference_types(self) -> None:
        source = view(
            menu_count=1,
            app_default_count=1,
            detail_target_count=2,
            incoming_panels=("./includes/List", "./includes/linechart"),
            outgoing_panels=("./includes/Item",),
        )

        self.assertEqual(
            "app-default,menu,detail:2,panel:List,panel:linechart,contains:Item",
            entry_refs(source),
        )

    def test_metadata_match_uses_template_and_view_type(self) -> None:
        chart = view(template="viewWithChart")

        self.assertTrue(metadata_matches(chart, {
            "code": 0,
            "data": {"id": 100, "TempFile": "viewWithChart", "ShowType": "ListView"},
        }))
        self.assertFalse(metadata_matches(chart, {
            "code": 0,
            "data": {"id": 100, "TempFile": "Sudoku", "ShowType": "ListView"},
        }))

    def test_metadata_match_uses_detail_view_aliases(self) -> None:
        detail = view(view_id=102, view_type=1)

        self.assertTrue(metadata_matches(detail, {
            "code": 0,
            "data": {"viewId": 102, "viewName": "OrderDetail"},
        }))
        self.assertFalse(metadata_matches(detail, {
            "code": 0,
            "data": {"viewId": 103, "viewName": "OrderDetail"},
        }))


if __name__ == "__main__":
    unittest.main()
