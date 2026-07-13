# Legacy Report Output Add Action Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the report output-method Add command's old visual hierarchy.
- Preserve output selection, deduplication, ordering, and report state.
- Keep the action fully visible in the mobile dialog viewport.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-25-00Z-legacy-report-output-add.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` places the report output-method
  `select.form-control#rtp-selecttype(size="10")` in `.col-md-4`, followed by
  a right-aligned arrow button that calls `add()`.
- The old arrow has no primary-button class; the Vue migration had introduced
  a filled primary icon button.

## Implementation

- Kept the existing icon-only Add event and right alignment.
- Switched the command to PrimeVue's secondary outlined small presentation and
  fixed its scoped geometry at 34x34px with zero internal padding.
- Reduced only the mobile output-list minimum height from 220px to 180px so the
  action remains fully visible above the dialog footer.
- Added no component, helper, state, event, DTO, or serializer changes.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:640b532052a9a1939a33598cdb9de3715cb82618c2688bd9986b4dbc4eada312`.
- Authenticated `/view101` report setup rendered the Add command with
  `p-button-secondary`, `p-button-outlined`, and `p-button-sm`; computed width
  and height were both 34px.
- Clicking Add produced exactly one selected `Item ID[原值]` output. At 390x844
  the output-method list computed to 180px and the complete button remained
  above the dialog footer without overlap.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-output-add/output-add-desktop.png`
- `artifacts/runs/20260713-legacy-report-output-add/output-add-mobile.png`

## Risks And Follow-Ups

- The PrimeVue right-arrow icon is retained as the familiar equivalent of the
  old Bootstrap glyphicon; the command behavior and accessible label are
  unchanged.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
