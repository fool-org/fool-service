# Agent Chat Providers and Vue Workspace

## Prompt

配置 DeepSeek、OpenAI provider，并集成聊天页面。

## Scope

- Reused the existing ordered `fool-agent` session and draft workflow.
- Added one server-side OpenAI-compatible Chat Completions client for DeepSeek
  and OpenAI rather than introducing provider SDK dependencies.
- Added provider discovery/selection and kept API keys out of browser and
  session responses.
- Added the authenticated Vue `/agent` workspace and shell navigation entry.
- Preserved deterministic local replies as an explicit fallback.

## Changes

- `fool-agent`
  - Added `AgentProviderProperties` defaults and environment binding.
  - Added `AgentChatProviderService`, provider catalog, bounded conversation
    history, controlled system/draft context, error handling, and local fallback.
  - Added `provider` to message requests and provider/model evidence to turn
    results.
  - Added focused provider request/response and fallback tests.
- `business-application/src/main/resources/application-docker.yml` and
  `docker-compose.yml`
  - Added DeepSeek/OpenAI keys, base URLs, models, and default-provider settings.
- `frontend`
  - Added `AgentChatPage.vue`, `/agent` routing, desktop/mobile navigation,
    provider selection, ordered capability progression, conversation history,
    and draft validation display.
  - Added GET API support, agent contract types, and focused source/API tests.
- Updated `README.md`, `docs/agent-sessions.md`, and `tasks.md`.

## Validation

- Passed backend module tests:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-agent -am -DfailIfNoTests=false test`.
- Passed application package assembly:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl business-application -am -DskipTests package`.
- Passed frontend tests and build:
  `cd frontend && npm test && npm run build` (`21` files, `222` tests).
- Passed `python scripts/check_repo_harness.py` after the final evidence update.
- Passed `git diff --check` after the final evidence update.

## Runtime Evidence

- Compose recreated the backend/frontend with the new environment contract;
  MySQL and Redis were healthy and `db-migrate` was `Exited (0)`.
- `GET /api/v1/agent/providers` returned:
  - DeepSeek / `deepseek-v4-flash` / `configured=false`.
  - OpenAI / `gpt-5-mini` / `configured=true` / `defaultProvider=true`.
- Live OpenAI smoke session
  `f9c10bdb-2570-472c-9fd3-335109a9cce3` returned `code=0`,
  `provider=openai`, `model=gpt-5-mini`, `draftRisk=low-read-only`, and three
  persisted messages.
- Explicit local fallback smoke returned `provider=local`,
  `model=deterministic`, and three messages.
- The frontend proxy returned the same provider catalog and `GET /agent`
  returned the Vue application shell.

## Skipped or Downgraded Checks

- `docker compose up -d --build backend frontend` was stopped after Docker Hub
  base-image metadata remained unavailable. For runtime validation only, the
  newly packaged JAR and built Vue assets were copied into the existing local
  images, committed, and recreated through Compose. The checked-in Dockerfiles
  and Compose build definitions were not altered by this workaround.
- The in-app Browser skill could not initialize and repeatedly returned
  `Cannot redefine property: process`; no screenshot or click-through browser
  evidence was produced. Vue route/source tests, production build, proxy HTTP,
  and `/agent` SPA fallback checks passed instead.
- `python scripts/runtime_doctor.py` passed Compose, database, frontend route,
  auth, list/detail/data/report, and backend health checks, but returned four
  failures outside this change:
  `view:getreaditemview-detailviews`, `message:getmsg`,
  `message:getmsg-legacy-web-route`, and `message:getnotify`.

## Risks

- DeepSeek was contract-tested with the shared compatible client but not called
  live because `DEEPSEEK_API_KEY` was not set in this environment.
- A standard clean Docker image rebuild still needs to be rerun when registry
  metadata access is available.
- Visual browser acceptance remains pending until the Browser plugin bootstrap
  issue is resolved.

## Follow-Ups

- Set `DEEPSEEK_API_KEY` and run one live DeepSeek message smoke when available.
- Rerun the standard Docker build and browser click-through after their external
  prerequisites recover.
- Triage the four unrelated runtime-doctor migration failures separately.
