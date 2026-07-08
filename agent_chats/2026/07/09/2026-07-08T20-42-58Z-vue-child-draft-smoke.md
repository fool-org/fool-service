# Vue Child Draft Smoke

## Prompt

Continue the Docker/Vue FoolFrame migration, keep the frontend usable, and
avoid binding rendered pages to concrete business DTOs.

## Scope

- Verified the Docker Vue first screen in Chrome and reproduced a runtime
  console error: `Cannot read properties of undefined (reading 'itemId')`.
- Replaced direct child draft map indexing in the template with explicit
  `model-value` / `update:model-value` bindings.
- Added shared draft helpers so missing child add/update drafts read as empty
  strings and writes initialize from the rendered View group/item metadata.
- Kept child collection editors tied to `getreaditemview.DetailViews` fields;
  no business DTO fallback columns were added.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-42-58Z-vue-child-draft-smoke.md`

## Validation

- GREEN: `cd frontend && npm test`
- GREEN: `cd frontend && npm run build`
- GREEN: `docker compose up -d --build frontend`
- GREEN: `python scripts/runtime_doctor.py`
- GREEN: Chrome smoke at `http://localhost:8081/?smoke=fix-draft`
  loaded `/assets/index-C7xKCtzV.js`, rendered 8 View rows, rendered the
  detail child editor, and returned no browser warning/error logs.

## Risks

- This fixes the child draft render crash only. It does not change the current
  Docker seed data values shown in the first screen.
