# Application Browser Title Parity

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `layout.jade` and `default.jade` before touching frontend state: the
  signed-out title comes from `initapp.AppName`, while authenticated pages use
  `getmain.App.AppName`.
- Synchronized `document.title` from the same application metadata already
  rendered by the Vue login heading and shell brand.
- Kept the static HTML title as a fallback and added no View/data DTO, route,
  store, dependency, or additional abstraction.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-18-35Z-app-browser-title.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` (85 tests passed)
- `cd frontend && npm test` (20 files, 210 tests passed)
- `cd frontend && npm run build`
- `python scripts/runtime_doctor.py` (68 checks passed)
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2106 lines against the
  repository limit of 2100.
- Exact implementation commit `a98e3012` produced deployed image
  `sha256:63d5995451ac3184c16409441f9612c9fb16b0ca2b38c123d710b43ee4f71f67`;
  the running frontend container used that exact image ID.
- Authorized browser acceptance preserved the real Docker database and local
  CAPTCHA while intercepting only the real `initapp` application-name fields.
  The signed-out document title and heading both rendered
  `Legacy Login Title`; `admin/admin` login returned HTTP 200 and changed both
  the authenticated title and shell brand to `Fool Service`. Logout returned
  HTTP 200 and restored `Legacy Login Title` from cached init metadata without
  issuing another `initapp` request.
- Visible evidence:
  `artifacts/runs/20260715-app-title-parity/login-title.png` (`1440x900`,
  SHA-256
  `76a1f7d743213707cbe489f98123fc0758030da3b0f8cb08fb694c4de4011e84`).
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.

## Risks And Follow-ups

- The screenshot artifact is intentionally ignored by Git; its exact path and
  hash are recorded above.
- Repository harness status is blocked only by the concurrent oversized Java
  test noted above. This slice did not edit or stage that file.
