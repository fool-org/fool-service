# Vue Legacy Shell Menu

## Prompt

Continue the Docker/FoolFrame/Vue migration goal. The user clarified that the
frontend must first load the View definition, render the page from that View,
and only then query data by View context; binding the page to a concrete
business DTO is the wrong direction.

## Scope

- Rendered legacy shell menu entries from `getmain.TopMenu` and
  `getsubmenu.Items` in the Vue sidebar.
- Wired menu items with `ViewId` into the existing
  `getlistview(ViewId)` -> `querydata(ViewId)` workflow.
- Kept menu clicks from pre-binding `legacyQueryViewId`; query context is
  filled only after the View definition loads.
- Tightened the API boundary so non-zero response codes reject instead of
  being treated as successful empty data.
- Added stale stored-token recovery before first-screen View rendering.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/api.test.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- payload.test.ts viewWorkflow.test.ts`
  failed before `legacyMainMenuItems` and sidebar shell-menu wiring existed.
- Red: `cd frontend && npm test -- payload.test.ts` failed while
  `openShellMenu` still wrote `legacyQueryViewId` directly.
- Red: `cd frontend && npm test -- api.test.ts payload.test.ts` failed while
  `postApi` accepted non-zero API codes and first-screen workflow did not retry
  a stale stored token.
- Green: `cd frontend && npm test -- payload.test.ts viewWorkflow.test.ts`
  passed 73 tests.
- Green: `cd frontend && npm test -- api.test.ts payload.test.ts` passed 49
  tests.
- `cd frontend && npm test` passed 77 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend && docker compose up -d frontend` rebuilt the
  frontend image and restarted the local stack.
- `python scripts/runtime_doctor.py` passed after the Docker restart.

## Runtime Evidence

- Browser reload at `http://localhost:8081/` recovered from a stale stored token:
  the first `getmain` returned an invalid-token response, the app retried the
  legacy session, then rendered `Order List`, `View ID=100`, and 8 rows.
- Clicking sidebar `Views` called `getsubmenu` and replaced the shell menu with
  `OrderList`.
- Clicking sidebar `OrderList` called `getlistview` then `querydata`; the page
  remained on `Order List`, `View ID=100`, with View-derived headers
  `Order ID`, `Symbol`, `Customer`, and `State`.
- Frontend container logs showed the browser path:
  `getmain` invalid-token response, successful `getmain`, `getlistview`,
  `querydata`, then `getsubmenu`, then `getlistview` and `querydata` for the
  `OrderList` menu click.
- Browser screenshot artifact:
  `/tmp/fool-service-vue-shell-menu-view-first.png`.

## Skipped Checks

- Browser `domSnapshot()` still failed in this environment with
  `incrementalAriaSnapshot is not a function`; bounded Browser evaluate,
  scoped locator counts/clicks, container logs, and screenshot evidence were
  used instead.

## Risks

- The sidebar now shows both the static workspace navigation and the migrated
  FoolFrame shell menu. It is usable, but the next frontend pass should decide
  whether the static `Views/API Tools/Migration` navigation remains as an
  operator panel or moves behind a less prominent tool area.
