# Query selected-column IList surface

## Prompt

- Continue the active FoolFrame migration goal and keep changes scoped with TDD plus atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/SWDQ01-Soway.Query/SelectedColCollection.cs`.
- Migrated the legacy direct `Insert(index,item)` and `RemoveAt(index)` collection surfaces for selected output columns.

## Changes

- Added `SelectedColumnCollection.insert(int, SelectedColumn)`.
- Added `SelectedColumnCollection.removeAt(int)`.
- Added tests proving these methods exist and preserve the legacy direct-list behavior without reindexing existing `SelectedIndex` values.
- Updated `docs/migration/foolframe-parity.md` to record selected-column direct insert/remove-at parity.

## Validation

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest#insertKeepsLegacyDirectListInsertSurface+removeAtKeepsLegacyDirectListRemovalSurface -DfailIfNoTests=false test`
  - Result: failed because `SelectedColumnCollection` did not expose legacy `insert` and `removeAt` methods.
- Green test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest#insertKeepsLegacyDirectListInsertSurface+removeAtKeepsLegacyDirectListRemovalSurface -DfailIfNoTests=false test`
  - Result: passed.
- Focused selected-column collection suite:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=SelectedColumnCollectionTest -DfailIfNoTests=false test`
  - Result: passed.
- Frontend Vue validation:
  - `cd frontend && npm test && npm run build`
  - Result: passed; Vitest ran 2 tests and Vite production build completed.
- Post-slice Docker runtime refresh:
  - `docker compose up -d --build`
  - `docker compose up -d --force-recreate backend`
  - `docker compose ps`
  - `curl -sf http://localhost:8080/test`
  - `curl -sfI http://localhost:8081/`
  - Result: backend and frontend images built; backend container recreated; backend returned seeded order JSON; frontend returned `HTTP/1.1 200 OK`.

## Runtime Evidence

- Compose services after final runtime refresh:
  - `fool-service-backend-1`: Up on `0.0.0.0:8080->8080/tcp`
  - `fool-service-frontend-1`: Up on `0.0.0.0:8081->80/tcp`
  - `fool-service-mysql-1`: healthy on `127.0.0.1:3307->3306/tcp`
  - `fool-service-redis-1`: healthy on `127.0.0.1:6380->6379/tcp`

## Risks

- Full reactor `mvn test` was not rerun; the Docker image package build passed with `-DskipTests`, focused query tests passed, frontend test/build passed, and runtime smoke passed.

## Follow-ups

- Continue remaining `SWDQ01-Soway.Query` saved-query/report execution surfaces and richer query-to-view integration.
