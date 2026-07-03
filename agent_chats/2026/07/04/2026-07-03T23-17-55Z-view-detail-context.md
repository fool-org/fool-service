# Prompt

User pointed out that the Vue workflow should render from View metadata first,
then query data for that View, and that binding the page to concrete business
DTOs is wrong.

# Scope

- Keep the Vue main workflow on `getlistview(ViewId)` then `querydata(ViewId)`.
- Make default row open/new-row context follow the rendered View metadata.
- Stop using row `values` object keys to define the main list page structure.
- Leave unrelated `docs/superpowers/` untracked content untouched.

# Changes

- Added `viewDetailViewId()` in `frontend/src/viewWorkflow.ts` and used it in
  `frontend/src/App.vue` so default row open and fallback new-row
  initialization prefer `ListViewInfo.detailViewId` before falling back to the
  loaded list View id.
- Added `columnsFromRowItems()` so missing View columns fall back to legacy row
  `Items` metadata (`PrpId` / `PrpShowName`) instead of `row.values` DTO keys.
- Added Vitest coverage and App source guards for both boundaries.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- `cd frontend && npm test -- --run` passed: 3 files, 56 tests.
- `cd frontend && npm run build` passed.
- `python3 scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose up -d --build frontend` passed; Compose rebuilt frontend and
  backend images and restarted frontend/backend.
- `docker compose ps` showed backend and frontend up, MySQL/Redis healthy.
- `python3 scripts/runtime_doctor.py` passed compose, backend `/test`,
  `getlistview`, `querydata`, `querydatadetail`, `inputquery`, and
  `getmkqview` checks.

# Runtime Evidence

- `POST http://localhost:8081/api/v1/view/getlistview` with `{"ViewId":100}`
  returned `id=100`, `detailViewId=0`, and View columns
  `orderId,symbol,customer,state`.
- The current Docker seed still has `detailViewId=0`, so Vue falls back to the
  loaded list View id for that seed, but the code path now honors a configured
  `DetailViewId` when present.

# Risks

- A View with neither `tableColumn` nor row `Items` metadata will now render no
  main-list columns instead of deriving columns from arbitrary `values` keys.
  That is intentional for the View-first boundary but may expose incomplete
  backend metadata.

# Follow-ups

- Seed or migrate explicit `DetailViewId` values where legacy metadata expects
  a separate detail View.
