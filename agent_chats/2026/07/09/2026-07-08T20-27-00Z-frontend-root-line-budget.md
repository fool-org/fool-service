# Frontend Root Line Budget

## Prompt

Continue the Docker/Vue FoolFrame migration with file size and reuse control.

## Scope

- Kept the existing repository-wide source file limit at 2100 lines because an
  existing Java migration test is still 2098 lines.
- Added a stricter `frontend/src/App.vue` harness limit of 2000 lines now that
  the View/data workflow has been extracted.
- Updated validation docs and task state so future frontend work keeps moving
  logic into reusable helpers instead of growing the root Vue component.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/validation.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-27-00Z-frontend-root-line-budget.md`

## Validation

- GREEN: `python scripts/check_repo_harness_test.py`
- GREEN: `python scripts/check_repo_harness.py`

## Risks

- This is a guardrail only; it does not reduce `App.vue` further by itself.
  The next frontend slices should continue extracting cohesive workflow logic.
