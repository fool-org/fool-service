# Report Dialog Timing

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep report rendering driven by loaded View
metadata rather than business DTO assumptions.

## Scope

- Restore the two-stage report dialog timing from `mkreport.js`.
- Keep the existing View-driven report model and request builders unchanged.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-32-08Z-report-dialog-timing.md`

## Legacy Evidence

- `mkreport.initquery` opens `#rptdiag` only after `/report/mkqview` succeeds.
- `MakeReportController.mkrpt` hides `#rptdiag` before requesting report data,
  then its callback opens `#rptresultdiag` after the response.

## Implementation

- The setup dialog remains unmounted while report View metadata is loading.
- Confirm unmounts the setup dialog while `mkrpt` is in flight and remounts it
  as the result dialog after success.
- Failed requests retain the existing Vue error feedback and restore setup.
- Added two local booleans only; no new component, helper, or dependency was
  introduced. Report columns, filters, and payloads remain View-driven.

## Validation

- Focused payload contract: 82 tests passed after correcting the initial
  frontend-relative test path.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend-only Docker build and forced recreation passed. Deployed image id:
  `sha256:83949eb631a4a6aa54f740e0b4bbf86083ea967068c083f84cd9feab5b4268f4`.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Backend `/test`, frontend `/`, Compose services, healthy MySQL/Redis, and
  successful `db-migrate` exit state were verified.
- Temporarily changed runtime View 100 from `viewWithChart` to the default list
  template and restarted the backend solely for browser timing verification.
- With the backend paused, report-model loading showed zero dialogs; after
  unpause, exactly one setup dialog appeared.
- With the backend paused again, Confirm showed zero dialogs while `mkrpt` was
  in flight; after unpause, exactly one result dialog with Return appeared.
- Restored View 100 to `VIEW_FILE=990001`, restarted the backend, and verified
  the original Chart tab and 11 rendered table rows returned with no Statistics
  command.

## Runtime Evidence

- `artifacts/runs/20260714-report-dialog-timing/report-result.png`

## Risks And Follow-Ups

- The seeded runtime normally uses `viewWithChart`, which intentionally omits
  the Statistics command. The temporary database change was restored and is
  not represented in source or seed files.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this frontend-only interaction and no
  fresh CAPTCHA was generated.
