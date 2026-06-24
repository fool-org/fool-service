# Report unsupported surfaces

## Prompt

- Continue the active migration goal after the Docker permission retry and keep committing scoped migration slices.

## Scope

- Compared `../FoolFrame/src/Server/SWRPT01-Soway.Report/Report.cs`, `ReportResult.cs`, `ReportResultTable.cs`, and `ReportResultTableColumn.cs`.
- Migrated the legacy unsupported getter/no-op setter contract for report result/source/audit fields and result table surfaces.

## Changes

- Kept `Report.Name`, `Report.Params`, `Report.ID`, and `Report.No` as supported stored properties.
- Changed `Report.Result`, `Report.Source`, `Report.CreateTime`, `Report.CreatePerson`, `Report.ModifyTime`, and `Report.MoidiyPerson` getters to throw `UnsupportedOperationException`; setters are no-ops.
- Changed all `ReportResult`, `ReportResultTable`, and `ReportResultTableColumn` getters to throw `UnsupportedOperationException`; setters are no-ops.
- Updated `ReportMigrationTest` to separate supported report definition fields from legacy unsupported surfaces.
- Updated `docs/migration/foolframe-parity.md` to record the migrated unsupported report surfaces.

## Validation

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#reportKeepsLegacyUnsupportedGetterNoOpSetterSurface+reportResultKeepsLegacyUnsupportedGetterNoOpSetterSurface+reportResultTableKeepsLegacyUnsupportedGetterNoOpSetterSurface -DfailIfNoTests=false test`
  - Result: failed because the current Java data classes returned stored values instead of throwing.
- Green test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#reportKeepsLegacyUnsupportedGetterNoOpSetterSurface+reportResultKeepsLegacyUnsupportedGetterNoOpSetterSurface+reportResultTableKeepsLegacyUnsupportedGetterNoOpSetterSurface -DfailIfNoTests=false test`
  - Result: passed.
- Focused report migration suite:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest -DfailIfNoTests=false test`
  - Result: passed.
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

- Full reactor `mvn test` and frontend `npm test` were not rerun for this report-only surface change; the Docker image package and frontend Docker build passed, and runtime smoke passed.

## Follow-ups

- Continue remaining report work around table source adapters and query/export integration around the rendered report grid.
