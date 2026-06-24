# Reflective Column Prefix Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB01-Soway.Data/Discription/ORM/TableAttribute.cs`, `ColumnAttribute.cs`, and `ORMHelper.cs`.
- Migrated the reflective column-name slice for legacy `ColPreStr`, `PreIndex`, `PreLen`, and `OverideParent` behavior into the Java annotation path.

## Changes

- Added `Table.columnPrefix()` as the Java equivalent of legacy `TableAttribute.ColPreStr`.
- Added `Column.preIndex()`, `Column.preLen()`, and `Column.overrideParent()` as the Java equivalent of legacy column prefix insertion controls.
- `ReflectiveAppModuleSource` now applies the FoolFrame `GetColName` rule for explicit column annotations:
  - `preIndex == -1` keeps the column name unchanged;
  - blank table prefixes keep the column name unchanged;
  - `overrideParent = true` keeps the column name unchanged;
  - otherwise the table prefix, optionally truncated by `preLen`, is inserted into the annotated column name at `preIndex`.
- Repeatable `@Column` DBMaps now use the same column-name helper, so prefixed table metadata applies consistently to multi-column business-object mappings.
- Updated `docs/migration/foolframe-parity.md` to move table column-prefix metadata into the migrated reflective column metadata bucket.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `@Table.columnPrefix`, `@Column.preIndex`, `@Column.preLen`, and `@Column.overrideParent` did not exist, finished at `2026-06-23T16:07:03Z`.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Result: `AppManageMigrationTest`: 30 tests, 0 failures, 0 errors, finished at `2026-06-23T16:08:27Z`.

## Verification

- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules, finished at `2026-06-23T16:16:16Z`.
- Repository gates:
  - `python scripts/check_repo_harness.py`: `Repository harness validation passed.`
  - `git diff --check`: passed.
- Backend compose image build:
  - `docker compose build --pull=false backend`
  - Result: `backend  Built`; Dockerfile Maven `-DskipTests package` finished at `2026-06-23T16:17:10Z`; image manifest `sha256:d5d5849a00469fdc84252da9d800bd0322057f5cbae1893f26321593e30467ff`, container image id `d5d5849a0046`.
- Runtime smoke after `docker compose up -d backend`:
  - `docker compose ps`: backend/frontend/mysql/redis running; MySQL and Redis healthy.
  - `GET http://localhost:8080/test`: HTTP 200 with 2 seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200, `code=0`, `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200, `code=0`, 2 data rows.
  - `HEAD http://localhost:8081/`: HTTP 200, `text/html`.
  - Backend logs showed Spring startup at `2026-06-23T16:17:22+0800`, Tomcat started at `2026-06-23T16:17:23+0800`, and successful smoke responses through `2026-06-23T16:17:39+0800`.

## Remaining

- DBMaps query/loading runtime behavior is not fully migrated.
- Generated expressions and default values remain separate legacy `ColumnAttribute` metadata.
- Inherited-property reflection parity beyond Java-declared fields remains separate from the column-name helper covered here.
