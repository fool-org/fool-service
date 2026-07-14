# Legacy Login Implicit Submission

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared `index.jade`, `login.js`, Angular's form directive, and HTML's
  default-button implicit-submission rule before changing the Vue contract.
- Confirmed Enter fires the old default Login button's `ng-click="hello()"`
  before Angular prevents page submission.
- Retained Vue's existing submit button and form submit handler, adding only a
  source contract that keeps those two required pieces together.

## Changed Files

- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-37-19Z-legacy-login-enter-submit.md`

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
- Restored deployed frontend image:
  `sha256:ce7a2f81272de55d1209e60a9bf79335e206945e62d738347fc13e8bc6dc5884`.
- Authorized Chrome acceptance loaded the current deployed bundle, filled
  `admin/admin` and the current local CAPTCHA, then pressed Enter from the
  CAPTCHA input without clicking Login.
- The page entered the Admin shell and loaded Order List, proving the existing
  form submit handler reached `loginv2`. No key listener or implementation
  change was required.

## Risks And Follow-ups

- This implicit-submit parity slice is closed; broader migration work remains.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
