# ViewId Alias Workflow

## Prompt

User flagged that the migration should check/render the View page first, then
query data from that View, and that binding to concrete business DTOs is wrong.

## Scope

- Keep the Vue list workflow View-first.
- Normalize list View identity at the protocol/helper boundary.
- Avoid adding page-level business field or DTO special cases.

## Changes

- Added `ViewId` as a legacy JSON alias on backend `ListViewInfo`, alongside
  the existing `ID` alias.
- Added frontend `ListViewInfo` type support for `viewId`, `ViewId`, and
  `ViewID`.
- Updated shared Vue `viewId()` to prefer explicit ViewId aliases before
  falling back to `id` / `ID`.
- Added a workflow test proving `querydata` uses the View id returned by
  rendered `getlistview` metadata.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- useViewDataWorkflow.test.ts` failed because only
  `/api/v1/view/getlistview` was called when the response used `ViewId`.
- RED: Docker Maven focused test failed because serialized `ListViewInfo`
  lacked `ViewId`.
- GREEN: `npm test -- useViewDataWorkflow.test.ts`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoSerializesLegacyPascalMetadata test`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`.
- PASS: `git diff --check`.
- PASS: `docker compose build backend frontend && docker compose up -d backend frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8080/test` returned the backend smoke payload.
- Backend `POST /api/v1/view/getlistview` with `{"ViewId":100}` returned
  `"ViewId":100`.
- Frontend proxy `POST /api/v1/view/getlistview` with `{"ViewId":100}`
  returned `"ViewId":100`.
- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-2YOnyL9L.js`.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running.

## Risks

- Local host Maven still uses a Java runtime that cannot compile target 17, so
  Java checks for this repo should use the Maven 17 Docker command until the
  host JDK is corrected.

## Follow-ups

- Continue remaining migration work from `docs/migration/foolframe-parity.md`
  without reintroducing ViewName or business DTO shortcuts.
