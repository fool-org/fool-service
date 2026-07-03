# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server/New/HandlerSaveNew.cs` path.
- Expose the smallest migrated `savenewobj` slice for simple new-object creates
  and owner collection create routing.

# Changes

- Added `POST /api/v1/data/savenewobj`.
- Added `LegacySaveNewObjRequest` with legacy `SaveObj`, `OwnerViewId`,
  `OwnerId`, and `Property` aliases.
- Reused the existing `saveobj` object-to-dynamic mapper for `savenewobj`.
- Added a public `ModelDataService.createData(data, extraColumn, extraValue)`
  wrapper for owner collection creates.
- Added Vue API/payload support and a save-new-object panel with Docker-seeded
  `OrderList` defaults.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerSaveNewObjTest,DataQueryServiceSaveObjTest test`
  failed before `LegacySaveNewObjRequest` existed.
- RED: `npm test -- --run payload.test.ts`
  failed before the Vue `savenewobj` panel and payload builder existed.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerSaveNewObjTest,DataQueryServiceSaveObjTest test`
  - Passed: 6 tests, 0 failures, 0 errors.
- PASS: `npm test -- --run payload.test.ts`
  - Passed: 24 tests.
- PASS: `docker compose up -d --build`
  - Backend Maven package and frontend production build passed.

# Runtime Evidence

- Login with `admin` / `admin` returned a token.
- `POST http://localhost:8080/api/v1/data/savenewobj` with legacy `SaveObj`
  created object `9851340`; `querydatadetail` returned symbol `SOL-0-USDT`,
  state `Open`, and three simple detail rows.
- `POST http://localhost:8081/api/v1/data/savenewobj` through the Vue frontend
  proxy created object `9851341`; `querydatadetail` returned symbol
  `SOL-1-USDT`, state `Open`, and three simple detail rows.
- `GET http://localhost:8080/test` returned HTTP `200`.
- `GET http://localhost:8081/` returned HTTP `200`.

# Risks

- Docker seed has no separate `OrderItem` child view, so owner collection
  routing is service-test covered rather than runtime-smoked through HTTP.
- Current data-controller endpoints preserve the existing migration pattern and
  do not enforce legacy handler token authentication inside `fool-view`.

# Follow-ups

- Continue remaining server surfaces such as operation execution, user/app info,
  and the deeper model/query/event/report long tail.
