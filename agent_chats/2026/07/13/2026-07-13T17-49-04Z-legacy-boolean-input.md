# Legacy Boolean Input Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep the field editor driven by rendered View
metadata instead of concrete business DTOs.

## Scope

- Remove Vue-only Boolean copy from the shared field editor.
- Preserve checkbox state conversion and save values.

## Changed Files

- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-49-04Z-legacy-boolean-input.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/setextype.js` handles property
  type 8 by appending one checkbox and no accompanying `是` / `否` text.
- `../FoolFrame/src/Web/public/javascripts/app/savetext.js` derives the saved
  Boolean directly from the checkbox's checked state and sends it through the
  generic `{ Key, Value }` array.

## Implementation

- Removed only the invented checked-state text beside the shared PrimeVue
  Checkbox.
- Reused the existing View metadata dispatch, `fieldInputChecked` conversion,
  `true` / `false` model values, wrapper geometry, and enclosing field label.
- Added no state, DTO branch, API, dependency, style, or component. The shared
  editor is 194 lines.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:dbdd238cbd32087a333a3d0300fa5d5343cf2234ed466a3ce6b70ee53a07e3a2`.
- Authenticated `/view100/1001` detail-edit regression passed at 1440x1000 and
  390x844 with five readonly values, one editable Customer combobox, and no
  document-level horizontal overflow on mobile.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-boolean-input/boolean-regression-desktop.png`
- `artifacts/runs/20260714-legacy-boolean-input/boolean-regression-mobile.png`

## Risks And Follow-Ups

- The seeded Views expose no editable property type 8 field, so exact checkbox
  presentation is covered by source contract and boolean conversion tests; the
  runtime pass covers the shared editor's seeded detail path without inventing
  a business DTO fixture.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.
