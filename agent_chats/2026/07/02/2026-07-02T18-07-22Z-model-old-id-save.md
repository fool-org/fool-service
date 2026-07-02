# Model Old-Id Dynamic Save Parity

## Prompt

- Continue the FoolFrame migration, keep Docker running, keep the frontend on Vue, and make timely atomic commits.
- User asked for the current overall migration percentage during this slice.

## Scope

- Compared legacy `SCPB05-Soway.Model` save semantics for dynamic rows where the id property has changed after load.
- Ported the legacy old-id lookup behavior for simple dynamic saves.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/DbMysqlDynamic.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataUsesLegacyOldIdWhenIdPropertyChanged test`
  - Failed before implementation with `expected:<true> but was:<false>`.
- GREEN: same focused command after implementation.
  - Passed: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed: `Tests run: 57, Failures: 0, Errors: 0, Skipped: 0`.
- Harness: `python scripts/check_repo_harness.py`
  - Passed: `Repository harness validation passed.`
- Whitespace: `git diff --check`
  - Passed with no output.
- Runtime: `curl -fsS http://localhost:8080/test`
  - Returned seeded order JSON from the running Docker backend.

## Runtime Evidence

- `docker compose ps` showed backend/frontend running and MySQL/Redis healthy before the slice evidence update.
- Backend `/test` responded through the Docker-published port `8080`.

## Risks And Follow-Ups

- This covers simple dynamic save lookup by old id. Richer collection state parity, operation-trigger side effects, and routed-connection transaction behavior remain open.
- Existing Maven warnings remain: duplicate `spring-jdbc` dependency declaration in `fool-dao`, plus pre-existing deprecation/unchecked warnings.

## Commit

- Local commit subject: `feat(model): save dynamic rows by old id`
