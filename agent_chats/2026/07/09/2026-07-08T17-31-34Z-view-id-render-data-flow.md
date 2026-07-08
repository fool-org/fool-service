# ViewId Render And Data Flow

## Prompt

Tighten the migration around the View-first runtime flow: render pages from
View metadata first, then query data from the loaded View, without binding the
frontend or lookup path to business DTO names.

## Scope

- Removed the Vue workflow `viewName` state and stopped the input-query payload
  builder from accepting or sending `viewName`.
- Required `ViewId` on legacy `getlistview` and `getreaditemview`.
- Required `ViewId` for backend `inputquery` lookup resolution instead of
  falling back to `ViewName`.
- Updated focused frontend/backend tests, parity notes, and task state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetReadItemViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceInputQueryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- payload.test.ts` failed because `App.vue` still kept
  `const viewName = ref`.
- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest,ViewControllerLegacyGetReadItemViewTest,DataQueryServiceInputQueryTest test`
  failed because legacy View endpoints did not reject ViewName-only requests
  and `inputquery` returned `没有查到视图` instead of requiring `ViewId`.
- Green: `npm test -- payload.test.ts` passed.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest,ViewControllerLegacyGetReadItemViewTest,DataQueryServiceInputQueryTest test`
  passed.
- Frontend: `npm test` passed, 4 files / 81 tests.
- Frontend: `npm run build` passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
  exited 0. Existing `DataQueryServiceTest` logged `没有查到视图` as part of its
  caught test path, but the Maven run passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Artifacts

None. This is protocol/rendering boundary work.

## Risks

- Legacy clients that still send only `ViewName` to `getlistview`,
  `getreaditemview`, or `inputquery` now receive `ViewId is required`.

## Follow-ups

- Continue replacing the remaining API-tool surface with the same ViewId-first
  workflow where it is still manually driven.
