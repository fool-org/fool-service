# Legacy Empty Candidate Result Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old detail View and its shared query
script before changing the Vue candidate renderer.

## Scope

- Restore the old candidate table after a query returns zero rows.
- Remove only the Vue-specific empty-result sentence.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-40-52Z-legacy-candidate-result-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` keeps the candidate `#datalist`
  table mounted inside the select-existing dialog.
- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` appends the
  returned headings, removes only rows after the heading, and pads to the
  configured ten-row page even when `Data` is empty.

## Implementation

- Mounted the existing shared `ListDataTable` once candidate state is queried,
  rather than only when rows are present.
- Removed the Vue-only `暂无候选记录。` fallback. Existing metadata columns,
  ten-row filler behavior, query state, record count, and pagination are reused.
- Added no DTO, API, state, component, or CSS changes.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:293fed1cddaadf339ab5370178f022c6c5aae52bae6c044bd167b579732c4b34`.
- Authenticated `/view102/1001` opened the `选择 Items` dialog and queried
  `NO_MATCH_20260714`. The result retained one table with eleven rows: one
  metadata heading plus ten filler rows. It showed `共0条记录`, exposed no
  Select commands, and contained no `暂无候选记录。` copy.
- Stable viewport screenshots at 1440x1000 and 390x844 show the complete modal
  and table without clipping or overlap. Repository harness validation passed;
  frontend `/` and backend `/test` returned HTTP 200. MySQL and Redis remained
  healthy and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-candidate-result-empty/candidate-empty-desktop.png`
- `artifacts/runs/20260714-legacy-candidate-result-empty/candidate-empty-mobile.png`

## Risks And Follow-Ups

- The unqueried candidate area intentionally remains visually empty while its
  existing record label says `记录数未知,请查询`, matching the old timing.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
