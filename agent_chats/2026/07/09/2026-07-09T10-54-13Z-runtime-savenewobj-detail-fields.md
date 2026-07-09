# 2026-07-09 Runtime SaveNewObj Detail Fields

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep saves View-first and avoid concrete business DTO binding.
- Reuse existing runtime doctor helpers and keep the slice atomic.

## Scope

- Extended `scripts/runtime_doctor.py` to prove legacy `savenewobj` through the
  Vue proxy after `initnew` loads the detail View fields.
- Generated `SaveObj.Propertyies` from loaded detail metadata, skipping readonly
  fields and BusinessObject fields so missing foreign keys still exercise the
  dynamic default path.
- Added cleanup for the fixed smoke order id before and after the runtime
  check.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-54-13Z-runtime-savenewobj-detail-fields.md`

## Red Test

- `python scripts/runtime_doctor_test.py -k savenewobj` failed first because
  `api_checks` did not expose `data:savenewobj`.

## Validation

- `python scripts/runtime_doctor_test.py -k savenewobj` passed.
- `python scripts/runtime_doctor_test.py` passed with 27 tests.
- `python scripts/runtime_doctor.py` passed, including `data:savenewobj`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -N -B -e "SELECT COUNT(*) FROM market_order WHERE order_id=989902; SELECT COUNT(*) FROM market_order_item WHERE order_id=989902;"` returned `0` and `0`.

## Skipped Checks

- No Maven or frontend build was run because this slice only changes Python
  runtime-doctor coverage and migration evidence.

## Risks

- The cleanup helper is tied to the Docker smoke `market_order` fixture id.
