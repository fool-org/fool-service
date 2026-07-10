# QueryDataDetail Child Property Name Alias

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Compared `../FoolFrame` `detailView.jade`, `PropertyDataItems`, and
  `ReadItemViewItem`.
- Added the legacy `Name` response alias to the shared `ListDataValue` DTO so
  `querydatadetail.Data.Items[].Properties[]` can expose child table header
  names without a separate DTO.
- Tightened the Docker runtime doctor to require named child property metadata
  from loaded detail groups.
- Updated migration parity and task-state docs.

## Changed Files
- `fool-view/src/main/java/org/fool/framework/view/dto/ListDataValue.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T02-56-45Z-querydatadetail-child-property-name.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 46 tests.
- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataAdapterTest test` passed.
- `docker compose up -d --build backend` rebuilt and restarted the backend.
- `python scripts/runtime_doctor.py` passed against Docker.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- Direct Vue-proxy proof for `POST /api/v1/data/initnew` on the loaded detail
  View returned child group `OrderItem` with `Properties[].Name` values
  `Item ID` and `Item Name`.
- `docker run ... mvn -pl fool-view -Dtest=ViewDataAdapterTest test` without
  `-am` was downgraded because Maven could not resolve sibling reactor
  artifacts (`fool-model`, `fool-report`) in isolation.

## Runtime Evidence
- Legacy FoolFrame uses `ReadItemViewItem.Name` for
  `PropertyDataItems.Properties[]` and `detailView.jade` renders
  `Properties[j].Name` in child collection table headers.

## Risks
- This expands serialized value DTOs with one alias. It does not change
  persisted data, request parsing, or Vue payload construction.
