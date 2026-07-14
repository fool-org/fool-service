# Legacy View Timer Cadence

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping changes small and View-first.

## Scope

- Compared `timer.js` registration/counting and `querylistdata.js` interval
  watch with the Vue main View timer lifetime.
- Reused one timer when response metadata keeps the same interval.
- Matched the old one-second due-check/increment sequence.
- Kept the timer across manual Find, paging, and same-interval responses.
- Preserved View-navigation cleanup, interval-change replacement, hidden-chart
  gating, pending-request concurrency, and scheduled page-one reset.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/AutoRefresh.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-51-02Z-legacy-view-timer-cadence.md`

## Validation

- `cd frontend && npm test -- --run AutoRefresh.test.ts payload.test.ts`
  passed: 2 files, 83 tests.
- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Pending Docker rebuild and authorized browser/runtime acceptance.

## Risks And Follow-ups

- Temporarily set View 100's interval from zero to one second, record the
  initial delayed tick and subsequent one-second Nginx timestamps, invoke Find
  between ticks, then restore both metadata rows exactly.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
