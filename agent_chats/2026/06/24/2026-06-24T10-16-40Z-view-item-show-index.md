# Prompt

- Continue the active goal: run the environment with Docker, migrate against
  `../FoolFrame`, keep the frontend on Vue, and commit atomically.

# Scope

- Migrated the legacy `ViewItem.ShowIndex` metadata surface from FoolFrame into
  the Java view model, list-view DTOs, Vue API typing, Docker seed schema, and
  list-column/list-row output ordering.
- Kept SQL default list ordering deferred for a separate commit because it
  changes query execution behavior rather than metadata/view response shape.

# Changes

- `ViewItem` now maps `showIndex` to `fool_sys_view_item.show_index`.
- `TableColumnInfo` now exposes `showIndex` to the Vue frontend.
- `ViewAdapter` orders list columns and generated input info by `showIndex`.
- `ViewDataAdapter` orders returned column labels and row value keys by
  `showIndex`.
- `docker/mysql/init/006-view.sql` creates and backfills the `show_index`
  column idempotently for the seeded `OrderList` view.
- `frontend/src/api.ts` includes `showIndex` in the Vue table-column type.
- `docs/migration/foolframe-parity.md` records the completed metadata/output
  parity and leaves SQL default ordering as remaining work.

# Validation

- Red:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewAdapterTest,ViewDataAdapterTest test`
  failed because `ViewItem` did not expose legacy `showIndex` metadata.
- Green focused:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewAdapterTest,ViewDataAdapterTest test`
  passed, 8 tests.
- Schema apply:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
  passed.
- Schema verification:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM fool_sys_view_item LIKE 'show_index'; SELECT model_property, show_index FROM fool_sys_view_item WHERE view_id = 100 ORDER BY show_index;"`
  showed `show_index` and seed order `orderId=1`, `symbol=2`, `state=3`.
- Wider backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  passed, 15 tests.
- Frontend:
  `npm test` passed in `frontend/`, 2 tests.
- Frontend build:
  `npm run build` passed in `frontend/`.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build` passed; backend Docker build reported Maven
  `BUILD SUCCESS`.
- Runtime reload:
  `docker compose up -d --force-recreate backend frontend` recreated both
  runtime containers.

# Runtime Evidence

- `docker compose ps` showed backend and frontend running, MySQL and Redis
  healthy.
- `curl -sf http://localhost:8080/test` returned the seeded order JSON.
- `curl -sfI http://localhost:8081/` returned `HTTP/1.1 200 OK`.
- `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  returned table columns ordered as `orderId`, `symbol`, `state` with
  `showIndex` values `1`, `2`, and `3`.
- `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":10}}' http://localhost:8080/api/v1/data/query-list`
  returned `cols` as `Order ID`, `Symbol`, `State` and row values keyed
  `orderId`, `symbol`, `state`, with `rowIndex` still present.
- Calling `query-list` without `pageInfo` returned the existing generic error
  because `ModelDataService` dereferences a null `PageNavigator`; the successful
  smoke uses the real paged request shape.

# Risks

- Legacy default list-query SQL ordering by the first `ShowIndex` item remains
  deferred.
- Full self-contained backend tests still depend on an external/Compose MySQL
  override for DAO-backed modules.

# Follow-ups

- Migrate legacy default descending list-query SQL order by the first
  `ShowIndex` item in a separate behavior commit.

# Linked Commits

- Pending at evidence creation.
