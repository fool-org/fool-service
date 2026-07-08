# Vue Child Draft Render

## Prompt

Validate the Vue frontend as a usable View-first screen and fix runtime issues
without reintroducing concrete business DTO bindings.

## Scope

- Browser-checked the Docker frontend at `http://localhost:8081/`.
- Confirmed first-screen View metadata and row data render, but found a runtime
  error reading `undefined.itemId` while child add-row metadata rendered.
- Fixed the timing by syncing child drafts immediately after
  `getreaditemview` metadata loads.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Green: `cd frontend && npm test -- --run`.
- Green: `cd frontend && npm run build`.
- Green: `docker compose up -d --build frontend`.
- Green: `docker compose up -d --force-recreate frontend`.
- Browser proof: Docker Vue first screen loads `Order List` with 8 rows from
  the View-first `getlistview` / `querydata` path.
- Clean Chrome proof: loaded bundle `index-B1Fhodkk.js` reported no console
  messages or page errors.
- Interaction proof: clicking the second row `Open` updates the selected row
  and detail fields to object `9851343` / `QA-456235-USDT`.

## Skipped Checks

- The Browser plugin `domSnapshot()` path failed with an extension-side
  `incrementalAriaSnapshot` capability error, so clean console validation used
  Playwright with the local Chrome channel.

## Risks

- The main View screen is now usable for loaded list/detail/open flows, but
  richer child add/select/save flows still need separate browser proof.
