# Child Detail View Routing

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Read child-group `detailViewId` / `DetailViewId` through one shared helper.
- Keep inline child editors only when no detail View is configured.
- Render configured child rows as read-only values with the old deep-detail
  route shape.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-26-49Z-child-detail-view-routing.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 132 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- The `/view{ViewId}/{ObjectId}` deep-detail route remains covered by the
  Docker runtime doctor from the preceding deployed frontend validation.
- No new Docker image was required because this slice changes only the
  metadata-driven choice between inline rendering and the existing route.

## Risks And Follow-Ups

- Final authenticated browser acceptance needs a seeded child group with a
  nonzero `DetailViewId` to click the visible Edit link.
- Operation-result message and return-route behavior remains the next parity
  slice.
