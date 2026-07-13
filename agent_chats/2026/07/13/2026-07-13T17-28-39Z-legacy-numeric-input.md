# Legacy Numeric Input Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View-driven field editor before
changing data behavior.

## Scope

- Restore the old numeric text-input filtering for property types 1-6.
- Preserve the old unrestricted fallback for other numeric property types.
- Keep values and save payloads driven by rendered View metadata.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/fieldInput.ts`
- `frontend/src/fieldInput.test.ts`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-28-39Z-legacy-numeric-input.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` supplies property type, value,
  model, edit, and readonly metadata to each rendered field container.
- `../FoolFrame/src/Web/public/javascripts/app/detailview.js` `SetEdit` reads
  those View attributes before delegating control creation to `setextype`.
- `../FoolFrame/src/Web/public/javascripts/app/setextype.js` renders property
  types 1/2 and 3/4 as digit-filtered text inputs with four/eight-character
  limits, and types 5/6 as decimal-character text inputs with the same limits.
- `../FoolFrame/src/Web/public/javascripts/app/savetext.js` reads the resulting
  input string into the generic `{ Key, Value }` property list.

## Implementation

- Replaced the native numeric input mapping with the old text-input contract.
- Added one shared metadata constraint map, a max-length helper, and a pure
  sanitizer reused by every simple detail and child field editor.
- Kept types 0, 7, and 10 on unrestricted text inputs, matching the old
  `setextype` fallback rather than inventing constraints.
- Added no DTO branch, API, dependency, component, or duplicate editor state.

## Validation

- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:b7df59eb47f8a408a02c0708b6037d350748ce0fe2f8c81f08720b5d60203d82`.
- Pure-function coverage proves digit/decimal filtering, four/eight-character
  limits, named/numeric metadata aliases, and the unrestricted Decimal fallback.
- The authenticated `/view100/1001` edit state remained usable at 1440x1000 and
  390x844 with text inputs and no clipping or overlap.
- Repository harness passed; backend `/test` and frontend `/` returned HTTP 200;
  MySQL and Redis were healthy and `db-migrate` was `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-numeric-input/numeric-editor-desktop.png`
- `artifacts/runs/20260714-legacy-numeric-input/numeric-editor-mobile.png`

## Risks And Follow-Ups

- Current Docker seed metadata exposes property types 1-6 only as readonly list
  columns, so the editable filter path is covered by pure-function and component
  source contracts rather than a seeded business-page interaction.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.
