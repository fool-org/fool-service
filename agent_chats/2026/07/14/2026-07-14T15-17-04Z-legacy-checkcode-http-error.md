# Legacy Checkcode HTTP Error

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared `login.js` CAPTCHA refresh with the Vue `loadCheckCode` flow.
- Suppressed only the shared transient error produced when `getcheckcode`
  returns no HTTP response.
- Preserved successful key/image replacement and all response-backed login
  business-error handling.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-17-04Z-legacy-checkcode-http-error.md`

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
  `sha256:e6808b1d432cb229b1deab15ed416e1316d8a0329583590ea710a42fb2ddc537`.

## Risks And Follow-ups

- Browser acceptance should stop the backend after page load, click Refresh,
  and prove the existing image/fields remain without a dialog.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
