# AppInstall Routed Transaction Boundaries

## Prompt

Continue the Docker/Vue/FoolFrame migration with reusable AppInstall parity
slices and atomic commits.

## Scope

- Added `DaoService.inTransaction(...)` as the shared transaction boundary for
  multi-step DAO work.
- Wired DriverManager-created legacy connection DAOs with Spring
  `DataSourceTransactionManager` / `TransactionTemplate` over the cached
  `SingleConnectionDataSource`.
- Wrapped routed AppInstall module-source metadata installs, schema DDL, and
  default View generation in the target DAO transaction boundary.
- Updated migration parity and task state.

## Changed Files

- `fool-dao/src/main/java/org/fool/framework/dao/DaoService.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DriverManagerAppDaoServiceFactory.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/DaoAppInstallGatewayTransactionTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: focused Maven test failed to compile because
  `DaoService.inTransaction(...)` did not exist.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am -Dtest=DaoAppInstallGatewayTransactionTest test`
  passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-app-manage -am test`
  exited 0. Existing `DataQueryServiceTest` logged `没有查到视图` as part of its
  caught test path, but the Maven run passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Artifacts

None. This is AppInstall transaction-boundary behavior.

## Risks

- This is a per-connection transaction boundary. It does not add a distributed
  transaction across separate sys/work databases.

## Follow-ups

- Continue deeper runtime routed-connection transaction behavior where model
  data mutations span multiple services.
