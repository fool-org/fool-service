# Frontend Asset Coverage Audit

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits.
Trace real legacy behavior before adding code and keep shared Vue components
free of business DTO bindings.

## Scope

- Inventoried all old root Jade pages, includes, and application JavaScript
  modules.
- Classified active behavior separately from framework bootstrap, dead
  fragments, and generated placeholder pages.
- Compared the inventory with current imported View metadata and the existing
  route matrix.
- Added no application code because the audit found no unmapped reachable
  frontend behavior.

## Changed Files

- `docs/migration/foolframe-frontend-assets.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T07-34-12Z-frontend-asset-coverage.md`

## Validation

- Source inventory: 12 root Jade pages, 8 includes, and 23 application
  JavaScript modules.
- Old-source reference audit: `TopBar.jade` and `leftbar.jade` are
  unreferenced; `reqconfig.js` and `swapp.js` are framework bootstrap only.
- Current metadata: 116 default top-level Views, one `Sudoku`, and one
  `viewWithChart`.
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/check_repo_harness.py` (passed).
- MySQL audit: zero current ViewItems with a non-empty `format_regx`.

## Runtime Evidence

- Existing `docs/migration/foolframe-view-matrix.md` remains the per-View
  runtime evidence and classifies 58 lists, one chart, one Sudoku, 57 details,
  and one Map panel.
- Existing mutation evidence covers reversible new/edit/save, collection
  add/update/delete, View operation, menu image, avatar, and metadata editor
  paths; this source audit does not replace those browser artifacts.

## Skipped Or Downgraded Checks

- Frontend tests/build and browser replay were not rerun because this slice
  changes documentation only and identifies no product-code mismatch.

## Risks And Follow-ups

- New migration work still requires a concrete old-source/current-runtime
  contradiction; an unused old file is not sufficient evidence of a gap.
- Unrelated authorization design/task edits and `docs/superpowers/` were not
  edited or staged.
