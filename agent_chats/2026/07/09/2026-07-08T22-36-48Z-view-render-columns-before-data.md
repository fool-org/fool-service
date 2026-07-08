# View Render Columns Before Data

## Prompt

Keep the migrated Vue workflow View-first: load the rendered View page before
querying data, and avoid binding page behavior to concrete business DTO rows.

## Scope

- Frontend list workflow only.
- Do not change backend query semantics or Docker seed metadata in this slice.

## Changes

- Added a regression test proving `querydata` is not called when `getlistview`
  returns no renderable columns.
- Added a `canRenderLoadedView` guard before the shared data query path.
- Cleared stale list data whenever a new View definition is loaded.
- Updated migration parity docs and task state.

## Validation

- RED: `cd frontend && npm test -- --run useViewDataWorkflow.test.ts --testNamePattern "does not query data when the loaded View cannot render columns"` failed because the workflow still called `querydata`.
- GREEN: `cd frontend && npm test -- --run useViewDataWorkflow.test.ts --testNamePattern "does not query data when the loaded View cannot render columns"`.
- PASS: `cd frontend && npm test` ran 111 tests.
- PASS: `cd frontend && npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend`.
- PASS: `docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-DYIO8rPU.js`.
- `docker compose ps --format json` showed backend, rebuilt frontend, MySQL,
  and Redis running; MySQL and Redis were healthy.

## Risks

- A View with no list columns now intentionally shows no data instead of
  querying rows that cannot be rendered safely. That matches the View-first
  contract, but any intentionally columnless legacy View would need an explicit
  renderer before it can show data.

## Follow-ups

- Continue replacing Docker smoke configuration with real migrated View
  metadata where available instead of expanding the seeded order DTO example.
