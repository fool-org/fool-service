# Prompt

Continue the FoolFrame migration, with the explicit constraint that migrated
screens should render from View metadata first, then query data through that
View context. Binding the flow to concrete business DTOs is wrong.

# Scope

- Keep the current Vue/View workflow view-id driven.
- Add the next small FoolFrame model-layer parity slice without broad frontend
  churn.
- Avoid growing large files or adding a second operation-command expression
  parser.

# Changes

- `fool-model/src/main/java/org/fool/framework/model/model/PropertyTrigger.java`
  - Added a runtime model for legacy `SW_SYS_PROPERTY_TRIGGER` rows.
- `fool-model/src/main/java/org/fool/framework/model/model/Property.java`
  - Changed property trigger metadata to `List<PropertyTrigger>` with an empty
    default list.
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
  - Hydrates property triggers and trigger commands from
    `SW_SYS_PROPERTY_TRIGGER` / `SW_SYS_PROPERTY_TRIGGER_COMMANDS`.
  - Executes property `SET` trigger `SET_VALUE` commands on dynamic create/save
    writes before column values are persisted.
  - Reuses the existing model-trigger command value resolver.
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
  - Added MySQL-backed proof that a legacy property `SET` trigger updates the
    saved row value.
- `tasks.md`
  - Marked the property trigger `SET_VALUE` slice complete.
- `docs/migration/foolframe-parity.md`
  - Recorded the parity increment and narrowed the remaining trigger backlog.

# Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerSetValue test`
  - Passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -DfailIfNoTests=false test`
  - Passed.
- `cd frontend && npm test -- --run`
  - Passed: 3 test files, 49 tests. Existing tests verify View metadata loads
    before `querydata` and no business-name query shortcut is used.
- `python3 scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.
- `docker compose up -d --build backend`
  - Passed; Docker backend build completed with Maven reactor `BUILD SUCCESS`.
- `python3 scripts/runtime_doctor.py`
  - Passed: compose services, `/test`, `getlistview`, `querydata`,
    `inputquery`, and `getmkqview`.

# Runtime Evidence

- `docker compose ps`
  - `fool-service-backend-1`: Up, port `8080`.
  - `fool-service-frontend-1`: Up, port `8081`.
  - MySQL and Redis healthy.

# File Size Check

- `ModelDataService.java`: 736 lines.
- `ModelDataServiceTest.java`: 1245 lines.
- `frontend/src/App.vue`: 1999 lines.

# Risks

- This slice covers property `SET` triggers with `SET_VALUE` commands only.
- ItemsAdd/ItemsDelete property triggers, non-`SET_VALUE` commands, trigger
  filters, and external/WCF/JSON invocation paths remain backlog.
- The current runtime smoke proves the stable View workflow, not a browser
  click-through of every operation button.

# Follow-ups

- Continue migration from legacy rendered View behavior first, then data/query
  semantics, instead of deriving workflows from seed business objects.
