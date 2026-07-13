# Legacy Report Condition Group Icon Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse installed components.

## Scope

- Restore a visible merge-group glyph in the condition header.
- Preserve grouping state, click handling, and availability rules.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-50-00Z-legacy-report-condition-group-icon.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders the merge-group command as
  `glyphicon glyphicon-list` in the condition table header.
- The Vue migration specified `pi-object-group`, but the installed
  `primeicons.css` contains no matching class, leaving an unnamed visual gap.
- The installed icon set exposes `pi-list`, the direct list-shaped equivalent.

## Implementation

- Replaced only `pi-object-group` with `pi-list` on the existing PrimeVue
  button.
- Retained the icon-only title and accessible label, grouping event, disabled
  expression, and all condition state unchanged.
- Added source contracts for both the valid replacement and obsolete-name
  removal.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:cfc317cd441dc7c04fe8eab8dde4e0d3845f0be257fc0e03674a28b36d7eaa38`.
- Authenticated `/view101` condition setup rendered a `pi pi-list` child with a
  nonempty `::before` glyph and approximately 13x13px bounds in desktop and
  390x844 viewports.
- Screenshots show the list glyph occupying the legacy merge-command column
  without overlap. Repository harness validation passed; frontend `/` and
  frontend-proxied `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-condition-group-icon/group-icon-desktop.png`
- `artifacts/runs/20260713-legacy-report-condition-group-icon/group-icon-mobile.png`

## Risks And Follow-Ups

- The command remains disabled until the current grouping validator accepts the
  selection; matching old invalid-selection feedback is a separate behavior.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
