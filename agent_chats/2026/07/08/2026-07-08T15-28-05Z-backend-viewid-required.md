# Backend ViewId Required

## Prompt

先查 View 渲染页面，再根据 View 查 data；避免按具体业务 DTO 或业务名启动数据查询。

## Scope

- Audited the remaining backend compatibility routes after the Vue render
  boundary was tightened.
- Changed `/api/v1/view/get-view` and `/api/v1/data/query-list` so a request
  without `ViewId` throws `CommonException` instead of falling back to
  `ViewName`.
- Kept the existing service methods intact for internal compatibility; the
  controller API boundary now requires the rendered View id.
- Updated smoke examples and task/parity state to stop advertising viewName-only
  data loading.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataControllerLegacyQueryDataTest,ViewControllerLegacyGetListViewTest test` failed because viewName-only requests did not throw.
- Green: same command after the controller guard change.
- Green: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`.

## Skipped Checks

- A first full-module Docker run without `--network fool-service_default`
  failed with `UnknownHostException: mysql`; it was rerun with the documented
  compose network and passed.

## Risks

- Clients still using viewName-only `/get-view` or `/query-list` must switch to
  `ViewId`; this is intentional for the migrated View-first protocol.
