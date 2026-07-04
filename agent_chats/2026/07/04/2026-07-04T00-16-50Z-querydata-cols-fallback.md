# Querydata Cols Fallback

## Prompt

Continue the FoolFrame migration with the Vue frontend bound to rendered View
metadata and legacy data protocol fields, not concrete business DTO shapes.

## Scope

- Compare FoolFrame list header rendering against the current Vue table-column
  fallback path.
- Keep the fix frontend-only and reuse existing view workflow helpers.

## Changes

- Added `columnsFromListResult` to convert camel-case `cols` and Pascal
  `Cols` from `querydata` into generic table column metadata.
- Updated the main View table fallback order:
  `getlistview.tableColumn` -> `querydata.Cols` -> row `Items`.
- Updated select-existing child candidate tables to use `querydata.Cols` when
  the candidate View definition has no `tableColumn`.
- Added focused frontend tests for camel/Pascal `Cols` and App wiring.
- Updated `tasks.md` and migration parity notes.

## Validation

- Passed: `cd frontend && npm test -- --run` (`63 passed`).
- Passed: `cd frontend && npm run build`.
- Passed: `docker compose build frontend`.
- Passed: `docker compose up -d --no-deps frontend`.
- Passed: `python3 scripts/check_repo_harness.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` returned the rebuilt Vue app HTML with
  `/assets/index-BYM9D4sd.js`.
- `curl -fsS http://localhost:8081/api/v1/data/querydata -H 'Content-Type:
  application/json' -d '{"ViewId":100,"PageSize":2,"PageIndex":1}'` returned
  both `cols` and legacy `Cols` with `["Order ID","Symbol","Customer","State"]`
  plus `items` / `Data` row aliases.
- `docker compose ps` shows backend, frontend, MySQL, and Redis up; MySQL/Redis
  are healthy.

## Risks

- This slice validates fallback source selection through tests and runtime API
  proof. It does not add browser screenshot proof for a no-`tableColumn` View.
