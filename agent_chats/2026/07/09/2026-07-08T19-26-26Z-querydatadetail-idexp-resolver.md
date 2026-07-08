# QueryDataDetail IdExp Resolver

## Prompt

Continue the FoolFrame migration while keeping the runtime flow View-first:
render/load View metadata first, then query data from that View context. Do not
bind View rendering or data lookup to concrete business DTOs.

## Scope

- Reused `OperationCommandValueResolver` for legacy `querydatadetail.IdExp`
  resolution instead of adding another expression parser.
- Kept detail lookup on the existing View-first path:
  `getViewData(viewId)` -> `view.getViewModel()` -> `getOneData(model, id)`.
- Added focused coverage for a static math `IdExp` resolving to the object id
  used by detail data loading.
- Left object/property expressions that require current object context out of
  scope because this handler resolves the object id before any detail object is
  loaded.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-26-26Z-querydatadetail-idexp-resolver.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceDetailTest test`
  - Failed because `$1000+$1` was treated as literal id `1000+$1`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceDetailTest test`
  - `6` focused detail tests passed.

## Risks

- `querydatadetail.IdExp` expressions that depend on a current object or
  property still cannot resolve during object-id selection because there is no
  detail object yet. The implementation preserves the blank-id fallback for
  unresolved expressions rather than inventing a business DTO context.
