# Vue Order Create Workflow

## Prompt

Continue the active migration goal: keep Docker running, compare against
`../FoolFrame`, migrate parity forward, keep Vue as the frontend, and commit
atomically.

## Legacy Reference

- `../FoolFrame/src/Web/app.js` exposes `GET /new:id` and `POST /data/new`.
- `../FoolFrame/src/Web/routes/index.js` routes new-object initialization into
  `detailView` with `opt:'new'`.
- `../FoolFrame/src/Web/Cloud-Social/soway.js` sends `initnew` with
  `Token`, `ViewId`, and `ParentObjId`, then `savenewobj` with `Token`,
  `SaveObj`, `OwnerViewId`, `OwnerId`, and `Property`.

## Scope

- Added `New Order` to the default Vue `OrderList` workflow.
- Reused existing `initnew`, `savenewobj`, and `saveobj` payload builders.
- Kept raw endpoint panels under `API Tools`.
- Rendered list cells from legacy row `fmtValue`, so enum values show
  `Open` / `Filled` instead of raw `0` / `1`.

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
- `docker compose ps`
- `curl http://localhost:8081/`
- `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":5,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- Browser runtime smoke at `http://localhost:8081/`:
  - clicked `Load Orders`
  - clicked `New Order`
  - filled symbol `QA-578367-USDT`
  - selected state `Filled`
  - clicked `Create Order`
  - verified row `1783093577687 / QA-578367-USDT / Filled`
  - verified detail fields show `Order ID 1783093577687`,
    `Symbol QA-578367-USDT`, and `State Filled`
  - verified default DOM does not contain hidden `Auth Session`
  - verified browser warning/error logs were empty

## Runtime Notes

The Browser plugin `domSnapshot()` helper still fails in this environment with
`o.incrementalAriaSnapshot is not a function`. Runtime proof used Browser
locators, page evaluation, console collection, and screenshot capture instead.

## Residual Risk

This closes the default `OrderList` create/edit happy path only. It does not
complete FoolFrame's generated detail form, nested item editing, report builder,
or arbitrary view rendering parity.
