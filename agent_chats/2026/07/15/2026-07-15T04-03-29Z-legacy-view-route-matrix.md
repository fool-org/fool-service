# Legacy View Route Matrix

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits.
Trace each page from the legacy View renderer before following data, and avoid
binding shared Vue components to concrete business DTOs.

## Scope

- Derived route classification from FoolFrame `routes/index.js`, Jade pages,
  and Sudoku include templates before querying current metadata.
- Kept View type, entry role, and renderer separate: View type alone does not
  choose the old top-level template.
- Added one read-only auditor for View/template/menu/default-detail/panel/
  operation metadata and current metadata/data endpoints.
- Generated a stable row for every imported View and reviewed all special
  chart, Sudoku, Group, Item, Map, list, and detail roles.

## Changed Files

- `scripts/legacy_view_matrix.py`
- `scripts/legacy_view_matrix_test.py`
- `docs/migration/foolframe-view-matrix.md`
- `docs/migration/foolframe-parity.md`
- `docs/validation.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-03-29Z-legacy-view-route-matrix.md`

## Commits

- `173de400 test(migration): audit legacy view route matrix`

## Validation

- `PYTHONPATH=scripts python -m unittest scripts/legacy_view_matrix_test.py`
  (5 tests passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py --output
  docs/migration/foolframe-view-matrix.md` (118/118 passed).
- `python scripts/check_repo_harness.py`.
- Frontend tests/build and Maven tests were not rerun because this slice found
  no frontend/backend implementation mismatch and changed no application code.

## Runtime Evidence

- Catalog classes: 58 ordinary lists, one chart, one Sudoku, 57 details, and
  one Map panel.
- All 61 list-like Views passed `getlistview + querydata`.
- All 57 detail Views passed `getreaditemview + initnew`.
- The auditor used the authorized local CAPTCHA and `admin/admin`, then logged
  out. It performs no save, delete, or operation request.
- Backend image:
  `sha256:72885b14ed031835c43f2e5ad7aceda1f709e9ec74696046da741d5d55c25318`.
- Frontend image:
  `sha256:f8c91559a1c8720a9532f2d18f349bf18a173f014c512066ede2665f271a2bf0`.

## Risks And Follow-ups

- Metadata/data initialization parity does not prove mutation behavior.
- Next, classify genuinely editable cases and run reversible new/edit/save,
  child-row, delete, and View-operation browser acceptance with database
  restoration after each case.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.
