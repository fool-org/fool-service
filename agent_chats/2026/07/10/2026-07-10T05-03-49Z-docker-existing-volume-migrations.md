# Docker Existing-Volume Migrations

## Prompt
- Continue the Docker/FoolFrame/Vue migration, maximizing reuse and keeping
  changes atomic.

## Scope
- Audited Compose and all nine `docker/mysql/init/*.sql` files for the gap
  between fresh MySQL initialization and an existing named volume.
- Replayed the complete SQL catalog successfully against the running 45-hour
  `fool_mysql_data` volume before changing startup wiring.
- Added a one-shot `db-migrate` service that mounts and sequentially reuses the
  existing SQL catalog after MySQL is healthy.
- Made backend startup depend on successful migration completion.
- Extended runtime doctor to require the migration container at `Exited (0)`
  and to inspect stopped one-shot services through `docker compose ps -a`.
- Added repository-harness checks for migration mount, ordered replay, and
  backend completion dependency.

## Changed Files
- `docker-compose.yml`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `AGENTS.md`
- `docs/validation.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-03-49Z-docker-existing-volume-migrations.md`

## Validation
- Full manual replay of `docker/mysql/init/*.sql` against the existing volume
  passed.
- `docker compose config --format json` preserved the one shell command and
  backend `service_completed_successfully` dependency.
- `docker compose run --rm db-migrate` applied all nine files and exited `0`.
- `docker compose up -d --build` built the frontend/backend and ran migration
  before backend startup.
- A second `docker compose up -d` appended a second complete nine-file replay,
  proving the migration runs on ordinary repeated startup.
- `python scripts/check_repo_harness_test.py` passed: 10 tests.
- `cd scripts && python -m unittest runtime_doctor_test.py` passed: 48 tests.
- `python scripts/runtime_doctor.py` passed, including
  `compose:db-migrate: Exited (0)`.

## Runtime Evidence
- `docker compose ps -a` showed MySQL/Redis healthy, backend/frontend running,
  and `fool-service-db-migrate-1` at `Exited (0)`.
- `docker compose logs db-migrate` contained two ordered runs from
  `001-market-order.sql` through `010-query.sql`.
- Backend creation/start occurred only after the migration service exited.

## Risks
- The migration source remains the current idempotent SQL catalog rather than
  a version-history framework; new schema changes must remain replay-safe.

## Follow-ups
- Add new schema only when a concrete remaining FoolFrame model/runtime path
  requires it, keeping runtime-doctor column/seed checks synchronized.
