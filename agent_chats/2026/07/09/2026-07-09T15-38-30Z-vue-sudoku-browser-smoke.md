# Vue Sudoku Browser Smoke

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Prove the frontend is usable through rendered View metadata, not business DTO
  shortcuts.

## Scope

- Browser-checked the Docker-rendered Vue page at `http://localhost:8081`.
- Loaded the seeded `OrderSudoku` View (`ViewId=103`) through the visible
  `Load View` control.
- Fixed child panel titles by reusing the shared `fieldTitle` helper and
  falling back to ViewItem `Name` / `name` metadata.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-38-30Z-vue-sudoku-browser-smoke.md`

## Red Check

- Initial headless Chrome DOM smoke loaded `ViewId=103` and found the five
  root Sudoku panels, but failed to find `Group Orders` and `Group Detail`.
  The page rendered generic `list` / `item` child text because `fieldTitle`
  ignored ViewItem `Name` metadata.

## Validation

- `cd frontend && npm test`
- `cd frontend && npm run build`
- `docker compose up -d --build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Headless Chrome CDP smoke against `http://localhost:8081`, loading
  `ViewId=103`, passed these DOM checks:
  `Order Sudoku`, `Orders List`, `Price Chart`, `Customer Map`, `Order Item`,
  `Order Group`, `Group Orders`, `Group Detail`, non-empty rows, and no
  `.error` / `[role=alert]`.

## Artifacts

- `artifacts/runs/2026-07-09-sudoku-browser-smoke/sudoku-dom.json`
- `artifacts/runs/2026-07-09-sudoku-browser-smoke/sudoku-page.png`

## Risks

- The browser smoke is a one-off local CDP check, not a committed reusable
  browser automation script. Existing frontend unit tests and Docker runtime
  doctor remain the repeatable gates for this slice.
