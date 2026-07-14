# Report Generation Transport Handling

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced initial generation and result paging through the old
  `MakeReportController` / `ShowReportController` before changing Vue.
- Restored success-only `mkrpt` transport handling: initial failure leaves both
  report dialogs closed; paging failure retains the current result.
- Extracted one component-local success-only action helper and reused it for
  report metadata and generation instead of duplicating transport handling.
- Kept response-backed errors, successful generation, paging boundaries,
  View-derived state, routes, payloads, and DTO bindings unchanged.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-39-29Z-report-generation-transport.md`

## Validation

- `cd frontend && npm test -- --run ViewReportPanel.test.ts payload.test.ts`
  (2 files, 86 tests passed)
- `cd frontend && npm test` (19 files, 193 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Authorized browser acceptance with local `admin/admin` login:
  `/view101` report setup loaded before backend stop. Initial generation
  eventually settled with zero setup/result/error dialogs and retained all 4
  list rows. After backend restart, selecting Item ID / original value opened
  `报表结果 共1页 当前第1页` with all four values.
- Nginx recorded the failed `POST /api/v1/report/mkrpt` as HTTP 504 after its
  upstream connect timeout, then the recovery request as HTTP 200.
- `docker compose ps -a` confirmed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Frontend container/image matched
  `sha256:08bc0b9824639e4de05f1b782e2774105ec6fd897c0ac8531b3724c05ec014eb`.
- Database proof remained `SW_SYS_VIEW(100).VIEW_DEFAULT=102`, both View refresh
  intervals `0`, `market_order=8`, and `market_order_item=4`.
- `python scripts/runtime_doctor.py` (all 67 checks passed)

## Risks And Follow-ups

- Paging transport failure should retain existing results; proving it requires
  a temporary second report page and remains source-contract-covered in this
  slice because the seeded report has one page.
- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
