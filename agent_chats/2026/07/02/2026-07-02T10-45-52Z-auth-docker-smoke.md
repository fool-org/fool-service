# Auth Docker Smoke

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: make the Vue auth workflow usable in Docker.

## Scope

- Replaced non-portable raw-MD5-byte password strings with stable MD5 hex.
- Seeded Docker MySQL with an `admin` user, role, and `OrderList` auth menu.
- Updated migration parity docs.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceTest.java`
- `docker/mysql/init/007-auth.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-45-52Z-auth-docker-smoke.md`

## Validation

- Focused auth test:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=AuthServiceTest test`
  passed with 1 test, 0 failures, and 0 errors.
- Docker schema seed:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/007-auth.sql`
  completed, and MySQL showed `admin` with password
  `21232f297a57a5a743894a0e4a801fc3`, role `1`, and auth items `01`/`0101`.
- Backend image/runtime:
  `docker compose up -d --build backend` built the backend image and restarted
  `fool-service-backend-1`.
- Runtime auth smoke:
  `/api/v1/auth/login` with `admin` and an empty password returned code `0`,
  user `admin`, and a token. `/api/v1/auth/profile` returned user `admin`.
  `/api/v1/auth/auth-menus` returned the `Views` root with `OrderList` child.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS` across the 15-module reactor.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.

## Risks

- Existing rows created by the old raw-MD5-byte format will not match the new
  MD5-hex login path until their password values are reset.
