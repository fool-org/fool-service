# Querydata Pascal View Data

## Prompt

Continue the FoolFrame migration, keep Docker/Vue evidence current, and avoid
binding the rendered page to concrete business DTOs. The user specifically
called out that the flow should render the View first, then query data by that
View.

## Scope

- Keep the list workflow View-first: `getlistview(ViewId)` before
  `querydata(ViewId)`.
- Add legacy FoolFrame Pascal response aliases for generic `querydata` result
  and row DTOs.
- Make Vue list helpers consume both camel and Pascal response shapes without
  reading business DTO fields from `values` for rendering decisions.
- Keep `App.vue` under the repository line budget by moving static shell
  helpers out of the component.

## Changes

- Added `TotalItem`, `TotalPage`, `PageIndex`, `Cols`, `FreshTime`,
  `AutoFreshTime`, and `Data` aliases on `ListViewResult`.
- Added `Id`, `RowIndex`, `Items`, and `RowFmt` aliases on `ListDataItem`.
- Added Vue `listRows`, `detailItemValues`, and alias-aware field helpers so
  list rows, totals, fallback columns, and child item drafts read generic
  View/data protocol fields.
- Added `viewShell.ts` for static navigation/status helpers and a small enum
  option accessor, keeping `App.vue` at 1998 lines.
- Updated migration parity docs and `tasks.md`.

## Validation

- Passed: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewDataAdapterTest test`.
- Passed: `cd frontend && npm test -- --run`.
- Passed: `cd frontend && npm run build`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- Rebuilt Docker backend/frontend with `docker compose up -d --build backend frontend`.
- Force-recreated backend after image rebuild with
  `docker compose up -d --no-deps --force-recreate backend`.
- The first doctor run immediately after backend recreate failed during startup
  with `Connection reset by peer` / `502 Bad Gateway`; reran after 8 seconds.
- Passed: `python3 scripts/runtime_doctor.py`.
- Passed: frontend-proxy `querydata` alias check at
  `http://localhost:8081/api/v1/data/querydata`, confirming result aliases
  `AutoFreshTime`, `Cols`, `Data`, `FreshTime`, `PageIndex`, `TotalItem`,
  `TotalPage`; row aliases `Id`, `Items`, `RowFmt`, `RowIndex`; and first item
  aliases `EditType`, `FmtValue`, `ObjId`, `PrpId`, `PrpModelId`,
  `PrpShowName`, `PrpType`, `ReadOnly`.
- Final `docker compose ps`: backend, frontend, MySQL, and Redis are up;
  MySQL/Redis are healthy.

## Risks

- Runtime proof still uses Docker seed ViewId `100`; broader View schema drift
  remains a migration backlog item.
- Vue direct rendering was validated by unit/build checks and runtime proxy/API
  checks, not by a browser screenshot in this slice.

## Follow-ups

- Continue reducing the API tools surface in `App.vue`; the main workflow is
  View-driven, but the component is still too large for long-term maintenance.
