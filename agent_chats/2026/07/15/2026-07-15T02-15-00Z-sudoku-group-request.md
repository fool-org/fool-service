# Sudoku Group Request Boundary

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `Sudoku.jade` and `includes/Group.jade` into
  `GroupViewController` before comparing the Vue data workflow.
- Confirmed the old Group branch loads `/view` metadata and creates
  `QuerylistdataController` only for each `ListViewType=0` child.
- Removed the Vue-only Group-root list query by reusing the existing
  metadata-only `loadViewById` path. No component, DTO, state module, route, or
  request-specific abstraction was added.
- Browser-verified exact implementation commit `62f17909` on Docker.

## Changed Files

- `frontend/src/useSudokuPanels.ts`
- `frontend/src/SudokuPanels.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T02-15-00Z-sudoku-group-request.md`

## Validation

- `cd frontend && npm test -- SudokuPanels.test.ts` (3 tests passed)
- `cd frontend && npm test` (20 files, 215 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Compose kept backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Authorized `admin/admin` acceptance loaded `/view103`. The captured initial
  View/data sequence retained `getlistview(104)` and two `querydata(100)` calls
  for the root List and Group child List, but contained zero `querydata(104)`
  calls.
- Desktop 1280x800 and mobile 390x844 each retained five root panels and
  `Group Orders`; document width equaled viewport width in both modes.
- Browser warnings and page errors were empty. The login token was logged out
  and the isolated tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:83fbae3abb39efb5164683cc3445fb96d4af7e84fba57cd3982e0d19a89d94e5`.
- Visible evidence:
  - `artifacts/runs/20260715-sudoku-group-request/desktop-group-panels.png`
    (`1280x800`, SHA-256
    `af6c411267c4fdfdb8d2137664718d6f04deef7457a106f2b29a824ef43cb53e`).
  - `artifacts/runs/20260715-sudoku-group-request/mobile-group-panels.png`
    (`390x844`, SHA-256
    `89797c636b34fc4f439cbdeb737331b073959f3fce7aa8a6d550467da8f82dd7`).

## Risks And Follow-ups

- The standard Compose build remained unavailable because local Buildx cannot
  update its activity file (`operation not permitted`). The host-validated
  `dist` was injected into the existing Nginx image for runtime acceptance.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
