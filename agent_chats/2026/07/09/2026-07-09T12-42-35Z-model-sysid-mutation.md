# Prompt

Continue the Docker/FoolFrame/Vue migration, keep commits atomic, maximize
reuse, and avoid broad rewrites.

# Scope

Close one concrete `SCPB05-Soway.Model` runtime mutation gap: models without an
explicit id property should still mutate and run filter commands against the
legacy `SYSID` key.

# Changes

- Added a focused regression test for save/update plus SAVE-trigger `Filter`
  execution on a runtime model whose metadata has no id property.
- Reused the model service's existing id resolution path with a `SYSID`
  fallback.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataAndTriggerFilterUseSysIdWhenModelHasNoIdProperty test`
  failed with `expected:<true> but was:<false>`.
- Green:
  same command passed with `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Final:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest test`
  passed with `Tests run: 36, Failures: 0, Errors: 0, Skipped: 0`.
- `python scripts/check_repo_harness.py` passed.

# Runtime Evidence

The focused test logs the generated trigger filter SQL using `SYSID`:
`WHERE 1=1  AND (\`SYSID\`= ?) And (\`ORDER_NAME\` = 'allowed')`.

# Risks

This does not add business-object POJO conversion for assembly handlers or
cross-routed transaction behavior; those remain separate migration work.

# Follow-ups

- Continue `SCPB05-Soway.Model` parity only for concrete collection,
  external-model, or routed-transaction cases found against FoolFrame.
