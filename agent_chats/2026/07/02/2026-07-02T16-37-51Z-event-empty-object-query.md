# Prompt

Continue the active migration goal: keep Docker running, migrate against
`../FoolFrame`, keep the frontend on Vue, and make timely atomic commits.

# Scope

Event object-query parity for the no-matched-rows path. Legacy event checking
returns an empty object list when a definition query finds no rows; Java was
still validating an ID column before seeing whether the result set had data.

# Changes

- Added a red test for `JdbcEventObjectQuery` returning no matches when the
  resolved model query returns zero rows.
- Changed `JdbcEventObjectQuery.matchedObjects` to return an empty list before
  object-ID-column validation when the result set is empty.
- Updated `docs/migration/foolframe-parity.md` to record zero-row object-query
  empty-result parity.

# Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryReturnsNoMatchesWhenLegacyQueryFindsNoRows test`
  failed with `Can't Gerneration Query Because The Id Column Isn't Included!`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventObjectQueryReturnsNoMatchesWhenLegacyQueryFindsNoRows test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-event -am test`
  passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

# Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running, with
  MySQL and Redis healthy.
- `curl -fsS http://localhost:8080/test` returned the seeded smoke rows.
- `curl -fsS http://localhost:8081/` returned the Vue app HTML.
- `POST /api/v1/view/get-view` for `OrderList` returned `code: 0`.

# Risks

- Maven still emits the pre-existing duplicate `spring-jdbc` dependency warning
  for `fool-dao`.
- This does not complete broader event dynamic object-query parity.

# Follow-ups

- Continue the remaining `SCPB09-SOWAY.EVENT` runtime behavior listed in
  `docs/migration/foolframe-parity.md`.
