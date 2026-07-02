# View Item Property Metadata

## Prompt

- Continue the active migration goal:
  1. run the environment through Docker
  2. finish migration against `../FoolFrame`
  3. keep the frontend on Vue
  4. commit atomically

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/ListView/HandlerGetListView.cs`
  and `ListView/ViewItem.cs`.
- Migrated the list-view item fields that the legacy handler actually assigns:
  `PropertyType` and `PropertyModel`.
- Left legacy DTO fields such as `PropertyId`, `EditViewId`, `EditExp`, and
  `ViewFile` out of this slice because the inspected handler does not fully
  populate them in the current path.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/TableColumnInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesLegacyColumnPropertyTypeAndModel,ViewDataServiceTest#getViewDataAttachesLegacyModelPropertyMetadataToItems test`
  failed as expected because `ViewItem` did not expose legacy property metadata.
- GREEN:
  same focused Maven command passed with 2 tests.
- Module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed.
- Frontend:
  `npm test` passed 1 file / 3 tests.
- Frontend build:
  `npm run build` passed.
- Docker:
  `docker compose up -d --build backend frontend` rebuilt and restarted the
  backend/frontend services.

## Runtime Evidence

- `docker compose ps` showed backend/frontend running, with MySQL and Redis
  healthy.
- `curl http://localhost:8080/test` returned the seeded order rows.
- `curl http://localhost:8081/` returned the Vue HTML.
- `curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}'
  http://localhost:8080/api/v1/view/get-view | jq '{propertyTypes:
  [.data.tableColumn[].propertyType], propertyModels:
  [.data.tableColumn[].propertyModel]}'` returned:

```json
{
  "propertyTypes": ["Long", "String", "String"],
  "propertyModels": [0, 0, 0]
}
```

## Risks

- This slice exposes Java/Jackson enum names for `PropertyType`, consistent
  with the current Vue API shape for other view enums.
