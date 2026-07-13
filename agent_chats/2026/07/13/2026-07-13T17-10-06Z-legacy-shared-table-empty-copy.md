# Legacy Shared Table Empty Copy Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View table lifecycle before
changing the shared Vue table empty states.

## Scope

- Keep the shared table shell mounted at zero columns or rows.
- Remove generic Vue empty/loading copy.
- Keep data rows suppressed until View columns exist.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-10-06Z-legacy-shared-table-empty-copy.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` and `viewWithChart.jade` render their
  list tables before query results exist.
- `../FoolFrame/src/Web/views/detailView.jade` keeps the candidate table mounted
  before the first query.
- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` appends headings,
  clears data rows, then pads to the configured page size without empty copy.

## Implementation

- Removed the column-count mount guard and both generic empty-state sentences.
- Passed an empty value list while View columns are absent, preserving the
  existing View-first guard against DTO-only row rendering.
- Reused the existing filler-row logic; added no API, DTO, component,
  dependency, composable, or CSS.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:cc8dcde9343ceb5b2441bcaca08ddfeac9d487c6be56d867de4a6162fff9b0ef`.
- Authenticated `/view100` query for `__codex_no_match__` rendered one table with
  11 rows: one View-derived heading and ten filler rows. It showed `共0条记录`
  and neither removed sentence.
- Focused screenshots at 1440x1000 and 390x844 show the fixed empty table and
  record footer without clipping or overlap; mobile columns remain scrollable.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-shared-table-empty-copy/shared-table-desktop.png`
- `artifacts/runs/20260714-legacy-shared-table-empty-copy/shared-table-mobile.png`

## Risks And Follow-Ups

- Seeded Views expose columns; the zero-column row suppression is covered by the
  source contract and frontend tests, while zero-result rendering was exercised
  through the Docker runtime.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
