# Restore Lookup Clear

## Prompt

Keep old interaction logic aligned while allowing visual modernization, bind
fields through View metadata, and commit each behavior atomically.

## Scope

- Initialize BusinessObject lookup text from the View-provided formatted value.
- Clear the draft foreign-key id when the user empties the lookup.
- Reset lookup text when navigation replaces the rendered View field.
- Preserve candidate querying and selected-id writeback.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-24-00Z-restore-lookup-clear.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including
  lookup initialization, clear-event writeback, field-change reset, and
  placeholder removal.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including View-first
  detail data, BusinessObject `inputquery`, and save routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `setextype.js` creates the BusinessObject input with the current formatted
  text and updates its property id only when a candidate is selected.
- `savetext.js` explicitly saves empty text and empty object id when the lookup
  input is cleared.
- Docker metadata exposes `OrderDetail.customer` as an editable
  `PropertyType.BusinessObject`, so this path affects the seeded Vue workflow.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
