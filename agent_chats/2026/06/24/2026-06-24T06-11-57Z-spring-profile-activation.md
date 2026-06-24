# Spring Profile Activation Syntax

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Addressed the parity backlog item for deprecated Spring profile syntax in module-level profile YAML files.
- Kept the change to profile-specific `application-dev.yml` and `application-test.yml` resources.
- Did not alter `application.yml` default active-profile declarations in this slice.

## Changes

- Replaced top-level `spring.profiles: dev` with `spring.config.activate.on-profile: dev` in:
  - `fool-auth/src/main/resources/application-dev.yml`
  - `fool-view/src/main/resources/application-dev.yml`
- Replaced top-level `spring.profiles: test` with `spring.config.activate.on-profile: test` in:
  - `fool-auth/src/main/resources/application-test.yml`
  - `fool-view/src/main/resources/application-test.yml`
  - `fool-query/src/main/resources/application-test.yml`
  - `fool-query/src/test/resources/application-test.yml`
  - `fool-dao/src/test/resources/application-test.yml`
  - `fool-model/src/test/resources/application-test.yml`
- Left the mixed `docs/migration/foolframe-parity.md` status update unstaged for a separate status commit.

## TDD

- Not applied: this is a configuration syntax migration. The verification target is config shape plus Maven resource/package processing.

## Verification

- Deprecated profile selector scan:
  - `rg -n "^\s*profiles:\s+(dev|test)\s*$" fool-auth/src/main/resources/application-dev.yml fool-auth/src/main/resources/application-test.yml fool-view/src/main/resources/application-dev.yml fool-view/src/main/resources/application-test.yml fool-query/src/main/resources/application-test.yml fool-query/src/test/resources/application-test.yml fool-dao/src/test/resources/application-test.yml fool-model/src/test/resources/application-test.yml`
  - Result: exit code 1, no legacy profile-specific `spring.profiles: dev|test` matches.
- Replacement syntax scan:
  - `rg -n "config:|on-profile:" ...same files...`
  - Result: all eight profile files contain `spring.config.activate.on-profile`.
- Maven package/resource processing:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth,fool-view,fool-query,fool-dao,fool-model -am -DskipTests package`
  - Result: exit code 0.

## Runtime Evidence

- `docker compose ps` before the change showed backend, frontend, MySQL, and Redis running; MySQL and Redis were healthy.
- This slice did not restart the Compose stack because it only changes profile resource syntax.

## Risks

- Spring context tests that depend on local datasource URLs were not run in this slice to avoid conflating config syntax with external DB availability.
- `application.yml` files still contain default `spring.profiles.active` settings; that is a separate policy decision from replacing deprecated profile-specific document selectors.

## Follow-ups

- Make backend tests self-contained without requiring datasource overrides.
- Commit this config slice separately from the already staged Query migration once `.git` writes are available.
