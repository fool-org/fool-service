# Report Draft Reopen Parity

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `view.jade`'s static report modal and `mkreport.initquery` before
  changing Vue. Old Cancel only dismisses the modal, and reopening reloads
  candidates without clearing outputs, conditions, report name, or active tab.
- Kept `ViewReportPanel` mounted for the current list View while separating its
  visibility from its lifetime.
- Reloaded View-derived report metadata on every open while retaining the
  component-local draft. The existing View-keyed component still resets on
  navigation to another View.
- Added no report DTO, App-level business state, new request route, dependency,
  or abstraction. `ViewReportPanel.vue` remains 397 lines.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T21-59-22Z-report-draft-reopen.md`

## Validation

- `cd frontend && npx vitest run src/ViewReportPanel.test.ts
  src/payload.test.ts` (2 files, 92 tests passed)
- `cd frontend && npm test` (20 files, 206 tests passed)
- `cd frontend && npm run build`
- `git diff --check`
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2106 lines against the
  repository limit of 2100.
- Built exact implementation commit `6d4714b4` from a clean archive with
  Buildx state redirected to `/private/tmp`. The running image is
  `sha256:ca2a3d4894cc777a7255db647b80bfe30a96ef86b7eda1aa9780957d03dc4b76`,
  with entry `index-CvJ-YQFt.js`, report chunk
  `ViewReportPanel-BHiJuoyK.js`, and editor chunk
  `MetadataFieldEditor-BQJAjYF6.js`.
- Authorized isolated-browser acceptance read a fresh local CAPTCHA, logged in
  with `admin/admin`, entered `/view101`, and supplied a two-column HTTP-200
  report metadata response. Logout returned HTTP 200.
- The first open selected output `草稿字段[原值]`, entered condition value
  `draft-value`, switched to Save, and entered report name `草稿报表`. Cancel
  closed the dialog.
- The second open raised the metadata-request count to two while preserving the
  selected Save tab, report name, selected output, one condition row, and its
  value. No browser console or page errors occurred.
- Post-acceptance MySQL checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.
- Docker Compose remained healthy, with `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- Seeded Market metadata does not expose a compact two-column report fixture,
  so the report metadata response was synthetic HTTP 200 while authentication,
  View routing, controls, dialog lifecycle, and logout remained live.
- Repository harness status remains blocked only by the concurrent oversized
  Java test noted above; frontend tests, type checking, Vite build, clean-image
  build, runtime interaction, and database invariants passed.
- Installation, Agent Session, app-manage, Maven, and initialization work
  remains unrelated and unstaged.
