# Prompt

Continue the Docker/FoolFrame/Vue migration and keep the rendered View as the
source of truth before querying data. Avoid binding the flow to concrete
business DTOs.

# Scope

- Fix a legacy protocol mismatch found while checking the View-first detail
  path.
- Keep the change at the shared DTO boundary so all callers benefit.
- Add the smallest runtime guard to the existing Docker doctor.

# Changes

- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyQueryDataDetailRequest.java`
  - Added `ViewId` and `ObjId` aliases for legacy Pascal payloads.
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
  - Expanded the alias test to cover `ViewId`, `ObjId`, and `IdExp` together.
- `scripts/runtime_doctor.py`
  - Added a `querydatadetail(ViewId, ObjId)` smoke check through the frontend
    proxy.
- `tasks.md`
  - Marked the legacy detail-data Pascal request boundary complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the parity increment.

# Validation

- Before rebuilding backend, `curl -d '{"ViewId":100,"ObjId":"1001"}' .../querydatadetail`
  returned `{"code":850001,"message":"没有查到视图","data":null}`.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerLegacyQueryDataDetailTest test`
  - Passed.
- `docker compose up -d --build backend`
  - Passed; Docker backend build completed with Maven reactor `BUILD SUCCESS`.
- After rebuild, `curl -d '{"ViewId":100,"ObjId":"1001"}' .../querydatadetail`
  returned `code=0` and detail `simpleData`.
- `python3 scripts/runtime_doctor.py`
  - Passed, including `data:querydatadetail`.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up, port `8080`.
  - `fool-service-frontend-1`: Up, port `8081`.
  - MySQL and Redis healthy.

# Risks

- This only fixes the legacy request field aliases. It does not change richer
  `IdExp` expression semantics beyond the already-covered static expression
  path.
