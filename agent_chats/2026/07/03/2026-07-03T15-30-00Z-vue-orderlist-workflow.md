# Vue OrderList Workflow

## Prompt

The user challenged that the frontend was still mostly components and basically
unusable while the active migration goal requires Docker runtime, FoolFrame
parity, and Vue frontend progress.

## Scope

- Replaced the default Vue landing surface with a focused `OrderList` workflow.
- Kept the raw endpoint panels available behind `API Tools` instead of making
  them the first screen.
- Added browser-tested list, select, detail, and save behavior against the
  Docker-seeded order data.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- `cd frontend && npm test`
- `cd frontend && npm run build`
- `git diff --check`
- `docker compose up -d --build frontend`
- `docker compose ps`
- `curl http://localhost:8081/`
- `curl http://localhost:8080/test`
- Browser runtime smoke at `http://localhost:8081/`:
  - default page title: `OrderList`
  - clicked `Load Orders`
  - verified 5 rendered rows including order `1001`
  - opened order `1001`
  - saved symbol/state through `Save Order`
  - verified detail fields show `Order ID 1001`, `Symbol BTC-USDT`, and
    `State Open`
  - verified the default DOM no longer contains the hidden `Auth Session` API
    tools text
  - verified browser error logs were empty

## Runtime Notes

`docker compose up -d --build frontend` also rebuilt the backend because
Compose dependency traversal was not disabled. The rebuild completed
successfully, and the stack reported backend on `8080`, frontend on `8081`,
MySQL healthy on `3307`, and Redis healthy on `6380`.

The Browser plugin `domSnapshot()` helper failed with an internal compatibility
error (`o.incrementalAriaSnapshot is not a function`), so the runtime proof used
Browser locators, page evaluation, and console collection instead.

## Residual Risk

This is not full legacy web parity. It makes the first migrated Vue workflow
usable for Docker-seeded `OrderList` data while the broader Jade/list/detail/
report UX and richer generated-form behavior remain migration work.
