# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server` `initnew` handler.
- Expose a minimal Spring and Vue migration slice for empty-object detail
  initialization.

# Changes

- Added `POST /api/v1/data/initnew`.
- Added `LegacyInitNewRequest` with legacy `ViewId` / `ParentObjId` aliases.
- Added `DataQueryService.initLegacyNewObject`, reusing the existing detail
  formatter with empty data and preserving `ParentObjId`.
- Added Vue API/payload types and an init-new-object panel.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerInitNewTest,DataQueryServiceDetailTest test`
  failed because `LegacyInitNewRequest` did not exist.
- RED: `cd frontend && npm test -- --run payload.test.ts` failed because
  the Vue route and `buildInitNewRequest` did not exist.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerInitNewTest,DataQueryServiceDetailTest test`
- PASS: `cd frontend && npm test`
- PASS: `cd frontend && npm run build`
- PASS: `docker compose up -d --build`

# Runtime Evidence

- Login with `admin` / `admin` returned a token.
- `POST http://localhost:8080/api/v1/data/initnew` with
  `{"ViewId":100,"ParentObjId":"5001"}` returned code `0`, blank `ObjId`,
  `ParentId=5001`, and at least three `SimpleData` rows.
- `POST http://localhost:8081/api/v1/data/initnew` with the same payload
  returned the same shape through the Vue frontend proxy.
- `GET http://localhost:8080/test` returned HTTP `200`.
- `GET http://localhost:8081/` returned HTTP `200`.

# Risks

- Current data-controller endpoints preserve the existing migration pattern and
  do not enforce legacy handler token authentication inside `fool-view`.

# Follow-ups

- Add `savenewobj` after mapping owner collection creation semantics against
  the existing Java dynamic save path.
