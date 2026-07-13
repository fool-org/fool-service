# Legacy Report Condition Empty State Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old View first and avoid DTO coupling.

## Scope

- Restore the old condition tab's initial empty structure.
- Preserve both Add entry points and all condition/filter state.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-45-00Z-legacy-report-condition-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/view.jade` renders the condition table header and
  a trailing row containing the Add glyph before any condition is created.
- The old View contains no explanatory empty-state sentence between those two
  rows; the Vue migration had added `未设置条件，将包含全部记录。`.

## Implementation

- Removed only the Vue-only empty-state element.
- Retained the header Add command, trailing Add command, table headings,
  condition collection, grouping, and filter serializer unchanged.
- Added a source contract that prevents the explanatory text from returning.

## Validation

- Focused condition and source-contract suites: 86 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:2c217f8e64064011bd34336f29fd0ce30858d7082eb7b4253ecaafce00c0dceb`.
- Authenticated `/view101` condition setup exposed exactly two Add commands and
  zero matches for the removed empty-state sentence before any condition was
  created.
- Desktop and 390x844 screenshots show the header and trailing Add row without
  overlap. Repository harness validation passed; frontend `/` and
  frontend-proxied `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-report-condition-empty/condition-empty-desktop.png`
- `artifacts/runs/20260713-legacy-report-condition-empty/condition-empty-mobile.png`

## Risks And Follow-Ups

- The empty condition set still intentionally serializes to no filter, matching
  the existing View-driven report request contract.
- Runtime screenshots show the merge-group command icon needs a separate
  visibility/parity fix; it is intentionally excluded from this atomic slice.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
