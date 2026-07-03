# View Keyword Candidates

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Correct the architecture so the frontend first loads View metadata, then
  queries data from that View, without binding page flow to business DTOs or
  frontend-built SQL filters.
- Keep file size and code reuse under control.

## Scope

- Added `keyword` to the legacy `querydata` request DTO and controller path.
- Kept legacy raw `queryFilter` support for API tools and report compatibility.
- Reused backend View/model keyword filtering so legacy `querydata` maps
  visible View items to real model columns on the server.
- Changed Vue select-from-existing candidate loading to send `keyword` instead
  of constructing a `LIKE` expression from `tableColumn`.
- Extracted child candidate keyword/page/row/column state into
  `frontend/src/useChildCandidates.ts` to avoid further growing `App.vue`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `npm test -- payload.test.ts`
  - Red first: legacy payload omitted expected `keyword`.
- `npm test -- useChildCandidates.test.ts`
  - Red first: `useChildCandidates` module did not exist.
- `cd frontend && npm test && npm run build`
  - Passed: 3 test files, 43 tests.
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=DataControllerLegacyQueryDataTest,DataQueryServiceOrderingTest test`
  - Passed: 8 focused backend tests.
- `docker compose up -d --build`
  - Passed; backend and frontend images built.
- `docker compose up -d --force-recreate backend frontend`
  - Passed; backend/frontend containers recreated.
- `docker compose build --quiet frontend`
  - Passed after the frontend composable extraction.
- `docker compose up -d --no-deps --force-recreate frontend`
  - Passed; frontend container recreated after extraction.

## Runtime Evidence

- Docker status:
  - backend: `fool-service-backend`, recreated and running on `0.0.0.0:8080`.
  - frontend: `fool-service-frontend`, recreated and running on
    `0.0.0.0:8081`.
  - MySQL and Redis remained healthy.
- Backend smoke:
  - `curl http://localhost:8080/test` returned JSON rows.
- Legacy `querydata` keyword:
  - Request:
    `{"viewId":100,"pageSize":10,"pageIndex":1,"keyword":"BTC"}`
  - Result: `code=0`, `totalItem=1`, row `1001 / BTC-USDT / Ada Capital / 0`.
- Legacy `querydata` no-match keyword:
  - Request:
    `{"viewId":100,"pageSize":10,"pageIndex":1,"keyword":"NO-SUCH-SYMBOL-XYZ"}`
  - Result: `code=0`, `totalItem=0`.
- Legacy raw `queryFilter` compatibility:
  - Request:
    `{"viewId":100,"pageSize":10,"pageIndex":1,"queryFilter":"order_state=\"0\""}`
  - Result: `code=0`, `totalItem=4`.
- Browser target: `http://localhost:8081/`.
- Browser plugin was available. `domSnapshot()` still fails in this environment
  with `incrementalAriaSnapshot is not a function`, so rendered proof used
  read-only page evaluation, screenshots, scoped Playwright interaction, and
  console logs.
- Browser interaction after frontend rebuild:
  - Page title: `Fool Service`.
  - Visible title: `Order List`.
  - Filled `Keyword` with `BTC`.
  - Clicked `Load View`.
  - Rendered list reduced to 1 row:
    `1001 / BTC-USDT / Ada Capital / Open`.
  - Detail panel selected object `1001`.
  - Console warnings/errors: none.

## Skipped Checks

- Full `mvn test` was not run; this slice touched `fool-view` and focused
  `-pl fool-view -am` tests covered the changed backend query path. Local host
  Maven uses Java 8, so Java validation used the repository-documented Maven
  Java 17 Docker image.

## Risks

- The running Docker seed currently has no visible `selectFromExists=true`
  child group, so candidate controls were verified by unit tests and the same
  backend keyword path through the main View list. The next migration slice
  should seed or hydrate the configured child selected View so the child dialog
  itself is visible in Docker.
