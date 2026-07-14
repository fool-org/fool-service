# Main View Error Dialog

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `querylistdata.js` response errors into `showerror.showerrormsg` before
  comparing the current main View outlet.
- Replaced the main View's inline error Message with the old modal interaction.
- Extracted the existing detail error markup into `LegacyErrorDialog` and reused
  it in both main View and detail surfaces.
- Kept login's error-code/CAPTCHA dialog separate and left transport handling,
  View/Data projection, routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/LegacyErrorDialog.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.test.ts`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-25-04Z-main-view-error-dialog.md`

## Validation

- `cd frontend && npm test -- --run ViewListPanel.test.ts ViewDetailPanel.test.ts payload.test.ts`
  (3 files, 88 tests passed)
- `cd frontend && npm test` (19 files, 191 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized browser acceptance with local `admin/admin` login:
  `/view999999` opened one `发生错误` dialog containing `发生未知错误` and one
  `关闭` command. Dismissal removed the dialog, then `/view100` rendered all 8
  seeded records.
- Nginx recorded `POST /api/v1/view/getlistview` for `/view999999` as HTTP 200
  with a 65-byte business-error response.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:c7d7b6939aae34405b3a6f8dd0f9455f42cb59757923222e7b66c16b33f6d8da`.
- Database proof remained `SW_SYS_VIEW(100).VIEW_DEFAULT=102`, both View refresh
  intervals `0`, `market_order=8`, and `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
