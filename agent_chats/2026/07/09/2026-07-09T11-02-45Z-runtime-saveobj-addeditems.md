# 2026-07-09 Runtime SaveObj AddedItems

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep collection writes View-first and reuse existing runtime doctor helpers.

## Scope

- Extended `scripts/runtime_doctor.py` to prove legacy
  `saveobj.Itemproperties[].AddedItems[]` through the Vue proxy.
- The check creates a temporary parent row, adds a child row using loaded
  detail child metadata, verifies the child value through `querydatadetail`,
  and cleans up the fixed smoke id.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-02-45Z-runtime-saveobj-addeditems.md`

## Red Test

- `python scripts/runtime_doctor_test.py -k addeditems` failed first because
  `api_checks` did not expose `data:saveobj-addeditems`.

## Validation

- `python scripts/runtime_doctor_test.py -k addeditems` passed.
- `python scripts/runtime_doctor_test.py` passed with 29 tests.
- `python scripts/runtime_doctor.py` passed, including
  `data:saveobj-addeditems`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -N -B -e "SELECT COUNT(*) FROM market_order WHERE order_id IN (989902,989903,989904); SELECT COUNT(*) FROM market_order_item WHERE order_id IN (989902,989903,989904) OR item_id IN (989902,989903,989904);"` returned `0` and `0`.

## Skipped Checks

- No Maven or frontend build was run because this slice only changes Python
  runtime-doctor coverage and migration evidence.

## Risks

- The runtime check depends on the Docker smoke detail View exposing one child
  collection with an id-like column and a string-like display column.
