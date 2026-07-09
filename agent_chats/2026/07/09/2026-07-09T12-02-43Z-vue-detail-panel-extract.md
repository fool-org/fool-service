# Vue Detail Panel Extract

## Prompt

Continue the Docker/Vue FoolFrame migration, keep commits atomic, maximize
reuse, and control frontend file size.

## Scope

- Extracted the View-first detail, operation, lookup, and child-collection
  rendering from `App.vue` into `ViewDetailPanel.vue`.
- Kept the existing parent-owned workflow state and API actions in `App.vue`;
  the new component receives rendered View metadata and emits existing actions
  back to the parent.
- Updated the frontend source-guard tests so metadata-driven child rendering
  assertions follow the extracted component without relaxing the constraints.
- Updated migration/task state for the frontend reuse slice.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T12-02-43Z-vue-detail-panel-extract.md`

## Validation

- GREEN: `cd frontend && npm test -- --run`
- GREEN: `cd frontend && npm run build`
- GREEN: `python scripts/check_repo_harness.py`

## Skipped Checks

- Full Maven reactor and Docker runtime doctor were not rerun because this is a
  frontend component extraction with unchanged backend/API behavior.

## Risks

- Browser smoke was not rerun in this slice; build-time Vue type checking and
  existing source guards cover the moved metadata-driven panel paths.
