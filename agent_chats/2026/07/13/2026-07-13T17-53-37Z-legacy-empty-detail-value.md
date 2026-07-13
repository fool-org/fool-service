# Legacy Empty Detail Value Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep rendering driven by View metadata before any
data values and avoid concrete business DTO bindings.

## Scope

- Restore the old empty simple-value placeholder in detail view mode.
- Preserve shared values, edit drafts, and save payloads.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T17-53-37Z-legacy-empty-detail-value.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` renders each `SimpleData` value
  as paragraph text and explicitly renders `&nbsp;` when `FmtValue` is empty.
- That placeholder is presentation-only; old `savetext.js` still derives save
  values from controls created only after edit starts.

## Implementation

- Added a non-breaking-space fallback only in the detail view-mode template.
- Kept `fieldDisplayValue` returning the protocol value unchanged, so View
  projection, drafts, child values, and payload building retain their existing
  contracts.
- Added no state, DTO branch, helper, API, dependency, style, or component.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend image build and forced frontend recreation passed. Deployed
  image id:
  `sha256:6b9465ad9eccc8c694c988487be46a8f58bd74da50db8ae27323719912c859df`.
- Authenticated `/itemview100` rendered all six schema-only detail values as
  character code 160 with a 22px text line at 1440x1000. The same six values
  retained the placeholder at 390x844 with no document-level overflow.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-empty-detail-value/empty-detail-desktop.png`
- `artifacts/runs/20260714-legacy-empty-detail-value/empty-detail-mobile.png`

## Risks And Follow-Ups

- The placeholder is intentionally not written into data or save state.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this slice without generating a new
  CAPTCHA.
