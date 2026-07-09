# View Order Token Gate

## Prompt

Continue the Docker/Vue FoolFrame migration while keeping the goal focused on
View-first behavior and avoiding concrete business DTO binding.

## Scope

- Rechecked the `DataQueryService` query/report ordering path after the View
  rendering concern.
- Added a focused regression test proving an explicit order token that is only
  present as a hidden Model property cannot drive SQL ordering when it is absent
  from the loaded View's rendered items.
- Removed the fallback from View token resolution to arbitrary Model properties.
  Unknown explicit order tokens now fall back to the existing default rendered
  View ordering.
- Reused the existing `propertyByViewToken` and default order path; no new DTO
  mapping protocol or parser was added.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T06-53-21Z-view-order-token.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceOrderingTest#queryLegacyViewDataIgnoresExplicitOrderOutsideRenderedViewItems test`
  failed because the hidden `hiddenRank` Model property was used for SQL
  ordering.
- GREEN: same focused command passed after removing the hidden Model property
  fallback.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `docker compose build backend`
- GREEN: `docker compose up -d backend frontend`
- GREEN: `python scripts/runtime_doctor.py`

The first runtime doctor run immediately after container recreation saw
backend `Up 3 seconds` and failed with connection reset / frontend 502 while
Spring Boot was still starting. Backend logs showed `Started Application` after
6.471 seconds; rerunning the same doctor after startup passed all checks.

## Risks

- This intentionally rejects explicit order tokens that are not represented by
  the loaded View. If a legacy screen requires hidden sort keys, that View
  metadata needs to expose the key instead of relying on raw Model properties.
