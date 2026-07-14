# Candidate View Transport State

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the collection picker from the rendered child View into
  `detailview.js initQueryView()` before changing Vue.
- Reused the shared action options to preserve pending state only for a silent
  candidate View transport failure.
- Retained View-before-data ordering, linked View ids, candidate columns,
  candidate query state, payloads, routes, DTOs, and components.
- Added no component, helper, state owner, request type, or dependency.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-07-00Z-candidate-view-transport.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (84/84 passed)
- `cd frontend && npm test` (19 files, 195 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built and deployed exact implementation commit `7f73f8cd` as image
  `sha256:79cabbce9c94555003e6ca015323c1a6277566cf2fa25d695c835c33edbb3965`;
  the running container matched it and served `index-f7c4WPnZ.js`.
- Authorized browser acceptance read the local CAPTCHA and used `admin/admin`.
  A deterministic HTTP 502 limited to the child candidate `getlistview` left
  the non-dismissible `加载中 / 正在加载，请稍后....` dialog visible without
  shared/transport feedback. The captured request sequence contained one
  `getlistview` and zero `querydata` calls.
- Removing the failure and reloading returned HTTP 200 from `getlistview`,
  opened `选择 Items`, then returned HTTP 200 from `querydata` only after
  `查找`; the picker rendered `共4条记录`. Logout returned HTTP 200.
- `docker compose ps -a` showed backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Final database evidence retained six rows in each View table, eight orders,
  four order items, and unchanged order 1001 values.
- `python scripts/runtime_doctor.py` (67/67 passed)

## Risks And Follow-ups

- The forced 502 was a deterministic browser interception, so Nginx recorded
  the recovered HTTP 200 requests; the browser response supplied 502 evidence.
- The deliberately pending loader requires a reload before retry, matching the
  old controller rather than adding a new recovery affordance.
- Unrelated README/POM, agent-session, `docs/superpowers/`, and `fool-agent/`
  work remains untouched and excluded from this delivery.
