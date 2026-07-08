# Vue Record Map Boundary

## Prompt

先查 View 渲染页面，再根据 View 查 data；页面不能和具体业务 DTO 绑定。

## Scope

- Tightened the Vue render boundary after rechecking the existing
  `getlistview(ViewId)` -> `querydata(ViewId)` and detail
  `getreaditemview(ViewId)` -> `querydatadetail(ViewId)` flows.
- Removed the unused generic record-map table helpers that could infer columns
  and row keys from arbitrary object DTO keys.
- Removed `ListDataItem.values` from the frontend TypeScript contract. Runtime
  legacy payloads may still carry extra fields, but frontend rendering helpers
  only read row `Items` and View metadata.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/useChildCandidates.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `cd frontend && npm test -- payload.test.ts` failed on
  `does not keep generic record-map table helpers` because
  `recordColumns` still existed.
- Green: `cd frontend && npm test -- payload.test.ts viewWorkflow.test.ts useChildCandidates.test.ts`.
- Green: `cd frontend && npm test`.
- Green: `cd frontend && npm run build`.

## Skipped Checks

- Maven and Docker runtime checks were not rerun because this slice only changes
  the Vue render/type boundary and migration evidence docs.

## Risks

- Backend compatibility payloads still serialize `values`; this change only
  prevents the Vue app from depending on them for rendered tables.
