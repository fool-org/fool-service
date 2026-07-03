# Tighten Source Size Harness

## Prompt

- Continue the Docker/FoolFrame/Vue migration.
- Keep file sizes under control and avoid pushing sloppy growth.
- Commit atomically.

## Scope

- Lowered the source-size harness limit from 2200 to 2100 lines.
- Updated the focused harness unit test and validation docs to match.
- Updated the original source-size evidence note so the recorded limit stays
  consistent with the current harness.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/validation.md`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-07-58Z-source-size-harness.md`
- `agent_chats/2026/07/04/2026-07-03T19-38-12Z-tighten-source-size-harness.md`

## Validation

- `python3 scripts/check_repo_harness_test.py && python3 scripts/check_repo_harness.py`
  passed.
- `git diff --check`
  passed with no output.
- Source line scan showed the current largest source file at 2062 lines and
  `frontend/src/App.vue` at 1990 lines, both below the new 2100-line limit.

## Skipped Checks

- Maven and frontend builds were not rerun because this slice only changes the
  Python harness, docs, task state, and evidence.

## Risks / Follow-ups

- The 2100-line limit is still a pragmatic cap, not a substitute for continuing
  to extract clear View workflow helpers/components when there is a real
  boundary.
