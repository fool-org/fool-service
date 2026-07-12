# Restore Item View Tabs

## Prompt

Continue aligning Vue layout, style, and interaction behavior with FoolFrame,
allowing visual polish while preserving the old workflow, and commit each
change atomically.

## Scope

- Compare old `item.jade` with the current `/itemview:id` schema-only panel.
- Restore `DetailViews` as interactive tabs with metadata field table headings.
- Reuse the existing detail collection Tabs/Table rather than adding a second
  schema-only implementation.
- Keep schema-only routes free of Add, picker, edit, delete, and object-data
  requests or object-row empty-state output.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T23-14-55Z-restore-itemview-tabs.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 81 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 152 tests passed and
  the production build completed; the `ViewDetailPanel` chunk decreased from
  72.93 kB to 72.78 kB.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  rebuilt and restarted the frontend image.
- `python scripts/runtime_doctor.py`: passed, including `/itemview100`,
  `getreaditemview.DetailViews`, auth, data, report, and message checks.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `curl http://localhost:8081/itemview100`: HTTP 200 with Vue app HTML.

## Source Evidence

- FoolFrame `item.jade` renders `view.DetailViews` through Bootstrap tabs and a
  field-heading table for each tab.
- The prior Vue schema-only branch flattened every child View into a static
  `groupTitle + comma-joined field names` grid.
- Old `item.jade` emits only the metadata headings for this route, so the
  schema-only table body is omitted rather than showing an object-row empty
  state.
- `loadLegacyItemView` still calls only `getreaditemview`; no data DTO or empty
  object query is introduced.

## Risks And Follow-Ups

- Authenticated visual inspection of `/itemview100` remains pending fresh local
  CAPTCHA authorization.
