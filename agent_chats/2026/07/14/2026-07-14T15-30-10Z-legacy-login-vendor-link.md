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

## Risks And Follow-ups

- The Docker seed currently leaves App company/URL empty. Browser acceptance
  should temporarily exercise a bare host and restore the database row.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
