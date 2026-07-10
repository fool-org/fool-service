# Detail Title View Metadata

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Avoid business DTO binding and keep code reuse/file size under control.

## Scope

- Compared old `detailView.jade`, whose heading uses View/Data names, with the
  current Vue detail panel.
- Added the FoolFrame Pascal `ViewName` alias to the shared view display-name
  helper.
- Rendered the Vue detail panel heading from the loaded read-item View metadata
  instead of a hard-coded `Detail` label.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/10/2026-07-10T02-23-19Z-detail-title-view-metadata.md`

## Validation

- `cd frontend && npm test` passed: 7 files, 129 tests.
- `cd frontend && npm run build` passed.

## Skipped

- Docker image rebuild/runtime doctor was not rerun for this slice; the change
  is frontend source-only and was validated with the Vue test/build gates.
