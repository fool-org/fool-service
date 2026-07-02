# Legacy Column Extra Metadata

## Prompt

- Continue migration against `../FoolFrame`, keep Docker running, keep Vue frontend,
  and make timely atomic commits.

## Scope

- Added legacy `ColumnAttribute` extra metadata to Java `@Column`:
  `KeyCanBeNullOrEmpty`, `SqlType`, and `IsIdentify`.
- Preserved non-persisted `KeysCanBeDefault` on runtime model properties with a
  transient Java field.
- Mapped reflective `identify` column metadata to `PropertyType.IdentifyId`.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/annotation/Column.java`
- `fool-common/src/test/java/org/fool/framework/common/annotation/ColumnTest.java`
- `fool-model/src/main/java/org/fool/framework/model/model/Property.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/ReflectiveAppModuleSource.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common,fool-app-manage -am test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Full root `mvn test`; the scoped Maven command ran the changed modules plus
  required upstream dependencies.

## Risks And Follow-Ups

- `sqlType` is retained as annotation metadata only; no Java SQL-type execution
  path exists yet.
- Broader AppInstall transaction and DBMaps runtime parity remain tracked in
  `docs/migration/foolframe-parity.md`.
