# InputQuery Owner Source

## Prompt

Continue the FoolFrame migration while keeping View rendering first, then data
and lookup requests from the rendered View context. Avoid binding lookup to
concrete business DTOs.

## Scope

- Made backend `inputquery` treat `#.` source-list expressions as owner
  context for existing child items, not only added child items.
- Kept direct source-list lookup and target-model fallback behavior unchanged.
- Passed Vue child lookup owner context through the shared
  `MetadataFieldEditor` instead of adding child-specific lookup code.
- Kept this scoped to source-list owner context; arbitrary expression
  evaluation remains out of scope.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-32-46Z-inputquery-owner-source.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest test`
  - Failed because existing child `#.` source lookup fell through to target-model candidates.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest test`
  - `9` focused inputquery tests passed.
- RED: `npm test -- payload.test.ts`
  - Failed because metadata lookup did not pass `ownerId`.
- GREEN: `npm test -- payload.test.ts`
  - `58` focused frontend tests passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  - `153` backend module tests passed.
- GREEN: `npm test`
  - `93` frontend tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `docker compose ps`
  - backend, frontend, MySQL, and Redis were running; MySQL was healthy.
- GREEN: `python scripts/runtime_doctor.py`
  - Docker runtime smokes passed through backend, frontend proxy, auth shell,
    View/data, inputquery, report, message, notify, and logout paths.

## Risks

- Vue child lookup now has owner context, but broader field-specific lookup
  behavior still depends on the rendered View and detail item metadata being
  present. This change does not add a second lookup path or business-specific
  child component.
