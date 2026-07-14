# Legacy Chart Find Page

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
with small reusable changes and View-first data binding.

## Scope

- Compared `view.jade`, `viewWithChart.jade`, `querylistdata.js`, and
  `viewWithChart.js` manual Find dispatch.
- Kept the current page only for a chart View's manual Find.
- Preserved plain View Find and scheduled-refresh page-one resets.
- Added no request type, payload field, route, DTO binding, or component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-44-24Z-legacy-chart-find-page.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` passed: 1 file, 82 tests.
- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Pending Docker rebuild and authorized browser/runtime acceptance.

## Risks And Follow-ups

- Add the minimum temporary rows needed for View 100 page two, verify chart
  Find retains Page 2 and plain View Find returns to Page 1, then remove those
  rows and prove the original count is restored.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
