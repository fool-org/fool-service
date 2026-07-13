# Legacy Report Result Button Group Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the report result paging commands' legacy grouped presentation.
- Preserve report generation, paging boundaries, and Return behavior.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-44-00Z-legacy-report-result-group.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` places Previous, Next, and two
  eventless export placeholders in `.btn-group.btn-group-xs.pull-right`.
- Bootstrap joins adjacent group borders, retains only the outer corner radii,
  and uses compact padding for this result-toolbar group.
- The Vue result page kept functional Previous/Next commands but rendered them
  as two independent buttons with an 8px gap.

## Implementation

- Added `legacy-button-group-xs` to the existing result action container.
- Reused one global utility for contiguous borders, 22px height, compact
  padding, and outer 3px radii; no report-specific component was introduced.
- Removed the redundant PrimeVue `size="small"` props because the shared group
  owns stable geometry.
- Kept 12px text as a readability improvement over the old customized 9px
  Bootstrap build.
- Continued omitting eventless export placeholders rather than exposing inert
  commands.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:b9984ebdc48073a287c57e1ad23ee244b515ab38ba21a2010b9a968bf8de980e`.
- Authenticated `/view101` runtime generated a one-page report and exposed
  exactly one grouped Previous/Next pair, both disabled at the page boundary.
- Desktop and 390x844 screenshots show a contiguous right-aligned group without
  overlap; Return restored the report setup tabs and removed result controls.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-result-group/report-result-group-desktop.png`
- `artifacts/runs/20260713-legacy-report-result-group/report-result-group-mobile.png`

## Risks And Follow-Ups

- The utility currently has one consumer but is intentionally generic for old
  extra-small button groups encountered in later template parity work.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
