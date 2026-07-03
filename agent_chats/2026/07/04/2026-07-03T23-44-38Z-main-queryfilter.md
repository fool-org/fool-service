# Main View QueryFilter

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep the rendered page driven
by View metadata before querying data.

## Scope

- Align the main Vue View workflow list filter with FoolFrame's
  `querydata.QueryFilter` payload.
- Avoid adding a custom query-builder UI.

## Changes

- Reused the existing `legacyQueryFilter` state for the main View workflow.
- Changed `queryCurrentViewData()` to send `queryFilter` instead of `keyword`.
- Renamed the main toolbar field from `Keyword` to `QueryFilter`.
- Added a focused frontend source test guarding the main View query payload.

## Validation

- Passed: `cd frontend && npm test`.
- Passed: `cd frontend && npm run build`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- Rebuilt and restarted the Compose frontend:
  `docker compose up -d --no-deps --build frontend`.
- Passed: `python3 scripts/runtime_doctor.py`.

## Risks

- QueryFilter remains a raw legacy expression. A full condition builder is
  still report/query-workflow backlog, not part of this small parity slice.

## Follow-ups

- Continue broader View/query/report parity migration.
