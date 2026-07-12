# Bootstrap Primary Theme

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the rendered signed-out page and current theme tokens with old
  Bootstrap primary button colors.
- Replace the Indigo semantic palette with exact old primary/hover/active blue
  states (`#337ab7`, `#286090`, `#204d74`).
- Reuse the palette for form focus states, the shell brand mark, and map
  markers; remove the invented purple brand gradient and shadow.
- Preserve neutral surfaces, status colors, and multi-series chart colors.

## Changed Files

- `frontend/src/theme.ts`
- `frontend/src/style.css`
- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-57-56Z-bootstrap-primary-theme.md`

## Validation

- `cd frontend && npm test -- --run`
  - 141 tests passed across 8 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- Before the theme change, a captcha-masked `1280x720` browser inspection
  proved the unframed 240px login form had no horizontal overflow and exposed
  the remaining purple primary button mismatch.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.
- A deployed captcha-masked `1280x720` browser recheck measured a 240px centered
  form, `scrollWidth=1280`, white page background `rgb(255, 255, 255)`, and old
  Bootstrap primary button background `rgb(51, 122, 183)`; the masked visual
  inspection showed no overlap or framing regression.

## Skipped Or Downgraded Checks

- The browser captcha was masked and was neither read nor submitted.
- The current in-app browser session exposes a fixed `1280x720` viewport, so
  `390x844` acceptance remains pending in the final authenticated browser run.

## Risks And Follow-Ups

- Authenticated browser acceptance must prove shared primary/focus states,
  shell branding, map markers, and mobile contrast after login.
