# Report Output Selector

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change and controlling
  file size and reuse.

## Scope

- Compare the report output tab with old `view.jade` and `mkreport.js`.
- Restore candidate-column, output-method, and selected-column list interaction
  using native `select size=10` controls.
- Allow the same View metadata field with different output methods while
  preventing exact duplicates.
- Restore add, up/down, delete, ascending, descending, and cancel-sort actions.
- Keep selected outputs as final `ReportCol[]` request state rather than three
  parallel maps in the parent report component.
- Extract pure output mutation helpers and a focused test suite; reduce the
  parent report component from 442 to 346 lines.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/reportOutputs.ts`
- `frontend/src/reportOutputs.test.ts`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-15-29Z-report-output-selector.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - Native selects keep the report chunk at 16.46 kB rather than the rejected
    PrimeVue Listbox draft's 44.76 kB.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks, including report
  model/run/save payload aliases.

## Skipped Or Downgraded Checks

- Authenticated report interaction remains gated by the current captcha until
  fresh user authorization is received.

## Risks And Follow-Ups

- Browser acceptance must prove three-list selection, duplicate output methods,
  ordering controls, request output order, report execution, and mobile stacking.
