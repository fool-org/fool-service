# Compose Test Datasource Defaults

## Prompt

- Continue the active migration goal:
  1. bring the environment up with Docker,
  2. migrate against `../FoolFrame`,
  3. use Vue on the frontend,
  4. make timely atomic commits.
- Current slice: make backend Maven tests run inside the Docker Compose network
  without command-line datasource overrides.

## Scope

- Default module `application-test.yml` datasource settings to the Compose MySQL
  service at `mysql:3306/car_wash`.
- Preserve local override support with `SPRING_DATASOURCE_URL`,
  `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- Add a `fool-view` test `application.yml` so tests activate the `test`
  profile instead of inheriting the module's `dev` profile.
- Update validation and migration parity docs with the new command surface.

## Changed Files

- `fool-auth/src/main/resources/application-test.yml`
- `fool-dao/src/test/resources/application-test.yml`
- `fool-model/src/test/resources/application-test.yml`
- `fool-query/src/main/resources/application-test.yml`
- `fool-query/src/test/resources/application-test.yml`
- `fool-view/src/main/resources/application-test.yml`
- `fool-view/src/test/resources/application.yml`
- `docs/validation.md`
- `docs/migration/foolframe-parity.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  failed in `fool-dao` with `Communications link failure` /
  `Connection refused` because test config pointed at `127.0.0.1:3306`.
- Intermediate:
  the same focused command progressed after datasource defaults changed, then
  failed in `fool-view` because its main `application.yml` still activated the
  `dev` profile for tests.
- Green focused:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS`.
- Green full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS`.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Diff hygiene:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.

## Risks

- The documented command assumes the Compose project/network name
  `fool-service_default`.
- Host-only Maven remains supported through `SPRING_DATASOURCE_URL` and related
  datasource environment overrides.

## Follow-ups

- Continue closing the remaining FoolFrame parity gaps in
  `docs/migration/foolframe-parity.md`.
