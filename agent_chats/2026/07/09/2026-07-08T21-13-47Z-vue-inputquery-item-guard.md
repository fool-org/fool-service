# Vue InputQuery Item Guard

## Prompt

Continue the Docker/Vue FoolFrame migration, keep the UI View-first, reuse
existing helpers, and avoid concrete business DTO binding.

## Scope

- Fixed Vue input-query item display helpers so an empty legacy candidate entry
  returns empty text instead of throwing during render.
- Kept the fix in shared `viewWorkflow` helpers, not in individual templates.
- Rebuilt the Docker frontend and verified the first-screen View workflow still
  renders list/detail data and responds to `Load View`.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-13-47Z-vue-inputquery-item-guard.md`

## Validation

- Browser RED: `http://localhost:8081/` rendered but logged
  `Cannot read properties of undefined (reading 'itemId')` from the old
  frontend bundle `index-D_Sd38jX.js`.
- RED: `npm test -- viewWorkflow.test.ts` failed because
  `inputQueryItemId(undefined as never)` read `.id`.
- GREEN: `npm test -- viewWorkflow.test.ts`
- GREEN: `npm run build`
- GREEN: `npm test`
- GREEN: `docker compose build frontend >/tmp/fool_frontend_build.log 2>&1 && docker compose up -d frontend >/tmp/fool_frontend_up.log 2>&1`
- Browser GREEN: fresh tab on `http://localhost:8081/?qa=item-helper` loaded
  `index-C2xMzW1f.js`, rendered the Order List and Detail panels, clicked
  `Load View`, and current-bundle warning/error logs were empty. The browser
  log API still contained six old `itemId` errors from the previous bundle, so
  validation filtered logs by the current asset URL.

## Risks

- Browser `domSnapshot()` failed in the in-app browser runtime with an
  `incrementalAriaSnapshot` capability error, so rendered verification used
  read-only DOM evaluation, locator interaction, console logs, and screenshot
  evidence instead.
