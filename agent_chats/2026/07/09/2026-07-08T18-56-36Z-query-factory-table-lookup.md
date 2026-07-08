# QueryFactory Table Lookup Parity

## Prompt

Continue the FoolFrame migration with the view-first rule: render the View
page first, query data from that View, and do not bind generic query behavior
to concrete business DTO/display labels.

## Scope

- Compared FoolFrame `QueryFac.GetTable` and confirmed legacy lookup uses
  table DBName only with trim/case normalization.
- Removed the Java-only `QueryFactory.getTable` fallback that matched table
  ShowName and returned null on missing tables.
- Kept the existing empty `getColumns` default unchanged.
- Updated task and migration parity notes for the corrected query boundary.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryFactory.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryFactoryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest test`
  - Failed because ShowName lookup still returned a table instead of throwing.
- GREEN: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest test`
  - `QueryFactoryTest` passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am test`
  - `fool-common`, `fool-dao`, and `fool-query` reactor tests passed.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `docker compose up -d --build backend`
  - Backend image rebuilt and `fool-service-backend-1` restarted.
- GREEN: `docker compose ps`
  - Backend and frontend running; MySQL and Redis healthy.
- GREEN: `curl -sS -o /tmp/fool-service-backend-smoke.txt -w "%{http_code} %{size_download}\n" http://localhost:8080/test`
  - `200 465`.
- GREEN: `curl -sS -o /tmp/fool-service-frontend-smoke.html -w "%{http_code} %{size_download}\n" http://localhost:8081/`
  - `200 399`.

## Risks

- Code that previously passed a rendered table label into `getTable` now fails,
  matching FoolFrame. Callers must resolve View/model metadata before building
  data queries.
