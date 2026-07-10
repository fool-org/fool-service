# Root Entry Route Guard

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Compare the legacy Web routes before adding more work.

## Scope

- Compared `../FoolFrame/src/Web/app.js` route entries with current runtime
  doctor coverage.
- Added `GET /` to the Docker frontend route smoke checks. The old Web root
  entry renders the app shell before `/main` and View deep links, so the Vue
  container should prove the same entry point returns the built HTML bundle.

## Changed Files

- `scripts/runtime_doctor.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/10/2026-07-10T02-19-08Z-root-entry-route-guard.md`

## Validation

- `python scripts/runtime_doctor.py` passed, including `frontend:root-path`.

## Skipped

- Static legacy informational pages `/about` and `/contact` were not added;
  they do not participate in the View-first migration workflow.
