# Querydata FreshTime Status

## Prompt

Continue the FoolFrame migration with Docker running and Vue as the frontend.
Keep the rendered View/data flow generic instead of binding to business DTOs.

## Scope

- Compare FoolFrame list update-time display with the current Vue View
  workflow.
- Consume the existing `querydata.FreshTime` response field with the smallest
  frontend change.

## Changes

- Added `listFreshTime` to read camel-case `freshTime` and FoolFrame Pascal
  `FreshTime`.
- Displayed the loaded result timestamp beside the main View workflow paging
  status.
- Added focused frontend tests for helper aliases and App wiring.
- Updated migration parity docs and `tasks.md`.

## Validation

- Passed: `cd frontend && npm test -- --run` (`63 passed`).
- Passed: `cd frontend && npm run build`.
- Passed: `docker compose build frontend`.
- Passed: `docker compose up -d --no-deps frontend`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` returned the rebuilt Vue app HTML with
  `/assets/index-Cc5veqZ9.js`.
- `curl -fsS http://localhost:8081/api/v1/data/querydata -H 'Content-Type:
  application/json' -d '{"ViewId":100,"PageSize":1,"PageIndex":1}'` returned
  `freshTime` and legacy `FreshTime` in the data payload.
- `docker compose ps` shows backend, frontend, MySQL, and Redis up; MySQL/Redis
  are healthy.

## Risks

- Vue displays the backend timestamp string directly. No custom date parser was
  added because the migrated backend emits ISO timestamps.
