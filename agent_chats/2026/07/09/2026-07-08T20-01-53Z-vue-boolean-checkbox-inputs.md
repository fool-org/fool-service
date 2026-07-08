# Vue Boolean Checkbox Inputs

## Prompt

Continue the Docker/Vue FoolFrame migration, keeping rendering View-first and
avoiding page bindings to concrete business DTOs.

## Scope

- Reused the shared Vue metadata input helpers so Boolean property metadata
  and legacy `EditType=CheckBox` render as native checkboxes.
- Kept widget selection driven by View field metadata, not field names such as
  `active`.
- Kept frontend save payload values as legacy strings, then coerced Boolean
  string values in `ModelDataService.columnValue` by `PropertyType.Boolean`
  before dynamic persistence.
- Rebuilt backend and frontend Docker services and reran the runtime doctor
  against the refreshed Compose stack.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/MetadataFieldEditor.vue`
- `frontend/src/payload.test.ts`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-01-53Z-vue-boolean-checkbox-inputs.md`

## Validation

- RED: `npm test -- viewWorkflow.test.ts`
  - Failed because Boolean / CheckBox metadata still mapped to `text`, and
    `fieldInputChecked` was not implemented.
- RED: `npm test -- payload.test.ts`
  - Failed because `MetadataFieldEditor.vue` did not bind checkbox checked
    state through a shared helper.
- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataCoercesLegacyBooleanStringByPropertyType test`
  - Failed with MySQL `Data truncation: Data too long for column 'ACTIVE' at row 1`.
- GREEN: `npm test -- viewWorkflow.test.ts`
  - `35` focused View workflow helper tests passed.
- GREEN: `npm test -- payload.test.ts`
  - `59` payload/editor source checks passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataCoercesLegacyBooleanStringByPropertyType test`
  - Focused Boolean persistence regression passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am test`
  - `fool-model` module test set passed.
- GREEN: `npm test`
  - `97` frontend tests passed.
- GREEN: `npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
  - No whitespace errors.
- GREEN: `docker compose up -d --build backend frontend`
  - Rebuilt backend and frontend images and recreated both containers.
- GREEN: `python scripts/runtime_doctor.py`
  - Compose, auth shell, View/data, inputquery, report, message, notify, and
    logout checks passed.
- GREEN: `docker compose ps`
  - Backend, frontend, MySQL, and Redis were running; MySQL and Redis were
    healthy.
- GREEN: `curl http://localhost:8080/test`
  - Returned `200 465`.
- GREEN: `curl http://localhost:8081/`
  - Returned `200 399`.

## Risks

- The frontend still submits Boolean checkbox values as strings to preserve
  the legacy metadata editor payload contract. Backend dynamic persistence now
  owns the Boolean coercion for `PropertyType.Boolean`.
