# Prompt

Continue the active goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.

# Scope

This slice starts migrating `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT` into `fool-service`.

# Changes

- Added `fool-event` as a Maven reactor module and wired it into `business-application`.
- Added legacy event enums: `EventState`, `MsgState`, `MsgNotifyType`, and `EventModelRefType`.
- Added legacy event/message entities for `SW_EVT_DEF`, `SW_EVT_EVENT`, and `SW_SYS_MSG`.
- Added `EventSqlHelper` for the legacy `SELECT * FROM <table> WHERE <filter>` query shape.
- Added `MessageFactory` with deterministic test constructors and Spring service exposure.
- Added `EventMessageRepository` plus `JdbcEventMessageRepository` to persist generated messages.
- Added event runtime contracts and services: `EventRuntimeService`, `EventRuntimeResult`,
  `EventDefinitionRepository`, `EventObjectQuery`, `EventRecordRepository`, `EventRecipientResolver`,
  `EventMatchedObject`, and `EventNotificationPlan`.
- Added JDBC runtime adapters for running definitions, matched object queries, and idempotent event record persistence.
- Added legacy recipient expansion models and `LegacyEventRecipientResolver` for department recursion, role users, direct users, company department users, and All fallback.
- Added Spring auto-configuration for the event package.
- Added `docker/mysql/init/004-event.sql`.
- Fixed `MessageFactory` constructor injection for runtime Spring instantiation with multiple test constructors.
- Updated migration parity docs and README status.

# Validation

- Red test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event test`
  first failed on missing event classes, then failed on missing message repository persistence path.
- Green focused test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event test`
  passed with 8 tests, 0 failures, 0 errors.
- Runtime red/green:
  `docker compose up -d --build backend`
  first failed because Spring could not instantiate `MessageFactory` with multiple public constructors and no `@Autowired` constructor marker; after adding the focused constructor-injection test and annotation, the rebuilt backend started.
- Full backend image build:
  `docker compose up -d --build backend`
  rebuilt all 14 Maven modules and started `fool-service-backend-1`.
- SQL applied:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
- Runtime smoke:
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW TABLES LIKE 'SW_EVT_%'; SHOW TABLES LIKE 'SW_SYS_MSG';"`
  returned `SW_EVT_DEF`, `SW_EVT_EVENT`, and `SW_SYS_MSG`.
  `docker compose logs --tail=160 backend`
  showed `Started Application in 2.09 seconds`.
- Final checks:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event test`
  passed with 8 tests, 0 failures, 0 errors.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis still running.
- Runtime service red/green:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am test`
  first failed on missing event runtime contracts/repositories, then failed on missing `@Autowired` constructor marker for `EventRuntimeService`.
- Green runtime service test:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am test`
  passed with 12 tests, 0 failures, 0 errors.
- Full Maven package:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across the reactor.
- Rebuilt Docker backend:
  `docker compose up -d --build backend`
  completed and `docker compose ps --all` showed backend, frontend, MySQL, and Redis running.
  `docker compose logs --tail=160 backend` showed `Started Application in 1.973 seconds`.
- Post-runtime smoke:
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.
- Recipient resolver red/green:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am test`
  first failed on missing recipient domain classes and `LegacyEventRecipientResolver`, then passed with 14 tests, 0 failures, 0 errors after adding department/role/company/direct-user/All expansion.
- Recipient resolver final verification:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  passed across 15 Maven modules.
  `docker compose up -d --build backend`
  rebuilt the backend image and started `fool-service-backend-1`.
  `docker compose ps --all`
  showed backend, frontend, MySQL, and Redis running.
  `docker compose logs --tail=120 backend`
  showed `Started Application in 1.983 seconds`.
  `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  returned HTTP 200 with `[]`.
  `curl -I --max-time 10 http://localhost:8081/`
  returned HTTP 200 from nginx.
  `python scripts/check_repo_harness.py`
  passed.
  `git diff --check`
  passed.

# Risks

- The scheduled polling loop and application database traversal from `EventMakeService` are not migrated yet.
- Recipient expansion logic is migrated, but persisted recipient relation loading and concrete All-authorized-user source wiring are not migrated yet.
- Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.

# Follow-ups

- Complete event polling/application traversal, persisted recipient relation loading, and All-authorized-user source wiring.
- Continue with `SWRPT01-Soway.Report`.
