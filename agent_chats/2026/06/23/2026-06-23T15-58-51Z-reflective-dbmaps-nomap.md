# Reflective DBMaps and NoMap Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/ColumnAttribute.cs`, `ORMHelper.cs`, `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`, `Property.cs`, `MultiDBMap.cs`, and `ModelSqlServerFactory.cs`.
- Migrated the next reflective property-metadata slice: explicit no-map columns and multi-column `DBMaps` for business-object fields.

## Changes

- Made Java `@Column` repeatable through `@Columns`.
- Added `Column.propertyName()` for legacy complex-property DBMaps.
- Added `Column.noMap()` for explicit legacy no-map metadata.
- Added `MultiDbMap` and `Property.dbMaps`.
- `ReflectiveAppModuleSource` now:
  - treats explicit `@Column(noMap = true)` as a property with no database column;
  - treats repeated `@Column(value = ..., propertyName = ...)` annotations as `multiMap=true` with `dbMaps`;
  - leaves multi-map business-object properties without a single DB column, matching legacy `AssemblyModelFactory`.
- `LegacyMysqlDdlGenerator` now:
  - skips no-map properties with blank columns;
  - emits one physical column per DB map using the target model property type.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `MultiDbMap` did not exist, finished at `2026-06-23T15:57:10Z`.
  - A direct `fool-model` run without `-am` also failed dependency resolution and was not used as parity evidence.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=LegacyMysqlDdlGeneratorTest -DfailIfNoTests=false test`
  - Result: `LegacyMysqlDdlGeneratorTest`: 6 tests, 0 failures, 0 errors, finished at `2026-06-23T15:58:31Z`.
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Result: `AppManageMigrationTest`: 29 tests, 0 failures, 0 errors, finished at `2026-06-23T15:58:34Z`.

## Remaining

- DBMaps query/loading runtime behavior is not fully migrated.
- Legacy parent column prefix overrides (`PreIndex`, `PreLen`, `OverideParent`), generated expressions, and default values remain.
- Persisting DBMaps into a legacy-compatible relation/table shape remains separate from the in-memory model and DDL generation parity covered here.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules, finished at `2026-06-23T16:00:55Z`.
- Repository gates:
  - `python scripts/check_repo_harness.py`: `Repository harness validation passed.`
  - `git diff --check`: passed.
- Backend compose image build:
  - `docker compose build --pull=false backend`
  - Result: `backend  Built`; Dockerfile Maven `-DskipTests package` finished at `2026-06-23T16:01:55Z`; image manifest `sha256:e232ff04abf8b254f400c68fa90a4c21fd54ba8b0fa261df96ceb3204f77b5b2`, container image id `e232ff04abf8`.
- Runtime smoke after `docker compose up -d backend`:
  - `docker compose ps`: backend/frontend/mysql/redis running; MySQL and Redis healthy.
  - `GET http://localhost:8080/test`: HTTP 200 with 2 seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200, `code=0`, `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200, `code=0`, 2 data rows.
  - `HEAD http://localhost:8081/`: HTTP 200, `text/html`.
  - Backend logs showed Spring startup at `2026-06-23T16:02:05+0800`, Tomcat started at `2026-06-23T16:02:06+0800`, and successful smoke responses through `2026-06-23T16:02:23+0800`.
