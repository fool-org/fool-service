# Legacy Readonly Edit Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep the field editor driven by rendered View
metadata instead of concrete business DTOs.

## Scope

- Restore the old readonly-field behavior when detail edit mode starts.
- Preserve editable controls, save payloads, and shared detail/child reuse.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-45-42Z-legacy-readonly-edit.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` renders detail and child values
  as paragraph text, using a non-breaking space for empty values.
- `../FoolFrame/src/Web/public/javascripts/app/detailview.js` `SetEdit` removes
  that text and creates a control only when `data-readonly == 'false'`.
- Readonly fields therefore remain text in edit mode and are not introduced
  into the generic `savetext.js` field-save path.

## Implementation

- Replaced the shared editor's disabled readonly `InputText` with a styled text
  span and a non-breaking-space fallback.
- Kept the rule in the existing shared metadata editor, so parent detail and
  inline child fields use one implementation.
- Added source-contract coverage without adding state, a DTO branch, an API, a
  dependency, or a component. The shared editor remains below 200 lines.

## Validation

- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:daf6b2c06704063ac8e302e907dbfa25f100d61d49b23acbe82e365045b1bf14`.
- Authenticated `/view100/1001` edit mode rendered five readonly values as text,
  zero textboxes, and one editable Customer combobox at both 1440x1000 and
  390x844.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-readonly-edit/readonly-edit-desktop.png`
- `artifacts/runs/20260714-legacy-readonly-edit/readonly-edit-mobile.png`

## Risks And Follow-Ups

- The runtime seed proves the shared parent-detail path. Child fields use the
  same component, while the seeded child group links to separate details rather
  than exposing an inline readonly field.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.
