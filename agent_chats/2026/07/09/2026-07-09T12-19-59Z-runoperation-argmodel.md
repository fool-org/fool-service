# runoperation ArgModel Parity

## Prompt

Continue the Docker/FoolFrame migration goal with small, reusable, atomic
changes.

## Scope

Closed one concrete external-model parity gap: legacy runoperation now honors
operation-level `ArgModel` / `ArgFilter`, not only command-level
`ExuteOutModelMethod`.

## Changes

- `DataQueryService.runLegacyOperation` detects an operation-level target
  model and executes the operation against that target data object.
- Target object id comes from `ArgFilter` evaluated against the source row.
- Existing command evaluation is reused with target metadata and source values.
- Added a regression test proving target-model update and preventing accidental
  source-object persistence.
- Updated `tasks.md` and the FoolFrame parity log.

## Validation

- Failing proof before implementation:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyOperationWithArgModelExecutesAgainstTargetModel test`
  failed before the service routed operation-level `ArgModel`.
- Passing focused proof after implementation:
  same command passed with `Tests run: 1, Failures: 0, Errors: 0`.
- Passing runoperation suite:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest test`
  passed with `Tests run: 22, Failures: 0, Errors: 0`.
- Runtime smoke:
  `python scripts/runtime_doctor.py` passed all Docker auth/View/data/save/report/message checks.

## Runtime Evidence

- Docker Compose services were already up.
- Runtime doctor still passed `data:runoperation-aliases`, proving the Docker
  runoperation surface remains healthy after the target-model branch.

## Risks

- No distributed transaction is added here; this follows the existing
  single-service persistence boundary.

## Follow-ups

- Remaining external-model work should be driven by a concrete new legacy
  surface beyond operation-level `ArgModel` and command-level
  `ExuteOutModelMethod`.
