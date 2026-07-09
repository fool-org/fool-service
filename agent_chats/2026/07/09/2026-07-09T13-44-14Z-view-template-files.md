# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, maximize
reuse, and follow the View-first render path before data binding.

# Scope

- `fool-view` legacy `getlistview` metadata for View template files.
- Migration task-state and delivery evidence for this slice.

# Changes

- Added a no-map `View.tempFile` carrier for legacy View template filenames.
- Hydrated View-level `TempFile` and View-item `ViewFile` from
  `SW_SYS_VIEW_FILE` through the existing `ViewDataService` metadata load.
- Made `ViewAdapter` emit the hydrated `TempFile` while keeping the legacy
  empty-string default.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataServiceTest#getViewDataHydratesLegacyViewAndItemTemplateFiles test`
  failed at test compile because `ViewTempFileRow`, `ViewItemFileRow`, and
  `View.getTempFile()` did not exist yet.
- Green:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataServiceTest#getViewDataHydratesLegacyViewAndItemTemplateFiles,ViewAdapterTest#viewInfoIncludesLegacyTempFile test`
  passed.
- Downgraded:
  `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
  failed in existing DAO integration tests with `UnknownHostException: mysql`
  because the Maven container was not on the Compose network.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test`
  passed.

# Runtime Evidence

- The Docker Compose stack was already running; this slice is metadata
  hydration covered by focused and dependent module tests.

# Risks

- Vue still renders the generic View workflow for non-empty `TempFile`; full
  custom-template rendering remains future work per real migrated templates.

# Follow-ups

- Continue comparing `../FoolFrame/src/Web/views/*` render surfaces before
  adding Vue-specific template behavior.
