# Prompt

User pointed out that page rendering should load View metadata first, then
load data from that View, and should not bind to concrete business DTOs.

# Scope

- Align `getreaditemview` response metadata with FoolFrame Pascal field names.
- Make the Vue detail editor load read-item View metadata for the active
  detail `ViewId` before merging `querydatadetail` values.
- Keep the change on shared View/data helpers instead of adding business
  DTO-specific branches.

# Changes

- Added `ViewName`, `ViewId`, `Items`, `DetailViews`, and read-item Pascal
  aliases on the Java `ReadItemView*` DTOs.
- Filled read-item `ID`, `PrpShowName`, and `PrpId`; `PrpId` now falls back to
  `ViewItem.modelProperty` when transient `Property` metadata is not hydrated.
- Added Vue helpers that convert `getreaditemview` read items into generic
  field metadata and merge `querydatadetail` values by `PrpId`.
- Changed the Vue detail workflow to use `getlistview.DetailViewId`, then
  `getreaditemview(DetailViewId)`, then `querydatadetail(DetailViewId)`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ViewAdapterTest,ViewControllerLegacyGetReadItemViewTest test`
  - Passed: 24 tests.
- `cd frontend && npm test`
  - Passed: 65 tests.
- `cd frontend && npm run build`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend frontend`
  - Passed.
- `docker compose up -d --build backend`
  - Passed after adding the `modelProperty` fallback.

# Runtime Evidence

- `python scripts/runtime_doctor.py`
  - Passed backend, frontend proxy, MySQL, Redis, `getlistview`,
    `querydata`, filtered `querydata`, `querydatadetail`, `inputquery`, and
    `getmkqview` checks.
- Direct runtime probe against both `http://localhost:8080` and
  `http://localhost:8081`:
  - `getlistview(ViewId=100)` returned `DetailViewId=102`.
  - `getreaditemview(ViewId=102)` returned Pascal keys `ViewName`, `ViewId`,
    `Items`, `DetailViews`.
  - first read item returned Pascal keys `Name`, `PrpType`, `Index`, `PrpId`,
    `PrpModelId`, `ID`, `PrpShowName`, `ReadOnly`, `EditType`.
  - first read item `PrpId=orderId` matched `querydatadetail.SimpleData[0].PrpId=orderId`.

# Risks

- `ReadItemView.DetailViews` still cannot be fully populated from the current
  Java `ViewItem` alone because it stores child view IDs, not hydrated child
  `View` objects. Child collection rendering continues to use
  `querydatadetail.Items` metadata.

# Follow-ups

- When view hydration can provide child list/edit `View` objects or a shared
  view lookup is introduced, populate `ReadItemView.DetailViews.Items` through
  the same metadata path.
