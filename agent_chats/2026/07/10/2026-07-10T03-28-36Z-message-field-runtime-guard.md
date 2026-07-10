# Legacy Message Field Runtime Guard

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Matched the fields read by legacy `public/javascripts/app/message.js` to the
  migrated `getmsg` response.
- Serialized Pascal `GernerationTime` as the legacy `/Date(ms)/` value while
  retaining the existing camel-case `LocalDateTime` field for Vue consumers.
- Reused the current message repository, controller route, Vue proxy, and
  runtime doctor rather than adding another message flow.
- Seeded one deterministic runtime message so the guard cannot pass on an
  empty `Messages` list.

## Changed Files
- `business-application/src/main/java/org/fool/framework/application/api/MessageController.java`
- `business-application/src/test/java/org/fool/framework/application/api/MessageControllerTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T03-28-36Z-message-field-runtime-guard.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 47 tests.
- Docker Java 17 `MessageControllerTest` passed: 3 tests.
- `docker compose up -d --build backend` completed successfully.
- `python scripts/runtime_doctor.py` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- `mysql:runtime-message-seed` inserted or reset the deterministic
  `SW_SYS_MSG` row for user `admin`.
- `message:getmsg` passed through `http://localhost:8081/api/v1/message/getmsg`
  with `MessageID`, `/Date(ms)/` `GernerationTime`, `MessageContent`,
  `ResultView`, and `ResultKey`.
- Compose reported backend, frontend, MySQL, and Redis running; MySQL and Redis
  were healthy.

## Risks
- The epoch conversion interprets timezone-free database timestamps in the
  backend process default timezone, matching the current runtime convention.

## Follow-ups
- Continue the remaining migration from legacy View-rendered pages and their
  actual DTO/data dependencies.
