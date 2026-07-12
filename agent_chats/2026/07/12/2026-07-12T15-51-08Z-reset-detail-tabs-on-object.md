# Reset Detail Tabs On Object

## Prompt

Continue aligning old page interaction across SPA transitions, keep state
handling reused and small, and commit every behavior atomically.

## Scope

- Compare old `itemreadonly.js` first-tab initialization with SPA detail
  object changes.
- Reset each object/new context to the first metadata child group.
- Close a select-existing candidate dialog inherited from the prior object.
- Preserve user-selected tabs while staying on the same object.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-51-08Z-reset-detail-tabs-on-object.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed; `ViewDetailPanel` is 72.84 kB.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:79f46df2327cb3f45d18923a4f128755989fe44d36d8e028cda11a50279f923c`.
- `python scripts/runtime_doctor.py`: passed, including detail deep links,
  read-item metadata, object data, child metadata, auth, report, and messages.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- Old `itemreadonly.js` calls `$('#detail-tab a:first').tab('show')` on every
  detail page load.
- Vue's shared detail component survives message/history transitions and
  previously retained the prior object's tab and candidate picker when group
  keys matched.
- The existing object/context watcher now resets both values; no extra watcher
  or duplicated detail state was added.

## Risks And Follow-Ups

- Current-build authenticated cross-object tab/picker browser confirmation
  remains pending fresh authorization to read and fill the current CAPTCHA.
