# Legacy Message `getmsg`

## Prompt

- Continue the active migration goal: Docker environment, FoolFrame parity,
  Vue frontend, and timely atomic commits.
- Advance a concrete FoolFrame slice after the current progress check.

## Scope

- Migrated the legacy `HandlerGetMessage` surface to
  `POST /api/v1/message/getmsg`.
- Reused the existing auth token flow to resolve the current user.
- Reused `SW_SYS_MSG` and `EventMessageRepository`; added only the generated
  message query and push-state update needed by the legacy handler.
- Exposed the route in the Vue operator console through the existing `/api`
  proxy.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/EventMessageRepository.java`
- `fool-event/src/main/java/org/fool/framework/event/JdbcEventMessageRepository.java`
- `business-application/src/main/java/org/fool/framework/application/api/MessageController.java`
- `business-application/src/test/java/org/fool/framework/application/api/MessageControllerTest.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `frontend/src/api.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  - `npm test` failed on the new Vue message polling assertion before the
    panel existed.
  - Docker Maven failed compiling the new event repository contract before
    `findGeneratedForUser` / `markPushed` existed.
- GREEN:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event,business-application -am -DfailIfNoTests=false test`
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
  - `docker compose up -d --build`

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl http://localhost:8080/test` returned the Docker seed rows.
- `curl http://localhost:8081/` returned the Vue index HTML.
- Inserted `SW_SYS_MSG.MSG_ID=00000000-0000-0000-0000-000000009901` with
  `MSG_STATE=0`, called `http://localhost:8080/api/v1/message/getmsg`, and
  verified MySQL updated the row to `MSG_STATE=1`.
- Inserted `SW_SYS_MSG.MSG_ID=00000000-0000-0000-0000-000000009902` with
  `MSG_STATE=0`, called `http://localhost:8081/api/v1/message/getmsg`, and
  verified MySQL updated the row to `MSG_STATE=1` through the frontend proxy.

## Skipped Checks

- No browser screenshot was captured; this slice is a small form/table addition
  covered by Vue source tests and production build.

## Risks / Follow-ups

- `getnotify`, read/deal transitions, timeout handling, and richer notification
  count behavior remain future event-message work.
