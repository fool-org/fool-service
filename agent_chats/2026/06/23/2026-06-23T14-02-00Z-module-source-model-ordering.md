# Module Source Model Ordering

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModuleSource.cs`.
- Migrated the legacy model ordering rule into `StaticAppModuleSource` for configured static module sources:
  - a base model is installed before the derived model
  - a collection property's referenced model is installed before the owner model
- Kept full reflective Java model discovery equivalent to legacy `.NET AssemblyModuleSource` as remaining work.

## Changes

- Added transient `baseModel` metadata to `Model` so module-source ordering can express legacy inheritance dependencies without changing the current database mapping.
- Updated `StaticAppModuleSource.getModels(...)` to return a stable dependency-ordered model list using only models inside the same module.
- Added cycle tolerance through the visiting set so malformed module definitions do not recurse forever.
- Updated the DAO mapping guard so transient `baseModel` is not treated as a persisted generic mapper field.
- Adjusted a relation metadata test fixture to find properties by name instead of relying on creation order after dependency sorting.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `Model.setBaseModel(...)` did not exist yet.
- Green:
  - Same focused command passed.
  - `AppManageMigrationTest`: 15 tests, 0 failures, 0 errors.
- Regression guard:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ModelDaoMappingTest -DfailIfNoTests=false test`
  - `ModelDaoMappingTest`: 1 test, 0 failures, 0 errors.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS`.
- Rebuilt and restarted backend:
  - `docker compose build --pull=false backend`
  - Result: backend image built, Maven reactor `BUILD SUCCESS`.
  - `docker compose up -d backend`
  - Result: backend container recreated and started.
- Runtime smoke:
  - `GET http://localhost:8080/test`: HTTP 200 with seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200 with `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200 with 2 rows.
  - `HEAD http://localhost:8081/`: HTTP 200.
- `docker compose ps --all` showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- Backend logs showed normal Spring startup and successful smoke requests.

## Remaining

- Dynamic `App.SysCon` datasource routing.
- Deeper legacy property/relation metadata edge cases.
- Full reflective Java model discovery equivalent to legacy `.NET AssemblyModuleSource`.
