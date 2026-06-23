# STD-VALIDATION-001: Validation Stays Layered By Change Type

## Intent

Validation should be cheap enough to run routinely and strong enough to catch
the risks created by the change. Docs-only work should not require a full Docker
runtime, and runtime changes should not stop at static checks.

## Scope

- `docs/validation.md`
- Maven module tests
- Frontend Vitest and build commands
- Docker Compose smoke checks
- CI repo harness workflow

## Automatic Enforcement

`python scripts/check_repo_harness.py and focused local commands` enforce the
published command matrix and verify that the repository exposes the expected
Maven, frontend, and harness command surfaces.

## Evidence

Evidence is the exact command output, optional `--report-json` or `--junit-out`
report, and any runtime artifact path recorded for Docker or HTTP smoke checks.

## Repair Path

If a change needs a command that is not in the matrix, add it to
`docs/validation.md` before relying on it. If a command cannot run, record the
skip reason and residual risk in the delivery evidence.

## Revision Trigger

Revise this standard when Maven modules, frontend scripts, Docker entrypoints,
CI gates, or migration acceptance checks change.
