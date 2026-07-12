# Bootstrap Navbar Style Parity

## Prompt

Continue aligning the Vue page with FoolFrame layout, style, and interaction,
with every behavior change committed atomically.

## Scope

- Compare the old `navbar-default`, dropdown, and list-group CSS with the Vue
  shell navigation.
- Replace invented indigo, rounded, oversized navigation rules with the old
  Bootstrap palette and geometry.
- Reuse the same neutral rules in the responsive Drawer.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-55-25Z-bootstrap-navbar-style.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 148 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  the final CSS decreased from 24.42 kB to 24.35 kB.
- Production CSS scan confirms the deployed bundle contains the old shell
  background/border, neutral expanded state, and 160px dropdown geometry.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: rebuilt frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all Compose, auth, menu, View/data,
  report, message, and logout checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `../FoolFrame/src/Web/public/stylesheets/bootstrap.css` defines the old
  `navbar-default` background/border as `#f8f8f8/#e7e7e7`, link text as
  `#777`, hover as `#333`, and open state as `#555/#e7e7e7`.
- The same file defines 50px navbar actions and a 160px-minimum dropdown with
  10px text, 2px top margin, 4px radius, and `0 6px 12px` shadow.
- Its narrow-screen dropdown rows use 25px left padding, retained in the Vue
  Drawer so child hierarchy remains visible without the invented border line.
- `menuinfo.js` appends child rows with Bootstrap `list-group-item` geometry.

## Risks And Follow-Ups

- Authenticated visual acceptance still needs a fresh CAPTCHA authorization.
- The in-app browser could not attach its new local test tab to the current
  task, so no screenshot is claimed for this slice.
- Brand typography is intentionally left for a separate atomic parity slice.
