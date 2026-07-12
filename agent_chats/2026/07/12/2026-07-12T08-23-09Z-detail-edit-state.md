# Detail Edit State

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Respect `querydatadetail.canEdit` / `CanEdit` instead of treating every
  selected object as editable.
- Render existing details read-only until the user enters edit mode.
- Start new details in edit mode and return successful reloads to read-only.
- Block View operations and hide child write controls while the main detail is
  not in the matching edit state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-23-09Z-detail-edit-state.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 131 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose up -d --no-deps frontend`
  - The rebuilt frontend image was deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Compose, auth, View-first detail/create/save/operation, report, and message
    checks passed.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- The Docker frontend served the rebuilt Vue bundle and the runtime doctor
  passed the legacy detail, create, save, and operation routes.
- Browser navigation reached the fresh local login page. Visible detail-state
  replay was not attempted because the new CAPTCHA had no current explicit
  solve permission.

## Risks And Follow-Ups

- Final authenticated browser acceptance must verify Edit -> Save -> read-only
  transitions and disabled operations on desktop and 390x844 layouts.
- Child `DetailViewId` navigation and operation-result feedback remain separate
  parity slices.
