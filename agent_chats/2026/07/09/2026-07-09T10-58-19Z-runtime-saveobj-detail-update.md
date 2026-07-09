# 2026-07-09 Runtime SaveObj Detail Update

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep writes View-first and reuse existing runtime doctor helpers.

## Scope

- Extended `scripts/runtime_doctor.py` to prove legacy `saveobj` through the
  Vue proxy.
- The check creates a temporary object with `savenewobj`, updates it through
  `saveobj` using loaded detail View fields, verifies the changed field through
  `querydatadetail`, and cleans up the fixed smoke id.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-58-19Z-runtime-saveobj-detail-update.md`

## Red Test

- `python scripts/runtime_doctor_test.py -k saveobj_updates` failed first
  because `api_checks` did not expose `data:saveobj`.

## Validation

- `python scripts/runtime_doctor_test.py -k saveobj_updates` passed.
- `python scripts/runtime_doctor_test.py` passed with 28 tests.
- `python scripts/runtime_doctor.py` passed, including `data:saveobj`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -N -B -e "SELECT COUNT(*) FROM market_order WHERE order_id IN (989902,989903); SELECT COUNT(*) FROM market_order_item WHERE order_id IN (989902,989903);"` returned `0` and `0`.

## Skipped Checks

- No Maven or frontend build was run because this slice only changes Python
  runtime-doctor coverage and migration evidence.

## Risks

- The runtime check depends on the Docker smoke detail View exposing at least
  one writable string-like field for read-back verification.
