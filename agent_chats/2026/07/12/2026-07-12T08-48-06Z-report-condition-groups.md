# Report Condition Groups

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Group consecutive report conditions and support nested complete groups.
- Reject non-contiguous selections and partial existing-group selections.
- Split one innermost group level without flattening outer groups.
- Serialize grouping into recursive legacy `FirstExp` / `Sequences` payloads.
- Prove backend SQL parentheses for a nested composite filter.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/reportConditions.ts`
- `frontend/src/reportConditions.test.ts`
- `frontend/src/style.css`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-48-06Z-report-condition-groups.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 136 tests passed, including four recursive condition-tree tests.
  - Vue TypeScript checking and Vite production build passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ReportControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`
  - `ReportControllerTest`: 23 tests passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including report metadata, execution, and save
    routes.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Java 17 backend tests prove recursive FilterExp parentheses and mapped-column
  SQL generation.
- The deployed Docker frontend served the grouped-condition bundle and the
  report runtime routes remained healthy.

## Skipped Or Downgraded Checks

- Local Maven could not compile Java 17 sources because its active JDK returned
  `invalid target release: 17`; the same focused test was rerun successfully in
  the repository-standard Java 17 Maven container.
- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final browser acceptance must group, nest, split, and run visible conditions
  on desktop and 390x844 layouts.
