# Shared View Row Table

## Prompt

Continue the FoolFrame migration and keep the Vue frontend View-first. The user
called out that the flow should inspect the rendered View page first, then query
data for that View; binding rendered rows to concrete business DTO fields is the
wrong direction.

## Scope

- Compare FoolFrame's `querylistdata` page rendering path against the current
  Vue workflow.
- Keep the fix frontend-only and small.
- Remove duplicate table rendering that could drift back toward DTO-field row
  assumptions.

## Changes

- Extended `ListDataTable` with reusable default-action controls:
  `defaultActionLabel` and `showDefaultAction`.
- Replaced the select-existing child candidate table with `ListDataTable` and a
  `Select` default action.
- Replaced the API-tool query result table with `ListDataTable` and no action
  column.
- Removed direct `rowValue` table-cell rendering from `App.vue`; row cells now
  flow through the shared View/Items metadata renderer.
- Updated raw-source tests to lock this rendering path.
- Updated `tasks.md` and migration parity notes.

## Validation

- Passed: `cd frontend && npm test -- --run` (`60 passed`).
- Passed: `cd frontend && npm run build`.
- Passed: `docker compose up -d --build frontend`.

## Runtime Evidence

- `docker compose up -d --build frontend` rebuilt the frontend image and, via
  Compose dependencies, also rebuilt the backend image successfully.
- `docker compose ps` shows backend, frontend, MySQL, and Redis up; MySQL/Redis
  are healthy.
- Passed: `curl -fsS http://localhost:8081/` returned the rebuilt Vue app HTML
  with `/assets/index-BSSpCzLu.js`.
- Passed: `curl -fsS http://localhost:8080/test` returned seeded order rows.

## Risks

- This slice centralizes Vue row-table rendering, but it does not add browser
  screenshot evidence. Runtime proof is HTTP/container level plus unit/build
  validation.
