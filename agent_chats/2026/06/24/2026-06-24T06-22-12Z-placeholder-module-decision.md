# Placeholder Module Decision

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Resolved the parity-doc remaining item about whether `fool-dynamic`, `fool-reflect`, and `fool-restapi` should become real Maven reactor modules.
- This is a migration-structure decision, not a Java behavior change.

## Findings

- `fool-dynamic/` contains only `pom.xml`; there is no Java source.
- `fool-reflect/` contains only `pom.xml`, and its artifact id is currently `foo-reflect`.
- `fool-restapi/` has no POM and only three empty controller stubs under a non-Maven source path:
  - `fool-restapi/src/org/fool/framework/auth/rest/controller/AuthController.java`
  - `fool-restapi/src/org/fool/framework/auth/rest/controller/ViewController.java`
  - `fool-restapi/src/org/fool/framework/auth/rest/controller/DataController.java`
- Actual migrated REST surfaces are already in:
  - `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
  - `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
  - `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- Reflective module-source behavior is tracked in `fool-app-manage`, not the placeholder `fool-reflect` module.

## Changes

- Updated `docs/migration/foolframe-parity.md` to mark the three modules as intentionally not wired into the root reactor for the current migration.
- Removed the matching remaining-work bullet.

## Validation

- Source/module inspection:
  - `find fool-dynamic fool-reflect fool-restapi -maxdepth 4 -type f | sort`
  - `find fool-dynamic fool-reflect fool-restapi -maxdepth 3 -type f -name 'pom.xml' -print -exec sed -n '1,220p' {} \;`
  - `find fool-restapi -type f -print -exec sed -n '1,80p' {} \;`
  - `rg -n "fool-restapi|fool-dynamic|fool-reflect|foo-reflect|restapi|AuthController|DataController|ViewController" -S . --glob '!target/**'`
- Repository validation:
  - `python scripts/check_repo_harness.py`: passed.
  - `git diff --check`: passed.
  - `git diff --cached --check`: passed.

## Runtime Evidence

- No runtime change. The current Docker/Vue smoke evidence remains in `agent_chats/2026/06/24/2026-06-24T06-12-52Z-docker-vue-runtime-smoke.md`.

## Risks

- These placeholders can be revived later if new source is added, but wiring them now would add empty or duplicate modules to the reactor.
- `fool-restapi` stubs are left in place to avoid deleting user/history artifacts during this migration pass.

## Follow-ups

- Continue concrete FoolFrame parity work in active modules.
- Remove or archive placeholder directories only if the user explicitly wants cleanup.
