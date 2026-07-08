# Vue First Screen Session Bootstrap

## Prompt

Continue the Docker/FoolFrame/Vue migration goal and keep the frontend moving
from component workspace toward a usable View workflow.

## Scope

- Added a minimal legacy session bootstrap before the first-screen View load.
- Reused existing Vue `initApp`, `loginV2`, `loadMainInfo`,
  `loadLegacyListView`, and `queryCurrentViewData` functions.
- Kept rendering on the View-first path:
  `initapp/loginv2/getmain -> getlistview(ViewId) -> querydata(ViewId)`.
- Updated migration/task notes for this browser-verified first-screen behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- payload.test.ts` failed because the app did
  not have `ensureLegacySession`, the Docker admin password default, or the
  required session bootstrap before `loadMainInfo`.
- Green: `cd frontend && npm test -- payload.test.ts` passed 46 tests.
- `cd frontend && npm test` passed 74 tests.
- `cd frontend && npm run build` passed.
- `docker compose up -d --build frontend` rebuilt and restarted frontend;
  Compose also rebuilt the backend dependency image successfully.
- `python scripts/runtime_doctor.py` passed all Docker/runtime checks.
- `python scripts/check_repo_harness.py` passed.
- Browser evidence: fresh `http://localhost:8081/` with local token removed
  rendered `Order List`, `View ID=100`, rows from `querydata`, and selected
  detail fields. Changing the main list page size to `2` then clicking
  `Load View` rendered 2 rows and `Page 1 / 4 · 8 rows`.
- Browser screenshot artifact:
  `/tmp/fool-service-vue-first-screen-autoload.png`.

## Skipped Checks

- Browser `domSnapshot()` failed in this environment with
  `incrementalAriaSnapshot is not a function`; Browser evaluate, locator
  interaction, console log filtering, and screenshots were used instead.

## Risks

- The Docker frontend now defaults the visible legacy password field to
  `admin` to make the local smoke account usable. This is scoped to the
  repository's Docker migration harness; a production auth shell should replace
  it when the app moves beyond local parity validation.
