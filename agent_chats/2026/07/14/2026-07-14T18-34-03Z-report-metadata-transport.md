# Report Metadata Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the report entry from `view.jade` into `mkreport.initquery` before
  comparing the current Vue report setup.
- Restored the old success-only transport path: a report-column network or
  non-2xx failure leaves the main View unchanged and the report setup closed.
- Reused the existing transport classifier and `silentTransport` action option.
- Kept response-backed errors, successful report setup, View-derived columns,
  routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-34-03Z-report-metadata-transport.md`

## Validation

- `cd frontend && npm test -- --run ViewReportPanel.test.ts payload.test.ts`
  (2 files, 85 tests passed)
- `cd frontend && npm test` (19 files, 192 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized browser acceptance with local `admin/admin` login:
  `/view101` rendered all 4 seeded rows. With the backend stopped, clicking
  `统计` left the rows visible and produced zero report/error dialogs. After
  backend restart, the same command opened `生成报表` with Item ID and Item Name
  candidates from the current View.
- Nginx recorded the failed `POST /api/v1/report/getmkqview` as `502`, then the
  recovery request as HTTP 200.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:3dffceff8cd5975770354e62affd317b0107531d9fa23bd73e1cd7f2b4d4f5f8`.
- Database proof remained `SW_SYS_VIEW(100).VIEW_DEFAULT=102`, both View refresh
  intervals `0`, `market_order=8`, and `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
