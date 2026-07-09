# Vue Sudoku Linechart Panels

# Prompt

Continue the Docker/FoolFrame/Vue migration, improving the frontend runtime
while keeping View metadata before data binding and reusing existing code.

# Scope

- Legacy `../FoolFrame/src/Web/views/includes/linechart.jade`.
- Vue Sudoku child-panel rendering.
- Existing `legacyChartData` chart projection.

# Changes

- Rendered Sudoku `linechart` child panels from the child panel's loaded
  `querydata` rows.
- Reused `legacyChartData`, so chart values still come only from legacy chart
  `EditType` metadata instead of concrete business DTO fields.
- Left unsupported Sudoku `Map`, `Group`, and `Item` panels as explicit empty
  states until their legacy behavior is implemented.
- Updated parity notes, task state, and delivery evidence.

# Validation

- Red:
  `cd frontend && npm test -- payload.test.ts`
  failed because Sudoku `linechart` rendering was missing.
- Green:
  `cd frontend && npm test -- payload.test.ts`
  passed `65` tests.
- Green:
  `cd frontend && npm test`
  passed `119` tests.
- Green:
  `cd frontend && npm run build`.
- Green:
  `python scripts/check_repo_harness.py`.
- Green:
  `git diff --check`.
- Green:
  `docker compose up -d --build frontend`; compose also rebuilt backend with
  `mvn -DskipTests package` and the Maven reactor ended `BUILD SUCCESS`.
- Green:
  `docker compose up -d --no-deps --force-recreate frontend && docker compose ps`.
- Green:
  `python scripts/runtime_doctor.py`.

# Risks

- The runtime doctor does not seed a browser-visible Sudoku `linechart` page.
  The guard for this slice is a frontend source test plus the shared chart data
  tests already covering legacy chart `EditType` projection.

# Follow-ups

- Implement Sudoku `Map`, `Group`, or `Item` panels only when the next slice
  can match their FoolFrame controller behavior without guessing.
