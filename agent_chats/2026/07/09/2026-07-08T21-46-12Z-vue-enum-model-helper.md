# Vue Enum Model Helper

## Prompt

Continue the FoolFrame migration while keeping Vue rendering and editor logic
bound to shared View metadata helpers instead of direct DTO alias reads.

## Scope

- Routed `enumFieldOptions` through the existing `fieldModelId` helper.
- Added a structure regression so `viewShell` does not directly read
  `prpModelId` / `PrpModelId`.
- Kept enum option loading and option selection on the same shared metadata
  path without adding a new abstraction.

## Changed Files

- `frontend/src/viewShell.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-46-12Z-vue-enum-model-helper.md`

## Validation

- RED: `npm test -- payload.test.ts` failed because `viewShell` did not use
  `fieldModelId(field)` and still read direct model-id aliases.
- GREEN: `npm test -- payload.test.ts`
- GREEN: `npm test`
- GREEN: `npm run build`
- GREEN: `git diff --check`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `docker compose build frontend && docker compose up -d frontend`
- GREEN: `curl -fsS http://localhost:8081/` returned bundle
  `index-DWyV273y.js`, and `docker compose ps` showed backend, frontend,
  MySQL, and Redis running.

## Risks

- This is only alias-boundary cleanup. It does not implement custom legacy
  widgets such as `ComboBox`, `SelectLable`, or `DropTextBox`.
