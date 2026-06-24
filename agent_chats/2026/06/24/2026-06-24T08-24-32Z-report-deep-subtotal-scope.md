# Report Deep Subtotal Scope Parity

## Prompt

- Continue the `/goal`: bring the Docker environment up, migrate against `../FoolFrame`, keep the frontend on Vue, and commit atomically.
- User confirmed permissions and asked to try again.

## Scope

- Compared `../FoolFrame/src/Server/SWRPT01-Soway.Report/Views/MatrixTableFactory.cs` with the Java `MatrixTableFactory`.
- Migrated the deeper legacy `GetCalScope` behavior where a static subtotal leaf includes previous non-static leaf ranges that share any real ancestor, not only the direct parent.
- Preserved existing split behavior where previous static subtotal leaves divide the calculated scope.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/MatrixTableFactory.java`
- `fool-report/src/test/java/org/fool/framework/report/ReportMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## TDD Evidence

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#createMatrixTableScopesDeepColumnStaticSubtotalsToSharedAncestors -DfailIfNoTests=false test`
  - Result: failed with `expected:<[0-1,]3-3> but was:<[]3-3>`.
- Green focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#createMatrixTableScopesDeepColumnStaticSubtotalsToSharedAncestors -DfailIfNoTests=false test`
  - Result: passed.
- Report regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest -DfailIfNoTests=false test`
  - First result after implementation: failed because the Java synthetic root was included as a shared ancestor.
  - Final result after excluding the synthetic root from ancestor matching: `ReportMigrationTest`: 13 tests, 0 failures, 0 errors.

## Runtime / Skips

- Docker Compose was already running from the previous runtime slice.
- Runtime smoke:
  - `docker compose ps`
  - Result: backend, frontend, MySQL, and Redis containers were running; MySQL and Redis were healthy.
  - `curl -sf http://localhost:8080/test && curl -sfI http://localhost:8081/`
  - Result: backend returned seeded order JSON; frontend returned `HTTP/1.1 200 OK`.
- Repository harness:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
- Whitespace check:
  - `git diff --check`
  - Result: passed.
- No frontend or runtime endpoint changed in this slice, so Vue build and Docker rebuild were deferred to broader validation.

## Follow-ups

- Continue `SWRPT01-Soway.Report` table source adapter and query/export integration.
- Continue database schema parity and full self-contained backend test work.
