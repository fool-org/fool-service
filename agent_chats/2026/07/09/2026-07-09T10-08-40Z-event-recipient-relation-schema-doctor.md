# 2026-07-09 Event Recipient Relation Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep the migration focused on actual FoolFrame parity surfaces and avoid
  speculative behavior.

## Scope

- Compared Docker event SQL and `JdbcEventDefinitionRecipientRelationLoader`
  for the event definition relation tables read during notification expansion.
- Extended `runtime_doctor.py` to guard
  `SW_APP_AUTH_USER_SW_EVT_DEF`, `SW_APP_AUTH_ROLE_SW_EVT_DEF`,
  `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF`, and
  `SW_APP_AUTH_COMPANY_SW_EVT_DEF`.
- No SQL, Java, or Vue behavior changed; this is runtime schema drift coverage
  only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-08-40Z-event-recipient-relation-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the event
  recipient relation columns were not a subset of
  `runtime_doctor.LEGACY_CORE_SCHEMA_COLUMNS`.

## Validation

- `python scripts/runtime_doctor_test.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, legacy core schema, auth shell, View/data, report,
  message, and logout checks.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Skipped Checks

- No Maven or frontend test run: this slice only extends runtime schema guard
  coverage and migration evidence.

## Risks

- This guards schema drift only. It does not add new event recipient behavior.
