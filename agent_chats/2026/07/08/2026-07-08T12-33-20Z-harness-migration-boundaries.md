# Harness migration boundaries

## Prompt

Continue the FoolFrame migration with Docker/Vue parity, atomic commits,
maximum reuse, and guardrails against migration drift.

## Scope

- Added Java package-boundary checks to the existing repository harness for
  `fool-*` modules.
- Added required migration parity markers for the active FoolFrame remaining
  areas and runtime doctor entrypoint.
- Added focused stdlib tests for both new harness checks.
- Updated validation, standards, task state, and parity notes.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/validation.md`
- `docs/standards/STD-HARNESS-001.md`
- `docs/standards/STD-MIGRATION-001.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `python scripts/check_repo_harness_test.py`: passed, 3 tests.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.

## Runtime Evidence

- Not run; this change only updates repository harness and docs.

## Risks

- Package-boundary enforcement is based on current module package prefixes.
  If a module is intentionally split or renamed, update the harness mapping in
  the same change.
