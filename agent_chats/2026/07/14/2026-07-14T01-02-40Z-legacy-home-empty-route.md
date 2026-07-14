# Legacy Home Empty Route

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old authenticated `/` and post-login `/main` no-default-View routes to
  their actual Jade templates.
- Restore the old login path replacement and route-specific empty-home copy.
- Keep the decision bound to `getmain.App.DefaultViewId`, not business data.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T01-02-40Z-legacy-home-empty-route.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/login.js` replaces the successful
  login path with `/main` and reloads.
- `../FoolFrame/src/Web/routes/index.js` renders `main.jade` when authenticated
  `/` has no `App.DefaultViewId`, but renders `Sudoku.jade` for the equivalent
  `/main` state.
- `../FoolFrame/src/Web/views/main.jade` says `默认首页 还没有配置`.
- `../FoolFrame/src/Web/views/Sudoku.jade` contains the longer SOWAY default
  configuration guidance.
- Vue stayed on the login URL and hard-coded the Sudoku copy for both routes.

## Implementation

- Replaced the path with `/main` immediately after successful `loginv2`, before
  entering the existing authenticated shell.
- Added one presentation ref populated only in the existing no-default-View
  branch. It selects the checked-in `/main` or `/` template text from the
  current path.
- Kept `legacyAppDefaultViewId`, `loadPrimarySection`, and all View/data loading
  unchanged; no router or business DTO binding was introduced.
- Strengthened the Home source contract for login path, both old strings, and
  the shared template binding.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:d83c8470e885da5a2cd8f0aa7cba4b622e9e36a9aa3be12c12d746899c672345`.
- The deployed Nginx app chunk contains both exact Jade strings.
- An authenticated Docker replay temporarily changed
  `SW_APPLICATION.SW_APP_VIEW` from 100 to 0. Successful `admin/admin` login
  landed on `/main` and showed only the long `Sudoku.jade` guidance; clicking
  desktop Home changed the URL to `/` and showed only `默认首页 还没有配置`.
- Both empty-home routes retained viewport and document widths of exactly 390px
  at 390x844. Browser console errors were empty, the test session logged out,
  and the database value was restored to 100.
- Full frontend tests/build, repository harness, Compose health, backend
  `/test`, and `git diff --check` passed before the atomic commit.

## Runtime Evidence

- Deployed bundle `/usr/share/nginx/html/assets/index-D9gZczne.js` in the
  `fool-service-frontend-1` container.
- `artifacts/runs/20260714-legacy-empty-home-visible/empty-main-desktop.png`
- `artifacts/runs/20260714-legacy-empty-home-visible/empty-main-mobile.png`
- `artifacts/runs/20260714-legacy-empty-home-visible/empty-root-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in route,
  JavaScript, and Jade sources supplied the old contract.
