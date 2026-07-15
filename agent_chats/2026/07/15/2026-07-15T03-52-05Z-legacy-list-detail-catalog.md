# Legacy List Detail Catalog Acceptance

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits. Trace each page from its legacy View template to data and avoid
binding shared Vue components to concrete business DTOs.

## Scope

- Traced old list row identity from `HandlerQueryData` and
  `ObjectProxyClass.ID` before changing the runtime data path.
- Reconciled imported model identities, display properties, joined detail
  predicates, and normalized relation target columns with physical tables.
- Added a compatible auto `SysId` only for the one strict editable-list
  candidate that had no model id property: ApplicationDatabase.
- Scanned all imported list Views and every data-backed default-detail route.
- Browser-verified five representative repaired catalog routes through real
  row clicks with the authorized local CAPTCHA and `admin/admin` credentials.

## Changed Files

- `docker/mysql/init/009-legacy-model-runtime.sql`
- `docker/mysql/init/012-legacy-list-model-identity.sql`
- `docker/mysql/init/013-legacy-list-property-storage.sql`
- `docker/mysql/init/014-legacy-editable-list-identity.sql`
- `docker/mysql/init/015-legacy-normalized-catalog-relations.sql`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceCompositeKeyTest.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceJoinedDetailTest.java`
- `fool-query/src/main/java/org/fool/framework/query/CompareFilter.java`
- `fool-query/src/test/java/org/fool/framework/query/CompareFilterTest.java`
- `scripts/runtime_schema.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T03-52-05Z-legacy-list-detail-catalog.md`

## Commits

- `6b6b82ce fix(model): honor legacy auto sys id reads`
- `b56521d3 fix(model): bind imported physical primary keys`
- `2c6f4427 fix(docker): restore imported list row identities`
- `6833056e fix(model): reconcile imported display properties`
- `c14a7a78 fix(model): retain explicit sysid detail reads`
- `08f7dbb4 fix(model): qualify joined detail identities`
- `278e5b46 fix(docker): identify editable legacy list rows`
- `160aec00 fix(model): align normalized catalog relations`
- `dad7619f test(runtime): accept physical catalog identities`

## Validation

- Full Maven reactor: `BUILD SUCCESS`, 16 modules, 46.366 seconds.
- `PYTHONPATH=scripts python -m unittest scripts/runtime_doctor_test.py`
  (50 tests passed).
- `python scripts/check_repo_harness.py`.
- `python scripts/runtime_doctor.py` (69 checks passed).
- Replayed migrations 014 and 015 twice; both remained idempotent.
- Imported list API scan: 60/60 passed.
- Data-backed default-detail scan: 47/47 passed.
- Frontend tests/build were not rerun because this slice changed no frontend
  source; the deployed frontend remains the previously validated image.

## Runtime Evidence

- Real row clicks opened ApplicationDatabase `/view122/1`, View
  `/view108/217`, Property `/view144/1499`, Model `/view146/181`, and
  EventDefinition `/view134/a23e90cd-596b-4403-b89a-ba1d7a583e9f`.
- All five routes rendered detail fields; View and Model rendered their
  metadata collections. No error dialog opened.
- The 390x844 ApplicationDatabase list had document width 390px and a 328px
  table viewport, with horizontal access contained inside the table.
- Visible evidence:
  - `artifacts/runs/20260715-legacy-detail-catalog/application-database-list.png`
    (`3b9e217229aff010e1f5452fecec9cf6a141393479eeb46166b39cb9af142dd9`).
  - `artifacts/runs/20260715-legacy-detail-catalog/application-database-mobile-list.png`
    (`9b36e81473a755c5f745615807612a685ded647ec1b2e76eaf9b37e41bd5f13b`).
- Backend image:
  `sha256:72885b14ed031835c43f2e5ad7aceda1f709e9ec74696046da741d5d55c25318`.
- Backend JAR SHA-256:
  `9756b6962495976d0dd9a5e44a38f408f2ed8ea43b7c3332bd73cd984fce6a89`.
- Compose services were running and `db-migrate` was `Exited (0)`.

## Risks And Follow-ups

- The browser acceptance proves read navigation and rendered detail state, not
  mutation parity for all imported catalog Views.
- Next, classify all 118 imported Views by legacy template and interaction
  surface, then run reversible writes only where the old View is editable.
- The standard Compose build remains blocked by Docker Buildx failing to update
  its local activity file (`operation not permitted`). The documented clean
  Java-17 JAR/image fallback was used for runtime acceptance.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.
