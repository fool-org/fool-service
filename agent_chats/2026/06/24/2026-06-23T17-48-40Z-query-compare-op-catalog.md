# Query Compare Operation Catalog Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/CompareOpFac.cs` and `CampareOp.cs`.
- Migrated the persisted compare-operation catalog slice for `SWDQ01-Soway.Query`.
- Preserved legacy behavior where `CompareOpFac.GetCompareOps(PropertyType, long prpId)` ignores `prpId` and loads by property type only.

## Changes

- Added `LegacyCompareOp` as the Java equivalent surface for legacy `CampareOp`.
- Added `JdbcCompareOpCatalog` to query legacy `SE_COMPARETYPE` joined with `SE_COMPARETYPE_PROPERTYINDEX` by `PropertyType.ordinal()`.
- Added Docker MySQL init schema and seed rows for `SE_COMPARETYPE` and `SE_COMPARETYPE_PROPERTYINDEX`.
- Updated `docs/migration/foolframe-parity.md` to mark persisted compare-operation catalog loading as migrated and narrow the remaining `SWDQ01-Soway.Query` work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=CompareOpCatalogTest -DfailIfNoTests=false test`
  - Result: compile failed because `JdbcCompareOpCatalog` and `LegacyCompareOp` did not exist.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=CompareOpCatalogTest -DfailIfNoTests=false test`
  - Result: `CompareOpCatalogTest`: 3 tests, 0 failures, 0 errors.
- Query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `CompareFilterTest` and `CompareOpCatalogTest`: 6 tests, 0 failures, 0 errors.

## Verification

- Compose MySQL schema smoke:
  - Applied `docker/mysql/init/010-query.sql` to the running Compose MySQL.
  - Queried `PROPERTYTYPE_VALUE=11` and verified string operations:
    - `等于`: `{0} = {1}`
    - `不等于`: `{0} <> {1}`
    - `包含`: `{0} LIKE {1}`
- Full backend Maven package on the Compose network with datasource pointed at `mysql:3306/car_wash`:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 36.679 s.
- Backend compose image/runtime:
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
  - Backend mapped at `localhost:8080`; Vue frontend mapped at `localhost:8081`.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.

## Remaining

- Compare-operation catalog loading is now covered, but selected table/column factories, joins, report/query instance orchestration, and deeper query-to-view integration remain separate migration slices.
