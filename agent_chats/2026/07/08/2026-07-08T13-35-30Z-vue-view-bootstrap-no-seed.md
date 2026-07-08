# Vue View Bootstrap Without Seed Defaults

## Prompt

Keep the migration focused on View-first rendering: load the View page first,
then query data from that View context, without binding the Vue workflow to a
specific business DTO or seeded `ViewId=100`.

## Scope

- Removed hard-coded `100` View defaults from the Vue workflow state.
- Changed `currentViewId` to come only from the loaded `getlistview` response.
- Made the API-tool data query path stop before `/api/v1/data/querydata` when
  no View has been loaded.
- Added a frontend regression check for the no-seed bootstrap contract.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `cd frontend && npm test -- src/payload.test.ts`
  - Failed on `does not bootstrap View/data rendering from the seeded business
    View` while `App.vue` still contained `ref(100)` and `currentViewId` fell
    back to `legacyListViewId`.
- PASS: `cd frontend && npm test -- src/payload.test.ts`
- PASS: `cd frontend && npm test`
- PASS: `cd frontend && npm run build`
- PASS: `python scripts/check_repo_harness.py`
- PASS: `git diff --check`
- PASS: `docker compose up -d --build frontend`
  - Rebuilt and recreated the frontend service; Compose also rebuilt backend
    because of the service dependency graph.
- PASS: `python scripts/runtime_doctor.py`
- PASS: `curl -fsS http://localhost:8081/ | head -20`
- PASS: `wc -l frontend/src/App.vue` -> `1999`
- PASS: `rg -n 'ref\(100\)|viewId\(viewResponse\.value\?\.data, legacyListViewId\.value\)' frontend/src/App.vue`
  - No matches.

## Risks

- The standalone API tools now require an entered or app-loaded ViewId before
  querying data. That is intentional for the migration boundary, but empty
  unauthenticated first load stays quiet until login/app metadata provides a
  default View.

## Follow-ups

- Continue retiring API-tool-only shortcuts when they can bypass rendered View
  metadata.
