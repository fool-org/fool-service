# Legacy Lookup Menu Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep the field editor driven by rendered View
metadata instead of concrete business DTOs.

## Scope

- Restore the old BusinessObject suggestion text hierarchy.
- Restore the old fixed lookup-menu footer.
- Preserve existing query context, selection, and save behavior.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-41-05Z-legacy-lookup-menu.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` exposes property type, model,
  view item, object, and owner context to the generic field editor.
- `../FoolFrame/src/Web/public/javascripts/app/setextype.js` renders each
  typeahead suggestion as `<strong>Text</strong> - Id` with an en-dash and
  appends `查找更多` as the dataset footer.
- The same old controller writes the selected suggestion ID and formatted text
  back to the generic property attributes used by `savetext.js`.

## Implementation

- Changed only the shared AutoComplete option and footer slots.
- Co-located two presentation rules in the 183-line field editor instead of
  expanding the 1173-line global stylesheet.
- Reused the existing lookup choices, request builder, empty feedback,
  force-selection, and selected-ID writeback.
- Added no state, DTO branch, API, dependency, or component.

## Validation

- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:c94f81882460bac42811247c9f82267005fad9a47a2d438641bd1c2de1d87a7a`.
- Authenticated `/view100/1001` lookup for `A` returned two options rendered as
  `Grace Trading - 3002` and `Ada Capital - 3001` with an en-dash plus one
  `查找更多` footer at both 1440x1000 and 390x844.
- Selecting Grace closed the menu and updated the input to `Grace Trading`;
  Save was not invoked.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-lookup-menu/lookup-menu-desktop.png`
- `artifacts/runs/20260714-legacy-lookup-menu/lookup-menu-mobile.png`

## Risks And Follow-Ups

- `查找更多` remains presentation-only, matching the old footer template which
  had no click handler in `setextype.js`.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.
