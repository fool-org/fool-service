# Prompt

Continue the active migration goal: keep Docker running, migrate against
`../FoolFrame`, keep the frontend on Vue, and make timely atomic commits.

# Scope

Query factory enum state-value mapping parity. The legacy concrete
`SCPB05-Soway.Model/Query/QueryFac .cs` `GetStateStr` implementation maps a
stored enum value back to its display string, while existing Java already
mapped display strings to stored values.

# Changes

- Added a red test proving `QueryFactory.getStateStr(column, "READY")` did not
  return the display string `就绪`.
- Reused the same `getStateValues()` list to support database-value to
  display-value lookup before preserving the existing display-value to
  database-value lookup.
- Updated `docs/migration/foolframe-parity.md` to list bidirectional
  state-value dictionary mapping.

# Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest#getStateStrMapsLegacyStateValuesInBothDirections test`
  failed with `expected:<就绪> but was:<READY>`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest#getStateStrMapsLegacyStateValuesInBothDirections test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryFactoryTest,QueryContextTest test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am test`
  passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

# Runtime Evidence

- The validation commands ran inside the active `fool-service_default` Compose
  network.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running, with
  MySQL and Redis healthy.
- `curl -fsS http://localhost:8080/test` returned the seeded smoke rows.
- `curl -fsS http://localhost:8081/` returned the Vue app HTML.
- `POST /api/v1/view/get-view` for `OrderList` returned `code: 0`.
- `POST /api/v1/data/query-list` for `OrderList` returned `code: 0` and two
  seeded rows.

# Risks

- Maven still emits the pre-existing duplicate `spring-jdbc` dependency warning
  for `fool-dao`.
- `DaoServiceTest` still logs a caught `NullPointerException` while the test
  class reports success; this was pre-existing noise and not changed here.
- This does not complete Query saved-query/report execution surfaces.

# Follow-ups

- Continue the remaining `SWDQ01-Soway.Query` saved-query/report execution and
  query-to-view parity work listed in `docs/migration/foolframe-parity.md`.
