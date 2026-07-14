# Legacy Report Export Placeholders

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare the old report-result command row with the Vue result dialog.
- Restore the two visible export placeholders with their original no-op behavior.
- Do not invent an export request, DTO, download path, state, or dependency.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-15-40Z-legacy-report-export-placeholders.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders `导出当前页` and `导出全部`
  immediately after the `前一页` / `下一页` buttons.
- Neither button has `ng-click`, and `mkreport.js` defines no export handler.
- The deployed Vue baseline rendered only the working paging pair.

## Implementation

- Appended two handler-free PrimeVue buttons to the existing report-result
  extra-small command group.
- Added a source contract proving neither placeholder has an `@click` handler.
- Allowed the existing heading row to wrap the intact group below its summary
  when both cannot fit, after a 390px screenshot exposed overlap.
- Reused the current shared button-group geometry; no component, helper,
  request, DTO branch, state, dependency, or media-specific rule was added.

## Validation

- Focused `npm test -- src/payload.test.ts`: 82 tests passed before the final
  responsive rule; the full suite below covers the final state.
- Full `npm test`: 14 files and 178 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` passed; the service was attached with
  `docker compose up -d --no-deps --force-recreate frontend` to image
  `sha256:b6016cd9be7c542565a42413dfe13716f388f3245a1f363d7f50e572a54d6dec`.
- Backend `/test` returned 465 bytes; MySQL/Redis were healthy and `db-migrate`
  remained `Exited (0)`.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Authenticated desktop `/view101` rendered the command labels in old order in
  one 225px group and retained a 1440px document width.
- At 390x844 the page summary ended at y=268.73 and the command group began at
  y=276.73, so the four buttons wrapped as one non-overlapping 225px group. The
  document scroll width remained exactly 390px.
- Clicking `导出当前页` and `导出全部` left the result dialog text and URL
  unchanged, matching their handler-free old-template behavior.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-export-placeholders/report-export-mobile.png`

## Skipped Checks And Risks

- The controls intentionally remain no-op because that is the old template and
  controller contract. Functional export requires a separately specified API.
- The full old FoolFrame application was not booted; the checked-in Jade and
  controller sources supplied the old View/interaction contract.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
