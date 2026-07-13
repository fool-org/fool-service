# Legacy Report Save Button Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the report definition save command's legacy information hierarchy.
- Preserve all report setup, validation, and save-route behavior.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-39-00Z-legacy-report-save-button.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders report Cancel with
  `.btn-default`, Confirm with `.btn-primary`, and `保存报表定义` with
  `.btn-info`.
- The Vue report footer already matched default and primary actions but still
  rendered Save as another outlined secondary action.

## Implementation

- Changed only the existing Save button severity from secondary outlined to
  information.
- Reused PrimeVue's existing semantic action API; no CSS, component, state, or
  request abstraction was added.
- Added a source-level assertion so the three-way footer hierarchy cannot
  silently collapse again.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:6547c8fd454233225b54f2e597042d21e026f8615e11bceaa68ec53e2b5cdda3`.
- Authenticated `/view101` report setup exposed exactly one Save button with
  `p-button-info`; it remained disabled without a name and became enabled after
  selecting an output and entering a report name.
- No Save click was issued, so runtime acceptance did not mutate report data.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-save-button/report-save-info-desktop.png`

## Risks And Follow-Ups

- PrimeVue owns hover, focus, disabled, and contrast states for the information
  severity; this intentionally avoids duplicating button-state CSS.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
