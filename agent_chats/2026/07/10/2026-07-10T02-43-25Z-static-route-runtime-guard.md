# Static Route Runtime Guard

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Added old FoolFrame Web static `GET /about` and `GET /contact` route checks
  to the Docker runtime doctor.
- Reused the existing Vue/Nginx frontend fallback; no new pages or components.
- Updated migration parity and task-state docs.

## Changed Files
- `scripts/runtime_doctor.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-43-25Z-static-route-runtime-guard.md`

## Validation
- `python scripts/runtime_doctor.py` passed against the running Docker stack
  and included `frontend:about-path` / `frontend:contact-path`.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- Before the patch, `curl http://localhost:8081/about` and
  `curl http://localhost:8081/contact` both returned `200 text/html` with the
  built Vue document.

## Risks
- This is a guard-only slice; it does not add dedicated About/Contact content.
