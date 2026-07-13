# Legacy Report Output Toolbar State Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the old output toolbar's availability at empty and boundary states.
- Match the old selected-column sort label exactly.
- Preserve request-pending protection and metadata-driven report state.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-40-00Z-legacy-report-output-toolbar-state.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders all six move, remove, and sort
  buttons without `disabled` attributes.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` performs selection
  and boundary checks inside `up`, `down`, `del`, `asc`, `desc`, and `casc`, so
  empty or boundary clicks are available but have no effect.
- The same script appends `[升序]` and `[降序]` directly to the stored output
  name, without an intervening space.

## Implementation

- Removed selected-output and boundary conditions from the six button disabled
  expressions while retaining the shared request-pending `disabled` prop.
- Reused the existing component handlers and `reportOutputs.ts` pure-function
  guards; no alternate state or event path was added.
- Removed the Vue-only leading spaces from both sort-label suffixes.

## Validation

- Focused output and source-contract suites: 85 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:57531a669599c959fd011357a691992221d287867931a009db0532706bbd1875`.
- Authenticated `/view101` report setup showed all six toolbar commands enabled
  with no selected output. Clicking all six left the selected output count at
  zero.
- After adding one output, Up and Down remained enabled and preserved the one
  boundary item. Ascending rendered `Item ID[原值][升序]`; cancel sorting
  restored `Item ID[原值]`; delete returned the list to zero items.
- Desktop and 390x844 screenshots show the available empty-state toolbar with
  no overlap. Repository harness validation passed; frontend `/` and
  frontend-proxied `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-output-toolbar/output-toolbar-desktop.png`
- `artifacts/runs/20260713-legacy-report-output-toolbar/output-toolbar-mobile.png`

## Risks And Follow-Ups

- Buttons remain disabled only while a parent report request is pending; the
  old Angular implementation did not model that asynchronous safety state.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
