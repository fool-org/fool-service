# Validation Harness

This document defines the minimum command surface for fool-service changes.
It keeps local validation, CI, and delivery evidence aligned without requiring
the heaviest runtime path for every edit.

## Change-Type Matrix

| Change type | Minimum check | Escalation |
| --- | --- | --- |
| Harness, standards, docs, task state | `python scripts/check_repo_harness.py` | Run JSON/JUnit output when wiring CI or artifacts |
| Harness script changes | `python scripts/check_repo_harness_test.py && python scripts/check_repo_harness.py` | Add one stdlib unittest for each new harness rule |
| Backend Java module | `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn test` after `docker compose up -d` | Focus with `-pl <module> -am -Dtest=<TestClass>` when the scope is isolated |
| Frontend Vue workflow | `cd frontend && npm test && npm run build` | Browser smoke through Docker when API contracts or runtime routing changes |
| Docker/runtime stack | `docker compose up -d --build`, then `python scripts/runtime_doctor.py` | Capture logs and add an artifact bundle under `artifacts/runs/<run_id>/` |
| Authorization, Agent actions, or audit | Backend and frontend checks above, `python scripts/runtime_doctor_test.py`, and `PYTHONPATH=scripts python -m unittest scripts/authorization_review_test.py` | Run the strict permission review, reversible security regression, and four-identity UI/network matrix before release acceptance |
| FoolFrame migration parity | `python scripts/legacy_migration_contract.py --require-legacy` plus focused backend/frontend tests | Run `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` when View routing, templates, or catalog metadata change; run the full Docker/Maven/frontend bundle before changing completion status |

## Command Surface

- `python scripts/check_repo_harness.py`
- `python scripts/check_repo_harness_test.py`
- `python scripts/check_repo_harness.py --report-json artifacts/runs/<run_id>/repo-harness.json`
- `python scripts/check_repo_harness.py --junit-out artifacts/runs/<run_id>/repo-harness.xml`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn test`
- `cd frontend && npm test && npm run build`
- `docker compose up -d --build`
- `docker compose ps -a` (`db-migrate` must finish with exit code `0`)
- `python scripts/runtime_doctor.py`
- `python scripts/runtime_doctor_test.py`
- `PYTHONPATH=scripts python -m unittest scripts/authorization_review_test.py`
- `python scripts/authorization_review.py --strict` (run at least quarterly and archive the JSON result)
- `python scripts/authorization_security_regression.py --report-json artifacts/runs/<run_id>/security-regression.json`
- `python scripts/legacy_migration_contract.py --require-legacy`
- `PYTHONPATH=scripts python -m unittest scripts/legacy_migration_contract_test.py`
- `PYTHONPATH=scripts python -m unittest scripts/legacy_view_matrix_test.py`
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py`

## Report Outputs

- Human output goes to stdout/stderr.
- JSON reports use `--report-json` and include `status`, `root`,
  `checked_files`, `standards`, `errors`, and `warnings`.
- JUnit reports use `--junit-out` for CI systems that aggregate XML.
- Source file size checks fail source files over 2100 lines, with
  `frontend/src/App.vue` capped at 2000 lines.
- Repository harness checks fail Java package boundary drift across `fool-*`
  modules, Vue render paths that infer columns/cells from business DTO
  `row.values`, missing required migration parity markers, and drift in the
  closed FoolFrame Web/API/project inventory.
- Runtime evidence should use `artifacts/runs/<run_id>/` when a browser,
  Docker, log, or HTTP observation is decisive.
- Authorization operations, audit-integrity response, policy invalidation, and
  the four-identity browser matrix follow `docs/authorization-operations.md`.

## CI Gates

The repository harness gate lives at `.github/workflows/repo-harness.yml` and
runs `python scripts/check_repo_harness.py`.

Keep CI and local commands consistent: if a command becomes required in CI,
document it in this file and make the failure actionable locally.

## Database Startup

`db-migrate` replays the idempotent `docker/mysql/init/*.sql` catalog after
MySQL becomes healthy. The backend depends on its successful completion, so
the same schema repair path applies to fresh and existing Docker volumes and a
failed migration prevents the application from starting on stale schema.

## Fallback / Skip Policy

Skipped validation is not equivalent to passed validation. Evidence must record:

- the exact command skipped,
- the missing prerequisite or reason,
- the remaining risk,
- the next command that should be run when the prerequisite is available.
