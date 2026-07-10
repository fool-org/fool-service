# Runtime Seed Row Guard

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep file size controlled, maximize reuse, and avoid DTO-bound shortcuts.

## Scope

- Added a runtime doctor check for Docker `car_wash` seed rows that the Vue and
  legacy protocol smoke workflow depends on.
- Replayed the existing idempotent `docker/mysql/init/010-query.sql` against
  the running MySQL volume after the new check exposed missing query catalog
  rows.
- Fixed the seed-row probe to use `--default-character-set=utf8mb4`, so Chinese
  query catalog labels are verified correctly by the MySQL client.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/10/2026-07-10T02-15-56Z-runtime-seed-row-guard.md`

## Runtime Evidence

- Before replaying `010-query.sql`, the new runtime doctor check failed:
  `mysql:runtime-seed-data`.
- The running DB initially reported `compare 0` and `selected 0` for the query
  catalog probes.
- Replayed:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/010-query.sql`
- After replay with an UTF-8 client, the runtime seed probe reported:
  `app 1`, `admin 1`, `orderlist 1`, `sudoku 1`, `compare 1`, `selected 1`,
  `event 1`, `order 1`.

## Validation

- `python scripts/runtime_doctor_test.py` passed: 45 tests.
- `python scripts/runtime_doctor.py` passed, including
  `mysql:runtime-seed-data`.

## Skipped

- Full Maven and frontend builds were not rerun; this slice only changes the
  Python runtime doctor, task state, docs, and the live Docker seed data.

## Risks And Follow-ups

- Existing developer MySQL volumes created before new init SQL slices may need
  the same idempotent replay or a volume reset.
