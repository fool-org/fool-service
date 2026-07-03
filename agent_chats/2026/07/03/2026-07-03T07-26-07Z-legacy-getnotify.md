# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the remaining `../FoolFrame` message surfaces.
- Expose the legacy `getnotify` contract in Spring and Vue.
- Keep the implementation limited because FoolFrame `DataService.GetNotify`
  throws `NotImplementedException`.

# Changes

- Added `POST /api/v1/message/getnotify`.
- Added `GetNotifyResult` / `NotifyInfo` DTOs.
- Added a Vue notify-count panel and frontend API types.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl business-application -am -DfailIfNoTests=false -Dtest=MessageControllerTest test`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `docker compose up -d --build`

# Runtime Evidence

- Login with `admin` / `admin` returned a token.
- `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8080/api/v1/message/getnotify`
  returned `{"code":0,"message":"success","data":{"notifies":[]}}`.
- `curl -H 'Content-Type: application/json' -d '{"token":"<login-token>"}' http://localhost:8081/api/v1/message/getnotify`
  returned `{"code":0,"message":"success","data":{"notifies":[]}}`.
- `curl -H 'Content-Type: application/json' -d '{"token":"bad-token"}' http://localhost:8080/api/v1/message/getnotify`
  returned `{"code":8100004,"message":"token无效","data":null}`.
- `curl http://localhost:8081/` returned HTTP `200`.
- `curl http://localhost:8080/test` returned HTTP `200`.

# Risks

- This is an empty shell because the legacy service method is not implemented.
  Real notification-count semantics still need a confirmed source.

# Follow-ups

- Implement `runoperation` only after mapping a real existing Java operation
  execution path or migrating the missing execution layer.
