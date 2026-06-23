# Validation Harness

This document defines the minimum command surface for fool-service changes.
It keeps local validation, CI, and delivery evidence aligned without requiring
the heaviest runtime path for every edit.

## Change-Type Matrix

| Change type | Minimum check | Escalation |
| --- | --- | --- |
| Harness, standards, docs, task state | `python scripts/check_repo_harness.py` | Run JSON/JUnit output when wiring CI or artifacts |
| Backend Java module | `mvn test` or a focused `mvn -pl <module> test` | Full `mvn test` when shared parent POM, common modules, or Spring wiring changes |
| Frontend Vue workflow | `cd frontend && npm test && npm run build` | Browser smoke through Docker when API contracts or runtime routing changes |
| Docker/runtime stack | `docker compose up -d --build`, `docker compose ps`, `curl http://localhost:8080/test` | Capture logs and add an artifact bundle under `artifacts/runs/<run_id>/` |
| FoolFrame migration parity | Update `docs/migration/foolframe-parity.md` plus focused backend/frontend tests | Docker smoke when a migrated workflow crosses frontend, backend, database, or runtime wiring |

## Command Surface

- `python scripts/check_repo_harness.py`
- `python scripts/check_repo_harness.py --report-json artifacts/runs/<run_id>/repo-harness.json`
- `python scripts/check_repo_harness.py --junit-out artifacts/runs/<run_id>/repo-harness.xml`
- `mvn test`
- `cd frontend && npm test && npm run build`
- `docker compose up -d --build`
- `curl http://localhost:8080/test`

## Report Outputs

- Human output goes to stdout/stderr.
- JSON reports use `--report-json` and include `status`, `root`,
  `checked_files`, `standards`, `errors`, and `warnings`.
- JUnit reports use `--junit-out` for CI systems that aggregate XML.
- Runtime evidence should use `artifacts/runs/<run_id>/` when a browser,
  Docker, log, or HTTP observation is decisive.

## CI Gates

The repository harness gate lives at `.github/workflows/repo-harness.yml` and
runs `python scripts/check_repo_harness.py`.

Keep CI and local commands consistent: if a command becomes required in CI,
document it in this file and make the failure actionable locally.

## Fallback / Skip Policy

Skipped validation is not equivalent to passed validation. Evidence must record:

- the exact command skipped,
- the missing prerequisite or reason,
- the remaining risk,
- the next command that should be run when the prerequisite is available.
