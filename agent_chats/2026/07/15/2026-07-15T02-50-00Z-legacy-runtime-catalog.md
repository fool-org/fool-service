# Legacy Runtime Catalog Projection

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the old View templates and `setextype.js` / `savetext.js` before
  following the current View-to-data request path.
- Found 83 imported Models and 118 imported Views in `SW_SYS_*`, while the
  normalized runtime exposed only four Models and six Views.
- Projected imported Model, Property, Enum, View, and ViewItem metadata through
  idempotent SQL. Existing normalized records remain authoritative.
- Inferred missing id-property references only from legacy `IdentifyId`
  properties and aligned the backend container timezone with MySQL.
- Added a generic runtime invariant for every legacy View, ViewItem, referenced
  Model, referenced Property, and View-model id property. No business DTO or
  Vue component was added.

## Changed Files

- `docker/mysql/init/009-legacy-model-runtime.sql`
- `docker/mysql/init/009-legacy-view-runtime.sql`
- `docker-compose.yml`
- `scripts/runtime_schema.py`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T02-50-00Z-legacy-runtime-catalog.md`

## Commits

- `d59e6c45 fix(model): project legacy metadata into runtime catalog`
- `5dfedeea fix(view): project legacy views into runtime catalog`
- `9f1f5005 fix(model): infer imported legacy id properties`
- `129d67d2 fix(docker): align backend runtime timezone`
- `4aae9ddd test(runtime): guard legacy catalog projection`

## Validation

- `docker compose config -q`
- `docker compose run --rm db-migrate` applied all 12 migration files again.
- `python scripts/runtime_doctor_test.py` (49 tests passed)
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (69 checks passed)
- `cd frontend && npm test` (20 files, 218 tests passed)
- `cd frontend && npm run build`
- Java tests were not rerun because this slice changed no Java source.

## Runtime Evidence

- Catalog counts after replay: Models 83 legacy / 82 runtime, Properties
  478 / 478, Enums 137 / 137, Views 118 / 118, and ViewItems 922 legacy /
  927 runtime union. The skipped Model is duplicate-name `AuthorizedUser` id
  182, which no legacy View references.
- `mysql:legacy-runtime-catalog` reports zero missing Views, ViewItems,
  View-referenced Models, View-model Properties, and valid id properties.
- `getreaditemview(112)` returned `User详细` with 16 metadata fields before
  `querydatadetail(112,1)` returned the object. `querydata(113)` also succeeded.
- Entering edit mode rendered three native DateTime controls and the `Male`
  enum. The create-time input value was `2026-07-03T12:22:51`; MySQL and the API
  respectively exposed `2026-07-03 12:22:51` and
  `2026-07-03 12:22:51.0` after the timezone correction.
- At 1280x900 and 390x844, document width equaled viewport width and browser
  warnings/errors were empty. The token was logged out and the isolated tab was
  closed.
- MySQL retained user id 1 / login `admin`, 8 `market_order` rows, and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.25`, and price `62500`.
- Visible evidence:
  - `artifacts/runs/20260715-legacy-user-view/desktop-user-datetime.png`
    (`1280x900`, SHA-256
    `66c8da343e1419ea8e6460fd3f1695b0e8d4e72c4159202e07419b7f8d05f9c7`).
  - `artifacts/runs/20260715-legacy-user-view/mobile-user-datetime.png`
    (`390x844`, SHA-256
    `55b2709909704be90d836b1198060a2a2920759a590468a3d349252c33787d78`).

## Risks And Follow-ups

- Existing normalized rows win on id or unique-name conflicts. The runtime
  guard covers every Model referenced by a legacy View, not unused duplicate
  catalog records.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
