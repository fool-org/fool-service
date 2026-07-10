# Typed View Save Workflow

## Prompt
- Continue the Docker/FoolFrame/Vue migration with a View-first frontend,
  maximum reuse, controlled file size, and atomic commits.

## Scope
- Traced detail, initialize, create, update, child mutation, and operation
  requests from rendered View metadata through the frontend payload builders.
- Removed console-era request mirrors for object ids, View ids, operation ids,
  owner fields, and JSON payload text.
- Changed save payload builders to accept typed `SaveKeypair[]` and
  `SaveItemProperty[]` values instead of parsing textarea JSON.
- Built parent and child save requests directly from the active detail View,
  selected object, rendered fields, and current drafts.
- Kept only the owner context that must survive between initializing and
  submitting a new child object.
- Reduced `useViewDataWorkflow` ownership to list, read-item, and detail View
  ids instead of synchronizing state for deleted API panels.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-31-27Z-typed-view-save-workflow.md`

## Validation
- `cd frontend && npm test -- --run` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend.
- `python scripts/runtime_doctor.py` passed all checks.

## Runtime Evidence
- Runtime doctor executed legacy `initnew`, `savenewobj`, `saveobj`, child
  add/update/delete, `runoperation`, and old Web aliases through the Vue proxy.
- `App.vue` is 1122 lines, down from 1207 before this slice.
- The built JavaScript is 135.14 kB, down from 136.57 kB before this slice.

## Risks
- Signed-in browser interaction still requires explicit approval to submit the
  local captcha-backed Docker admin login; runtime request behavior is covered
  by the Docker doctor in the meantime.

## Follow-ups
- Complete signed-in desktop/mobile browser inspection after captcha approval.
- Continue only concrete FoolFrame parity gaps proven by a source/runtime audit.
