# Prompt

- Continue the active goal: run the environment with Docker, migrate against
  `../FoolFrame`, keep the frontend on Vue, and commit atomically.

# Scope

- Migrated legacy list-view raw `Filter` SQL from
  `SCPB05-Soway.Model/Context/ListViewQueryContext.cs` into the Java
  `DataQueryService` list-query path.
- Kept the change scoped to `fool-view` service filtering, focused tests,
  migration parity docs, and delivery evidence.

# Changes

- `DataQueryService` now starts the generated query filter with `view.Filter`
  when the view metadata supplies raw SQL, then adds request filters with
  parameterized `CompareFilter` / `BetweenFilter`.
- Blank view filters still use the existing `1=1` default.
- `DataQueryServiceOrderingTest` captures the filter passed to
  `ModelDataService` and verifies the raw view filter is applied before the
  request filter.
- `docs/migration/foolframe-parity.md` records raw view-filter parity.

# Validation

- Red:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  failed because the generated SQL was `( 1=1 ) And (...)` instead of starting
  with the legacy view filter.
- Green focused:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  passed, 2 tests.
- Docker build/runtime:
  `docker compose up -d --build` passed and started MySQL, Redis, backend, and
  frontend.
- Runtime smoke:
  `docker compose ps` showed MySQL and Redis healthy, with backend on `8080`
  and frontend on `8081`.
- Runtime smoke:
  `curl -sf http://localhost:8080/test` returned seeded order data.
- Runtime smoke:
  `curl -sfI http://localhost:8081/` returned `HTTP/1.1 200 OK`.
- Runtime smoke:
  `curl -sf -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageIndex":1,"pageSize":10}}' http://localhost:8080/api/v1/data/query-list`
  returned `code:0`, 2 rows, and the migrated row indexes.
- Backend module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  passed, 17 tests.
- Frontend Vue:
  `cd frontend && npm test` passed, 2 tests.
- Frontend Vue:
  `cd frontend && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Diff hygiene:
  `git diff --check` passed.

# Runtime Notes

- Docker Desktop was not running at the start of this slice; `open -a Docker`
  restored the daemon and Docker Maven validation then ran normally.
- Existing `OrderList` smoke data keeps an empty view filter; no seed-data
  churn was added for this code-only parity slice.
- Local Java remains Java 8, so Java 17 Maven checks were run in Docker.

# Risks

- `view.Filter` is intentionally raw metadata SQL to match FoolFrame behavior.
  Request filters remain parameterized.
- Full backend verification depends on Compose MySQL for DAO-backed modules; it
  passed in this slice.

# Follow-ups

- Continue richer query-to-view integration and saved-query/report execution
  parity.

# Linked Commits

- Pending at evidence update.
