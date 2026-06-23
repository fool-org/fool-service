# Harness Standard Bootstrap

## Prompt

参考 `ai-shifu`、`elab`、`elab-system`、`ai-video-studio`，在
`fool-service` 完成 harness / Standard Engine 基建，然后提交。

## Scope

- Agent entrypoint and source-of-truth navigation.
- Repo-local validation matrix and checker.
- Versioned Standard Engine docs and machine-readable catalog.
- CI wiring for the minimum repo harness gate.
- Task-state and delivery-evidence surfaces.

## Changes

- Added `AGENTS.md` as the first-read repository entrypoint.
- Added `docs/validation.md` and `.github/workflows/repo-harness.yml`.
- Added `docs/standards/` with `STD-HARNESS-001`,
  `STD-VALIDATION-001`, `STD-EVIDENCE-001`, and `STD-MIGRATION-001`.
- Added `scripts/check_repo_harness.py` and `scripts/standard_engine.py`.
- Added harness unit tests under `tests/harness/`.
- Added `tasks.md` and `agent_chats/README.md`.

## Validation

- `python3 -m unittest discover -s tests/harness -p 'test_*.py' -v`
- `python3 scripts/check_repo_harness.py`
- `python3 scripts/check_repo_harness.py --report-json /tmp/fool-service-harness-final.json --junit-out /tmp/fool-service-harness-final.xml`
- `git diff --check`

## Runtime Evidence

- No runtime browser or Docker evidence was required for this harness/docs/scripts
  slice.
- JSON report path from local validation:
  `/tmp/fool-service-harness-final.json`
- JUnit report path from local validation:
  `/tmp/fool-service-harness-final.xml`

## Risks

- Existing unrelated Docker, frontend, Maven, and migration changes remain dirty
  in the worktree and are intentionally excluded from this commit.
- `mvn test` and frontend build were not run because this change is scoped to
  harness/docs/scripts.

## Follow-ups

- Add contract checks for source size, package boundaries, and migration drift.
- Add runtime evidence bundles once Docker/browser smoke automation stabilizes.
