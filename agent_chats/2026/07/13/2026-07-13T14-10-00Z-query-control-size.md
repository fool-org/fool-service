# Query Control Geometry Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap input/button geometry in list and candidate query forms.
- Preserve responsive placement and query interactions.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-10-00Z-query-control-size.md`

## Legacy Evidence

- Bootstrap 3 `.form-control` and default buttons render at 34px high with
  6x12px padding, 14px type, 20px line height, and 4px radius.
- `view.jade`, `viewWithChart.jade`, and `detailView.jade` all use those shared
  Bootstrap controls for query input and Find commands.
- The current PrimeVue query controls were 40px high in runtime screenshots.

## Implementation

- Added one shared CSS rule across the existing list and candidate toolbar
  selectors for input and button geometry.
- Reused the same rule for normal list, chart list, report/create commands,
  and candidate search; no component, state, event, or request code changed.
- `style.css` remains below the repository's 2000-line source limit.

## Validation

- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:eb338b643126a93be929041f589003053a44d86718faa2f65e1ba2dd177dbb6e`.
- Authenticated `/view101` computed a 240x34px input and 54x34px Find button;
  both used 6x12px padding, 14px type, and 20px line height.
- Authenticated `/view102/1001` candidate dialog rendered the same compact
  query controls at desktop and 390x844 while preserving responsive wrapping.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-query-control-size/desktop-candidate.png`
- `artifacts/runs/20260713-query-control-size/mobile-candidate.png`

## Risks And Follow-Ups

- The rule is intentionally limited to the two migrated legacy query toolbars;
  unrelated PrimeVue form controls retain their component sizing.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
