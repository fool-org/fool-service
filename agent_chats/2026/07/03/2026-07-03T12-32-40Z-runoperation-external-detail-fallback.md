# runoperation External Model Detail Fallback

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Matched the FoolFrame `CommandsType.ExuteOutModelMethod` fallback where the
  target model has no operation matching `SW_SYS_COMMAND_EXP`.
- Added test evidence that the migrated path loads the target object by
  `SW_SYS_COMMAND_ARGID`, maps `SW_SYS_COMMAND_ARGEXP` back to the source
  object, and does not persist the target object.
- No runtime code changed; the previous external-model implementation already
  had the behavior and this slice locks it down.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T12-32-40Z-runoperation-external-detail-fallback.md`

## Validation

- Focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test > /host-tmp/fool-external-detail-focused.log 2>&1; status=$?; tail -n 60 /host-tmp/fool-external-detail-focused.log; exit $status'`
  - Tests run: 19, failures: 0, errors: 0, skipped: 0.
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-view -am -DfailIfNoTests=false test > /host-tmp/fool-external-detail-module.log 2>&1; status=$?; tail -n 80 /host-tmp/fool-external-detail-module.log; exit $status'`
  - Tests run: 101, failures: 0, errors: 0, skipped: 0.
- `git diff --check`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.

## Runtime Evidence

- No backend rebuild was needed because this slice changes only tests and
  migration evidence.
- `docker compose ps`
  - Backend, frontend, MySQL, and Redis were running; MySQL and Redis were
    healthy.

## Skipped Checks

- Frontend unit/build checks were not rerun because no Vue source changed.
- Docker runtime smoke was not rerun because runtime code did not change.

## Remaining Risk

- Richer nested external-model edge cases, WCF/JSON operation types, operation
  triggers, and routed-connection transaction behavior remain future work.
