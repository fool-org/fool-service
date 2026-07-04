# Vue AutoFreshTime

## Prompt

Continue the FoolFrame-to-Vue migration, keeping the frontend View/data driven
instead of binding rows to business DTOs.

## Scope

- Compare FoolFrame list-page refresh behavior against the current Vue View
  workflow.
- Add the smallest frontend-only parity fix for legacy `AutoFreshTime`.

## Changes

- Added `listAutoFreshTime` to read camel-case and FoolFrame Pascal
  `AutoFreshTime` response fields.
- Scheduled a native browser interval after the main `querydata` workflow call
  succeeds.
- The interval only refreshes while the View workflow is active, skips when
  another action is pending, resets to page 1 like FoolFrame's `query()`, and
  clears on component unmount.
- Added frontend tests for the helper and source-level wiring.
- Updated migration parity docs and `tasks.md`.

## Validation

- Passed: `cd frontend && npm test -- --run` (`61 passed`).
- Passed: `cd frontend && npm run build`.
- Passed: `docker compose build frontend`.
- Passed: `docker compose up -d --no-deps frontend`.
- Passed: `python3 scripts/check_repo_harness.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` returned the rebuilt Vue app HTML with
  `/assets/index-COcj315a.js`.
- `curl -fsS http://localhost:8080/test` returned seeded order rows.
- `docker compose ps` shows backend, frontend, MySQL, and Redis up; MySQL/Redis
  are healthy.

## Risks

- This slice proves the timer wiring by unit/build/source checks and container
  smoke. It does not wait for a live refresh tick in a browser session.
