# Legacy Chart Find Page

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
with small reusable changes and View-first data binding.

## Scope

- Compared `view.jade`, `viewWithChart.jade`, `querylistdata.js`, and
  `viewWithChart.js` manual Find dispatch.
- Kept the current page only for a chart View's manual Find.
- Preserved plain View Find and scheduled-refresh page-one resets.
- Added no request type, payload field, route, DTO binding, or component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-44-24Z-legacy-chart-find-page.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` passed: 1 file, 82 tests.
- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Rebuilt and deployed frontend image
  `sha256:4d772ca09299a01ba343c80bf840e35a4139225de43844d6b27add795e1b3f84`;
  the running container used that exact image.
- Compose was healthy with `db-migrate` at `Exited (0)`; all 67
  `python scripts/runtime_doctor.py` checks passed.
- Added three `PARITY-PAGE-*` orders so chart View 100 had two pages. On Page
  2, switched to Chart and clicked Find; Page 2 retained `aria-current=page`,
  Chart stayed selected, and its rendered data still contained order 1001.
- Added seven `PARITY-ITEM-*` rows so plain View 101 had two pages. Its Page 2
  showed the legacy item, and clicking Find returned the paginator to Page 1.
- A temporary View 100 file-null probe was restored to 990001 before the plain
  View 101 acceptance because the running backend retained the loaded chart
  metadata.
- Removed all temporary rows. `market_order` returned to 8 rows,
  `market_order_item` returned to 4, View 100's file/interval metadata was
  990001/0, and its compatibility interval row was zero.

## Risks And Follow-ups

- The conditional intentionally distinguishes only metadata-selected chart
  Views; plain list and scheduled timer semantics retain their old resets.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
