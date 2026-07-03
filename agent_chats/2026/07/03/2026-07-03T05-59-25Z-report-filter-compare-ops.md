# Report FilterExp Compare Ops

## Prompt

- Continue the active migration goal: Docker runtime, FoolFrame parity,
  Vue frontend, and timely atomic commits.

## Scope

- Extended simple legacy report `FilterExp` handling from equality-only to
  the seeded legacy compare catalog IDs 1-7.
- Added support for `包含` by emitting a `LIKE '%value%'` raw filter for the
  existing report query path.
- Mapped legacy `FilterExp.Col.Name` and `FilterExp.Col.ID` tokens through
  view/model metadata to physical DB columns before composing the raw filter.
- Kept complex/composite `BoolExp` trees out of scope for this slice.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T05-59-25Z-report-filter-compare-ops.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test` failed because `包含` still threw `Only simple equality FilterExp is supported.`
- RED: Docker runtime smoke for `FilterExp.Col.Name = symbol` initially
  returned `{"code":-1,...}`; backend logs showed
  `SQLSyntaxErrorException: Unknown column 'symbol' in 'where clause'`, so
  property-name filters needed view/model metadata mapping to DB columns.
- RED: after adding an ID-only regression test,
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=ReportControllerTest test`
  failed because numeric `FilterExp.Col.ID` was not allowed through metadata
  lookup.
- PASS: focused `ReportControllerTest` after compare, metadata-column, and
  ID-token mapping fixes: 5 tests, failures 0, errors 0.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  passed with 65 tests, failures 0, errors 0.
- PASS: `docker compose up -d --build backend` rebuilt the backend image and
  restarted `fool-service-backend-1`.
- PASS: `curl -fsS http://localhost:8080/test` returned the seeded order rows.
- PASS: backend report smoke with `FilterExp.Col.Name = symbol` and
  `CompareOp.ID = 7` returned `code:0`, `totalRecords:1`, and `BTC-USDT`.
- PASS: frontend proxy report smoke with `FilterExp.Col.Name = symbol` and
  `CompareOp.ID = 7` returned `code:0`, `totalRecords:1`, and `BTC-USDT`.
- PASS: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT id,name,\`column\`,owner FROM fool_sys_model_property WHERE name='symbol' OR \`column\`='order_symbol' LIMIT 5;"` confirmed seeded property `symbol` has ID `1002` and DB column `order_symbol`.
- PASS: backend report smoke with ID-only `FilterExp.Col.ID = 1002` and
  `CompareOp.ID = 7` returned `code:0`, `totalRecords:1`, and `BTC-USDT`.
- PASS: frontend proxy report smoke with ID-only `FilterExp.Col.ID = 1002`
  and `CompareOp.ID = 7` returned `code:0`, `totalRecords:1`, and `BTC-USDT`.
- PASS: `docker compose ps` showed backend, frontend, MySQL, and Redis all
  running; MySQL and Redis were healthy.
- PASS: `git diff --check`
- PASS: `python scripts/check_repo_harness.py`

## Skipped Checks

- Frontend unit/build checks were not rerun because no frontend files changed.

## Risks And Follow-Ups

- Composite `FirstExp` / `Sequences`, saved report metadata, and export wiring
  remain open report parity work.
