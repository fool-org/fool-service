# Legacy Login Vendor Link

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared `index.jade`'s vendor anchor with `LoginPanel.vue` and traced
  `AppUrl` from the old/current application metadata DTOs.
- Restored external navigation for legacy bare-host values by adding
  `http://` only when the value has no scheme.
- Preserved current absolute HTTP(S) values instead of producing an invalid
  double scheme.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-30-10Z-legacy-login-vendor-link.md`

## Validation

- `cd frontend && npm test` passed: 14 files, 179 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Backend `/test` and frontend `/` smoke passed.
- `python scripts/runtime_doctor.py` passed all 67 checks.
- Deployed frontend image:
  `sha256:ce7a2f81272de55d1209e60a9bf79335e206945e62d738347fc13e8bc6dc5884`.
- Backed up the Docker app metadata row as `SW_APP_COMPANY=NULL` and
  `SW_APP_URL=NULL`.
- With `Legacy Vendor` / `legacy.vendor.test/path`, Docker initapp exposed the
  bare value and the browser rendered both `href` forms as
  `http://legacy.vendor.test/path` with `target=_blank`.
- With `https://legacy.vendor.test/secure`, both browser href forms retained
  that exact absolute URL with no duplicate scheme.
- Restored both database fields to `NULL`; initapp returned empty strings and
  a fresh browser load rendered zero vendor links.
- `python scripts/runtime_doctor.py` passed all 67 checks after restoration.
- Screenshot:
  `artifacts/runs/20260714-legacy-login-vendor-link/bare-url.jpg` (1280x720).

## Risks And Follow-ups

- No unresolved risk remains for this interaction slice.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
