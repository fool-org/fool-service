# 2026-07-09 AppInstall Remaining-Work Reconciliation

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep code small, maximize reuse, and avoid speculative migration code.

## Scope

- Rechecked `ReflectiveAppModuleSource` against FoolFrame
  `AssemblyModelFactory.GetRefrenceModules`.
- Confirmed current Java coverage already includes recursive file/jar package
  scanning, referenced model package traversal, module dependency ordering, and
  DBMaps metadata/runtime coverage called out elsewhere in the parity map.
- Trimmed the stale AppInstall remaining-work wording to the only explicit
  uncovered difference: arbitrary Java classpath dependency enumeration beyond
  explicit package/model references.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-12-17Z-appinstall-remaining-reconciliation.md`

## Validation

- `python scripts/check_repo_harness.py` passed.
- `python scripts/runtime_doctor.py` passed, including Docker compose,
  FH_JAVA `market_symbols`, View-first legacy schema, auth shell, View/data,
  report, message, and logout checks.
- `git diff --check` passed.

## Skipped Checks

- No Maven/frontend tests: documentation-only reconciliation, no production or
  frontend code changed.

## Risks

- The remaining Java classpath enumeration gap is intentionally not solved here;
  adding a new module catalog or classpath scanner API should wait for a real
  runtime/install path that needs it.
