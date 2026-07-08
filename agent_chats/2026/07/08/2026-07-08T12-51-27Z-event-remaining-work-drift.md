# Event remaining-work drift

## Prompt

Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
reuse, while staying focused on the goal instead of business DTO shortcuts.

## Scope

- Audited the next migration gap candidates after the inputquery selected View
  filter slice.
- Confirmed `saverpt`, `SelectedQueryTable`, and several query/event stubs are
  intentionally aligned with FoolFrame no-op or `NotImplementedException`
  surfaces.
- Corrected stale `SCPB09-SOWAY.EVENT` remaining-work text: the module map and
  event tests already cover the object-query cases that were still listed as
  open.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v
  "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl
  fool-event -am -DfailIfNoTests=false -Dtest=EventMigrationTest test`:
  passed, 44 tests.

## Risks

- This is a documentation/task-state correction only. It does not claim full
  event-module parity beyond the currently covered object-query, message,
  scheduler, and admin-notification paths.
