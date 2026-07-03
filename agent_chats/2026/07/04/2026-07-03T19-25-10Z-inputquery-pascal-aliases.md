# InputQuery Pascal Aliases

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep file sizes and reuse under control.
- Focus on the goal itself, not the seeded `BTC-USDT` order example.
- Correct the boundary: render from View first, query data from View metadata,
  and avoid binding the flow to concrete business DTOs.

## Scope

- Added Jackson aliases for legacy FoolFrame `inputquery` request fields on
  the protocol DTO only.
- Kept the lookup flow metadata-driven: View selection is still carried by
  `viewName` / `viewItemId`, not by an `Order` or other business DTO.
- Added a focused deserialization regression test for Pascal payloads.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/InputQueryRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerInputQueryTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-25-10Z-inputquery-pascal-aliases.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerInputQueryTest test`
  failed before the alias change with `Unrecognized field "Text"`.
- Green focused check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerInputQueryTest test`
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
- Backend runtime:
  `docker compose up -d --build backend`
- Live view/data boundary smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  returned View metadata with `tableColumn`.
- Live data smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":2,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  returned View-shaped rows with `items`.
- Live Pascal `inputquery` smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"Text":"Ada","ViewName":"OrderList","ViewItemId":"Customer","ModelID":"103","ObjID":"1001","OwnerId":"5001","IsAdded":false}' http://localhost:8080/api/v1/data/inputquery`
  returned `{"code":0,"message":"success","data":{"items":[{"id":"3001","text":"Ada Capital"}]}}`.
- Live camel `inputquery` smoke:
  `curl -fsS -H 'Content-Type: application/json' -d '{"text":"Ada","viewName":"OrderList","viewItemId":"Customer","objID":"1001","isAdded":false}' http://localhost:8080/api/v1/data/inputquery`
  returned `{"code":0,"message":"success","data":{"items":[{"id":"3001","text":"Ada Capital"}]}}`.

## Skipped Checks

- `cd frontend && npm test && npm run build` was not rerun because this change
  does not edit frontend files.
- Local Maven was not used because the host Java is 8 and the project targets
  Java 17; Docker Maven 17 was used instead.

## Risks / Follow-ups

- `App.vue` remains oversized and should keep being reduced by extracting
  metadata workflow pieces only when a real seam is already visible.
- Docker seed names such as `OrderList` and `BTC-USDT` are smoke fixtures, not
  a contract to bind the frontend or backend to order-specific DTOs.
