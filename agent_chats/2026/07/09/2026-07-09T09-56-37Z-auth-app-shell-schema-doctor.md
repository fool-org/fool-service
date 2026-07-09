# 2026-07-09 Auth App Shell Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep the Vue flow tied to legacy auth/app shell data before loading View
  metadata.

## Scope

- Compared the Docker app/auth SQL, migrated Java auth/app shell paths, and
  AppManage/Auth mappings.
- Extended the runtime doctor core schema guard to include modern `auth_user`,
  legacy `SW_AUTH_USER`, `SW_APPLICATION`, `SW_STOREDB`,
  `SW_APPLICATION_SW_STOREDB`, and `SW_APP_AUTH_*` menu/user/role relation
  columns used by `initapp`, `loginv2`, `getmain`, and `getsubmenu`.
- Updated the doctor success text so the evidence names auth/app shell schema,
  not just model/view/operation schema.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-56-37Z-auth-app-shell-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the expected
  auth/app shell columns were not a subset of
  `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This guards auth/app shell schema drift only. It does not add new auth or
  frontend behavior.
