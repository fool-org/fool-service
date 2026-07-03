# View QueryData Pascal Aliases

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep file sizes and code reuse under control.
- Correct the boundary: render pages from View metadata first, then query data
  by View, without binding the flow to concrete business DTOs.

## Scope

- Added Jackson aliases for FoolFrame Pascal request fields on the generic
  `getlistview` and `querydata` protocol DTOs.
- Kept the View/data chain metadata-driven: `getlistview(ViewId)` loads View
  columns and operations first, then `querydata(ViewId)` returns View-shaped
  rows.
- Added focused deserialization regression tests for the Pascal payloads that
  `../FoolFrame/src/Web/Cloud-Social/soway.js` sends.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ViewDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-45-46Z-view-querydata-pascal-aliases.md`

## Validation

- Red evidence:
  `fool-view/target/surefire-reports` recorded `Unrecognized field "ViewId"`
  for both `ViewDataRequest` and `LegacyQueryDataRequest` before the alias
  change.
- Green focused check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewControllerLegacyGetListViewTest,DataControllerLegacyQueryDataTest test`
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`
- Backend runtime:
  `docker compose up -d --build --force-recreate backend`
- Live Pascal `getlistview` smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"Token":"token-1","ViewId":100}' http://localhost:8080/api/v1/view/getlistview`
  returned `code=0`, `viewName=OrderList`, and populated `tableColumn`.
- Live Pascal `querydata` smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"Token":"token-1","ViewId":100,"PageSize":2,"PageIndex":1,"QueryFilter":"order_state=\"0\"","OrderByItem":0,"OrderByType":0}' http://localhost:8080/api/v1/data/querydata`
  returned `code=0`, page metadata, and View-shaped rows with `items`.

## Skipped Checks

- `cd frontend && npm test && npm run build` was not rerun because this slice
  does not edit frontend files.

## Risks / Follow-ups

- The default Docker seed still uses `OrderList` as the smoke View, but the
  production code touched here remains generic View/data protocol code.
- A first backend curl after image rebuild hit the old running container; the
  backend was force-recreated before recording runtime proof.
