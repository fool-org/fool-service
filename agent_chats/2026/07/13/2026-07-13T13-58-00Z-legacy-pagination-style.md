# Legacy Pagination Presentation Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the old shared Bootstrap pagination appearance.
- Preserve the existing list/candidate page state and query events.

## Changed Files

- `frontend/src/LegacyPagination.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-58-00Z-legacy-pagination-style.md`

## Legacy Evidence

- `view.jade` and `detailView.jade` render an unordered Bootstrap
  `.pagination` list beneath the record total.
- `navbar.js` renders `«` and `»`, up to seven direct page links, a disabled
  boundary state, and an active current page.
- Bootstrap 3 uses contiguous 34px controls, `#ddd` borders, `#337ab7` links
  and active fill, `#23527c` hover text, and `#777` disabled text.

## Implementation

- Reused PrimeVue Paginator's existing page calculation and events.
- Replaced only the Previous/Next icon slots with the legacy double angles.
- Added one shared style block for old dimensions, borders, colors, hover,
  active, disabled, and end radii; no page or query state was duplicated.
- Added source-contract coverage for both icon slots.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend/backend image build passed; the running frontend was then
  force-recreated after resource inspection proved Compose had retained the
  old container. Deployed image id:
  `sha256:7072a3ba080ade6f5b03a258698b8241d788669dc6d43aaa0c3bdceb3632c3b2`.
- Authenticated `/`: DOM rendered `« 1 »`; computed Previous/current-page
  heights were 34px, active fill was `rgb(51, 122, 183)`, disabled text was
  `rgb(119, 119, 119)`, and the first radius was `4px 0 0 4px`.
- At 390x844 the paginator remained within its 330px content width and page
  `scrollWidth` equaled `clientWidth` at 390.
- Repository harness validation passed; backend `8080/test`, frontend `/`, and
  frontend-proxied `8081/test` returned HTTP 200. MySQL and Redis remained
  healthy and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-pagination-parity/desktop.png`
- `artifacts/runs/20260713-pagination-parity/mobile.png`

## Risks And Follow-Ups

- The current Docker seed has one result page, so runtime proof covers both
  disabled boundaries and the selected page; source and PrimeVue behavior
  retain the already tested seven-link multi-page calculation.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
