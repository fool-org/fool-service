# Item View Tab Selection

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits, keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Compared `../FoolFrame/src/Web/views/item.jade` before inspecting data or
  business DTOs.
- Confirmed its `DetailViews` tab links and panes have no initial Bootstrap
  `active` class; a child table appears only after the user selects its tab.
- Changed only the existing `ViewDetailPanel` tab initialization for
  `schemaOnly` routes. Detail and new-object pages retain their first active
  child collection.
- Added no component, data request, business DTO branch, or new state module.
- Browser-verified exact implementation commit `9716ca50` on Docker.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T01-37-32Z-itemview-tab-selection.md`

## Validation

- `cd frontend && npm test -- --run ViewDetailPanel.test.ts payload.test.ts`
  (2 files, 91 tests passed)
- `cd frontend && npm test` (20 files, 214 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Frontend `/itemview100` and backend `/test` returned HTTP 200. Compose kept
  backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and `db-migrate` at
  `Exited (0)`.
- Authorized `admin/admin` acceptance loaded `/itemview100` at 1280x720. The
  `Items` tab initially had `aria-selected=false`, its panel computed to
  `display:none`, and no child table was visible.
- Clicking `Items` changed it to `aria-selected=true`, displayed the
  View-derived `Item ID` / `Item Name` / `操作` header, retained the route, and
  emitted zero network requests.
- A fresh 390x844 navigation again started unselected with no visible child
  table; document and viewport widths were both 390px.
- `/view100/1001` retained its active first tab and requested
  `getreaditemview` before `querydatadetail`. `/new100` retained its active
  first tab and requested `getreaditemview` before `initnew`.
- Browser warnings and page errors were empty. The temporary login token was
  logged out and the isolated test tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:3fef5b753aa96571f252580a25b647baeb54dde35f966b4dc0b4f762bef46e2e`.
- Visible evidence:
  - `artifacts/runs/20260715-itemview-tab-selection/desktop-initial.png`
    (`1280x720`, SHA-256
    `567704c1c7952fc2edd5d69dedd4d7682778a8adce465ef1442a88e2a3fd057a`).
  - `artifacts/runs/20260715-itemview-tab-selection/desktop-selected.png`
    (`1280x720`, SHA-256
    `1ad083ca26e30531d0dfb9f498026707b4356032170bcd478075516e9a36b0f6`).
  - `artifacts/runs/20260715-itemview-tab-selection/mobile-initial.png`
    (`390x844`, SHA-256
    `62bb5466a2bbad4536c15fed8478a2a0469b6bbf1d3b7ae52c223e9f2a31adc5`).

## Risks And Follow-ups

- `docker compose up -d --build frontend` could not update the local Buildx
  activity file and returned `operation not permitted`. The same host-validated
  `dist` was copied into a clean temporary container from the existing Nginx
  image, committed, and used to recreate the frontend. Re-run the standard
  Dockerfile build when Buildx can write its activity directory.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
