# Prompt

Continue the active migration goal: keep Docker running, migrate against
`../FoolFrame`, keep the frontend on Vue, and make timely atomic commits.

# Scope

Model single-row detail lookup parity for legacy
`SCPB05-Soway.Model/SqlServer/dbContext.GetDetail`. The Java
`ModelDataService#getOneData` path was still a stub, so dynamic data could not
load one row by model ID and data ID.

# Changes

- Added a MySQL-backed integration test for loading one dynamic row by model
  name and data ID.
- Implemented `ModelDataService#getOneData` with the existing `SqlGenerator`,
  `Mapper`, and `CompareFilter` path.
- Taught the generic DAO mapper to honor `@Column(noMap = true)`.
- Mapped runtime DBMaps to legacy `SW_SYS_MULTIMAP` / `SW_SYS_PROPERTY_DBMapsSysId`.
- Marked the empty runtime `Property.triggerList` shell as no-map so model
  detail loading is not blocked by an unimplemented trigger object.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#getOneDataLoadsLegacyDetailByModelIdAndDataId test`
  failed because `getOneData` returned `null`.
- GREEN:
  the same focused command passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

# Runtime Evidence

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
- Runtime trigger execution is still not implemented; `triggerList` is skipped
  only because the current `Trigger` model is an empty shell.
- This does not complete save/delete/init dynamic data parity.

# Follow-ups

- Continue the remaining `SCPB05-Soway.Model` runtime data operations and the
  open Query/Event/Report/AppInstallGateway items in
  `docs/migration/foolframe-parity.md`.
