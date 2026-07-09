# viewWithChart Chart EditTypes

# Prompt

Continue the Docker/FoolFrame/Vue migration, keep the goal on View-first
rendering before data binding, and avoid concrete business DTO coupling.

# Scope

- Legacy FoolFrame chart row item edit-type behavior from
  `../FoolFrame/src/Web/public/javascripts/app/swchartLine.js`.
- Java `ItemEditType` parity for chart item codes `11` through `14`.
- Docker `OrderList` seed metadata used by the runtime smoke View.
- Vue chart helper recognition of numeric and enum-name chart edit types.
- Runtime-doctor proof that chart rows are exposed only through row `Items`.

# Changes

- Added explicit `ItemEditType` codes and chart constants:
  `ChartAxis(11)`, `ChartLine(12)`, `ChartBar(13)`, and `ChartScatter(14)`.
- Seeded Docker View/model metadata for `amount` and `price`, with
  `orderId` as chart axis and `amount` / `price` as bar / line series.
- Made `legacyChartData` accept both legacy numeric edit types and serialized
  enum names.
- Added a runtime-doctor check that enforces chart axis/series row `Items`
  when the loaded View has `TempFile=viewWithChart`.
- Updated task state and parity notes for this completed migration slice.

# Validation

- Red:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  failed until enum-name chart edit types were recognized.
- Red:
  `cd scripts && python -m unittest runtime_doctor_test.RuntimeDoctorTest.test_query_rows_include_legacy_chart_items`
  failed until the runtime-doctor helper existed.
- Red:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataAdapterTest#listRowsExposeLegacyChartEditTypes test`
  failed until Java chart edit-type constants existed.
- Green:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  passed `42` tests.
- Green:
  `cd scripts && python -m unittest runtime_doctor_test.RuntimeDoctorTest.test_query_rows_include_legacy_chart_items runtime_doctor_test.RuntimeDoctorTest.test_api_checks_getlistview_requires_template_metadata`
  passed `2` tests.
- Green:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataAdapterTest#listRowsExposeLegacyChartEditTypes test`.
- Green:
  `cd frontend && npm test`
  passed `114` tests.
- Green:
  `cd frontend && npm run build`.
- Green:
  `cd scripts && python -m unittest runtime_doctor_test`
  passed `33` tests.
- Green:
  `python scripts/check_repo_harness.py`.
- Green:
  `git diff --check`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed `164` tests across the Maven module slice.
- Green:
  `python scripts/runtime_doctor.py`
  passed, including `data:querydata-chart-items`.
- Environment correction:
  the same Maven module command without `--network fool-service_default`
  failed in `fool-dao` with `UnknownHostException: mysql`; it was not counted
  as product validation and was rerun with the Compose network.

# Runtime Evidence

- Applied updated Docker View seed SQL to the running MySQL container:
  `docker compose exec -T mysql sh -lc 'mysql -uroot -pPa88word car_wash < /docker-entrypoint-initdb.d/006-view.sql'`.
- Rebuilt and recreated backend/frontend containers:
  `docker compose up -d --build backend frontend` and
  `docker compose up -d --no-deps --force-recreate frontend`.
- Direct HTTP querydata proof through the Vue proxy returned row `Items` with
  `EditType` values `ChartAxis`, `ChartBar`, and `ChartLine`.
- Browser check at `http://localhost:8081/` found the `数据` / `图表` tabs,
  clicked `图表`, and verified `.legacy-chart-pane` was visible, non-empty,
  with `bar` and `line` series plus `16` rendered meter elements.

# Risks

- The Docker sample still uses order-shaped smoke rows because this repository
  does not yet include a complete production `car_wash` migration. The chart
  contract itself is View metadata driven and guarded by the runtime doctor.

# Follow-ups

- Continue the broader remaining migration work from
  `docs/migration/foolframe-parity.md`, especially complete `car_wash` schema
  migration scripts and any newly identified legacy edge surfaces.
