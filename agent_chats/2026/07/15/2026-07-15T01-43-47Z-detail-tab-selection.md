# Detail Tab Selection

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Rechecked `../FoolFrame/src/Web/views/detailView.jade` before its data and
  mutation code.
- Confirmed its collection links and panes have no initial Bootstrap `active`
  class, just like `item.jade`.
- Searched the complete old application-script directory and found no
  `detail-tab`, `tabbtn-*`, `tab('show')`, or active-class initialization. The
  new-object `beginedit()` callback starts field editing only.
- Removed the Vue-only first-child default from the existing shared tab state.
  No component, DTO branch, request, or state module was added.
- Browser-verified exact implementation commit `82dc8eb5` on Docker.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T01-43-47Z-detail-tab-selection.md`

## Validation

- `cd frontend && npm test -- --run ViewDetailPanel.test.ts payload.test.ts`
  (2 files, 91 tests passed)
- `cd frontend && npm test` (20 files, 214 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Frontend `/view100/1001` and backend `/test` returned HTTP 200. Compose kept
  backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and `db-migrate` at
  `Exited (0)`.
- Authorized `admin/admin` acceptance loaded `/view100/1001` at 1280x720. The
  `Items` tab initially had `aria-selected=false`, its panel computed to
  `display:none`, and no child table was visible despite detail data loading.
- Clicking `Items` changed it to `aria-selected=true`, displayed Add plus the
  three View-derived child rows, retained the route, and emitted zero requests.
- `/new100` also started unselected, then displayed its Add command and empty
  View-derived child table after selection; its tab click emitted zero requests.
- `/itemview100` remained initially unselected after the shared change.
- Existing detail loaded `getreaditemview` before `querydatadetail`; new-object
  loaded `getreaditemview` before `initnew`.
- A fresh `/view100/1001` at 390x844 remained unselected with no visible child
  table; document and viewport widths were both 390px.
- Browser warnings and page errors were empty. The temporary login token was
  logged out and the isolated test tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:77f617bf0d6393b99726172639aa28363415859f4714d788834d129cd5cb3adf`.
- Visible evidence:
  - `artifacts/runs/20260715-detail-tab-selection/detail-initial.png`
    (`1280x720`, SHA-256
    `a2a487d5c4279ebac91d1ca1822cd8a179df73fe8467b1a3dd8cb382f936a91f`).
  - `artifacts/runs/20260715-detail-tab-selection/detail-selected.png`
    (`1280x720`, SHA-256
    `b7456710c383d215b039017680dc672202ace1891540273cc0d811a0ad14fcb9`).
  - `artifacts/runs/20260715-detail-tab-selection/new-initial.png`
    (`1280x720`, SHA-256
    `6429b086c46a918916c35072782f9247c51158c154138ad735e4275a991af35e`).
  - `artifacts/runs/20260715-detail-tab-selection/detail-mobile-initial.png`
    (`390x844`, SHA-256
    `7c100fc81dbd03554b7a4d1af654ab20a0e35d8077c4f89eb6b724c4d1d22ffb`).

## Risks And Follow-ups

- The standard Compose build remained unavailable because local Buildx could
  not write its activity file (`operation not permitted`). The host-validated
  `dist` was copied into a clean temporary container from the existing Nginx
  image, committed, and used to recreate the frontend. Re-run the standard
  Dockerfile build when Buildx can write its activity directory.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
