# 2026-07-09 Runtime SaveObj Items Delete

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Prove collection writes through the existing View-first runtime path.

## Scope

- Extended `scripts/runtime_doctor.py` to prove legacy
  `saveobj.Itemproperties[].Items[]` update and `DelteItems[]` delete through
  the Vue proxy.
- The check creates a temporary parent and child, updates the child value,
  verifies it through `querydatadetail`, deletes the child through
  `DelteItems`, verifies it is absent, and cleans up the fixed smoke id.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-06-18Z-runtime-saveobj-items-delete.md`

## Red Test

- `python scripts/runtime_doctor_test.py -k items_and_delteitems` failed first
  because `api_checks` did not expose `data:saveobj-items-delete`.

## Validation

- `python scripts/runtime_doctor_test.py -k items_and_delteitems` passed.
- `python scripts/runtime_doctor_test.py` passed with 30 tests.
- `python scripts/runtime_doctor.py` passed, including
  `data:saveobj-items-delete`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -N -B -e "SELECT COUNT(*) FROM market_order WHERE order_id BETWEEN 989902 AND 989905; SELECT COUNT(*) FROM market_order_item WHERE order_id BETWEEN 989902 AND 989905 OR item_id BETWEEN 989902 AND 989905;"` returned `0` and `0`.

## Skipped Checks

- No Maven or frontend build was run because this slice only changes Python
  runtime-doctor coverage and migration evidence.

## Risks

- The runtime check proves the Docker One2Many child collection path; richer
  collection-state parity remains tracked in the migration document.
