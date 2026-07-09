# 2026-07-09 Event Message Schema Doctor

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep focus on the migration target and avoid view/data DTO shortcuts.

## Scope

- Compared Docker event SQL and migrated event repositories for the actual
  `SW_EVT_DEF`, `SW_EVT_EVENT`, and `SW_SYS_MSG` columns used by scheduler,
  event de-duplication, message persistence, and `getmsg` push-state updates.
- Extended `runtime_doctor.py` to include those event/message columns in the
  legacy core schema guard.
- No SQL, Java, or Vue behavior changed; this is runtime schema drift coverage
  only.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T10-06-14Z-event-message-schema-doctor.md`

## Red Test

- `python scripts/runtime_doctor_test.py` failed first because the expected
  event/message columns were not a subset of
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

- This guards schema drift only. It does not add new event runtime behavior.
