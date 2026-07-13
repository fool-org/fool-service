# Legacy Detail Toolbar Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the detail page's contiguous default command group.
- Preserve edit, save, and View-operation state transitions.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-54-00Z-legacy-detail-toolbar.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` places Edit, Save, and every View
  operation in one `.btn-group`; all commands use `.btn-default`.
- The Vue detail toolbar rendered Edit and Save as separate primary buttons,
  left operations as outlined secondary buttons, and inserted 8px gaps.

## Implementation

- Marked Edit and Save as secondary outlined commands, matching existing View
  operation presentation.
- Applied the shared `legacy-button-group` class to the existing toolbar.
- Refactored the existing extra-small group CSS into a reusable base group plus
  its `xs` sizing variant; no duplicate detail-toolbar button rules were added.
- Kept PrimeVue icons as the existing usability improvement over glyphicons.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:f140baa3288d18cc85427d40053d2723b37bfbea2ff6023d4f5cd5d0bb9cb1f8`.
- Authenticated `/view102/1001` runtime exposed one grouped Edit/Save toolbar;
  both commands rendered secondary outlined, and Save started disabled.
- Clicking Edit disabled Edit, enabled Save, and exposed two editable textboxes.
  Save was not clicked, so runtime acceptance did not mutate object data.
- Desktop and freshly loaded 390x844 screenshots show contiguous controls with
  no wrapping or horizontal overflow.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-detail-toolbar/detail-toolbar-editing-desktop.png`
- `artifacts/runs/20260713-legacy-detail-toolbar/detail-toolbar-mobile.png`

## Risks And Follow-Ups

- Normal groups use the same 34px readable geometry as the migrated query and
  dialog controls; the old customized Bootstrap build used smaller text.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
