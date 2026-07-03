# Vue Order Item Add Workflow

## Prompt

Continue the active migration goal: keep Docker running, compare against
`../FoolFrame`, migrate parity forward, keep Vue as the frontend, and commit
atomically.

## Legacy Reference

- `../FoolFrame/src/Web/views/detailView.jade` renders child item tabs and
  add/edit/delete controls for `view.Data.Items`.
- `../FoolFrame/src/Web/public/javascripts/app/detailview.js` stores new
  child rows in `obj.Itemproperties[].AddedItems`.
- Existing Spring migration already maps `saveobj.Itemproperties` into dynamic
  collection writes.

## Scope

- Added an `Order Items` section to the default Vue `OrderList` detail panel.
- Added `Add Item`, sending `saveobj.Itemproperties[0].addedItems` for the
  `items` collection.
- Kept this as a happy-path child add. Generated child collection rendering,
  edit, delete, and modal selection remain out of scope.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`
- `cd frontend && npm run build`
- `git diff --check`
- `docker compose up -d --build --no-deps frontend`
- Browser runtime smoke at `http://localhost:8081/`:
  - clicked `Load Orders`
  - opened order `1001`
  - filled item name `Item-813008`
  - clicked `Add Item`
  - verified the item appears in the detail panel
  - verified no framework overlay and no browser warning/error logs
- MySQL proof:
  `SELECT item_id, order_id, item_name FROM market_order_item WHERE order_id = 1001 AND item_name = 'Item-813008';`
  returned `1783093814663 / 1001 / Item-813008`.

## Runtime Notes

The Browser plugin `domSnapshot()` helper still fails in this environment with
`o.incrementalAriaSnapshot is not a function`. Runtime proof used Browser
locators, page evaluation, console collection, screenshot capture, and MySQL
row verification instead.

## Residual Risk

`querydatadetail` still returns an empty `items` collection for this smoke view,
so the Vue panel only shows items added during the current session. Full
FoolFrame detail child-table rendering/edit/delete parity remains open.
