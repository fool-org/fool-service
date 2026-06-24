# Query Selected Type Catalog Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/SelectedTypeFac.cs` and `SelectType.cs`.
- Migrated persisted select-type catalog loading for `SWDQ01-Soway.Query`.
- Preserved legacy behavior where `SelectedTypeFac.GetSelectedType(PropertyType, long id)` ignores `id` and loads by property type only.
- Preserved legacy `GetAllSelectedType()` as `JdbcSelectTypeCatalog.listAll()`.

## Changes

- Added `JdbcSelectTypeCatalog` to query legacy `SE_SELECTEDTYPE` joined with `SE_SELECTEDTYPE_PROPERTYINDEX` by `PropertyType.ordinal()`.
- Added all-select-types loading from `SE_SELECTEDTYPE`.
- Extended `docker/mysql/init/010-query.sql` with `SE_SELECTEDTYPE` and `SE_SELECTEDTYPE_PROPERTYINDEX` schema plus seeded select types.
- Updated `docs/migration/foolframe-parity.md` to mark persisted select-type catalog loading as migrated and narrow remaining `SWDQ01-Soway.Query` work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectTypeCatalogTest -DfailIfNoTests=false test`
  - Result: test compile failed because `JdbcSelectTypeCatalog` did not exist.
- Green:
  - Same command.
  - Result: `SelectTypeCatalogTest`: 4 tests, 0 failures, 0 errors.

## Runtime Seed Smoke

- Applied `docker/mysql/init/010-query.sql` to the running Compose MySQL.
- Queried `PROPERTYTYPE_VALUE=11` and verified selected types:
  - `原值`: `{0}`, `require_group=1`
  - `计数`: `COUNT({0})`, `require_group=0`

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 15 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 33.743 s.
- Backend compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.

## Remaining

- `TableCollection` and `ColCollection` name-based lookup behavior is not migrated yet.
- Query/report instance orchestration and SQL projection generation remain separate migration slices.
