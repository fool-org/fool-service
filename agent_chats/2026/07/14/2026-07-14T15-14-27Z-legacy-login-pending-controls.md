# Legacy Login Pending Controls

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared `views/index.jade` and `public/javascripts/app/login.js` with the
  Vue signed-out login panel.
- Removed the Vue-only request-pending label, spinner, and control locking.
- Kept only the missing-CAPTCHA-key submit guard required while the async Vue
  bootstrap has not yet reached the state that old server rendering guaranteed.
- Left the shared request runner unchanged because it already permits concurrent
  requests.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-14-27Z-legacy-login-pending-controls.md`

## Validation

- `cd frontend && npm test` passed: 14 files, 179 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- `curl -fsS http://localhost:8080/test` passed.
- `curl -fsSI http://localhost:8081/` returned HTTP 200.
- `python scripts/runtime_doctor.py` passed all checks.
- Deployed frontend image:
  `sha256:e07db3e06f2df0d420f5707b55daf3a3e7379a5313edf103a8e610ecc3fe9d8c`.

## Risks And Follow-ups

- Browser acceptance for overlapping login and CAPTCHA requests remains to be
  captured after deploying the rebuilt frontend image.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
