# Legacy Empty Report Output Fields Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old report View before changing the
Vue output setup and keep it bound to report View metadata.

## Scope

- Retain the old three-part report-output editor when no model columns exist.
- Remove the Vue-specific zero-field sentence and avoid an invented output type.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-44-53Z-legacy-report-output-empty-fields.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` always renders the candidate-column,
  output-type, and selected-column `select` controls in the Output tab.
- `../FoolFrame/src/Web/public/javascripts/app/mkreport.js` only appends output
  types after a real candidate selection change; it defines no zero-field copy.

## Implementation

- Removed the column-count guard around the shared `ReportOutputSelector`.
- Removed `暂无报表字段。` and let the existing metadata arrays render empty
  controls naturally.
- Returned no output-type options when no metadata candidate exists; the
  existing `原值` fallback still applies to a real candidate with no types.
- Added no DTO, API, state, component, or CSS changes.

## Validation

- Focused source-contract suite: 82 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:e511841a5580e983f2d87f8ef9c8d45209259bb6467a6cf6fb467c0c9d429337`.
- Docker `getmkqview` for protocol sample `viewId=106` returned success with
  both `cols` and `Cols` empty, covering the zero-field input contract without
  database mutation.
- Authenticated `/view101` regression opened the Output tab with one dialog,
  three listboxes, two metadata candidates, and no `暂无报表字段。` copy.
  Stable 1440x1000 and 390x844 screenshots show the normal editor layout.
- Repository harness validation passed; frontend `/` and backend `/test`
  returned HTTP 200. MySQL and Redis remained healthy and `db-migrate` remained
  `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-report-output-empty-fields/report-output-desktop.png`
- `artifacts/runs/20260714-legacy-report-output-empty-fields/report-output-mobile.png`

## Risks And Follow-Ups

- `viewId=106` is a protocol-only zero-field sample and is not a navigable
  seeded View, so browser regression used the normal metadata path on `/view101`.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
