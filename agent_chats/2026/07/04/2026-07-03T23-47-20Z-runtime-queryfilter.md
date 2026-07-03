# Runtime QueryFilter Check

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep Docker/runtime evidence
current.

## Scope

- Prove the Docker frontend proxy `querydata` route handles a legacy
  `QueryFilter`.
- Keep the check tied to View-rendered row `Items`, not the compatibility
  `values` map.

## Changes

- Added a runtime doctor check that posts `QueryFilter: order_state="0"` to
  `/api/v1/data/querydata`.
- The check verifies every returned row has legacy `Items` metadata for
  `state` with `objId=0`.
- Updated `tasks.md` and the migration parity ledger.

## Validation

- Passed: `python3 scripts/runtime_doctor.py`.

## Runtime Evidence

- Docker runtime check passed through the frontend proxy at
  `http://localhost:8081/api/v1/data/querydata`.

## Risks

- The check uses Docker seed data for ViewId `100`; broader schema parity still
  belongs to the migration backlog.

## Follow-ups

- Keep adding narrow runtime proofs when a migrated Vue path changes.
