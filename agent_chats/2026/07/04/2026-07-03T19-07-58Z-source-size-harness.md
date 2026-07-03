# Source Size Harness

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep file size and code reuse under control.
- Avoid adding heavy process or new dependencies.

## Scope

- Added a small source-size contract to the existing repo harness.
- The harness now checks `.java`, `.ts`, and `.vue` source files and fails any
  file over 2200 lines.
- Skips generated/build/dependency directories: `.git`, `node_modules`,
  `target`, and `dist`.
- Added one stdlib unittest that proves an oversized source file is reported.

## Changed Files

- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `docs/standards/STD-HARNESS-001.md`
- `docs/validation.md`
- `tasks.md`

## Validation

- Red test:
  `python3 scripts/check_repo_harness_test.py`
  - Failed as expected before implementation because
    `check_source_file_sizes` did not exist.
- Harness unit test:
  `python3 scripts/check_repo_harness_test.py`
  - Passed: 1 test.
- Repository harness:
  `python3 scripts/check_repo_harness.py`
  - Passed.

## Runtime Evidence

- Existing Docker stack remained running:
  `docker compose ps`
  - backend and frontend were up on ports `8080` and `8081`.
  - MySQL and Redis were healthy.

## Skipped Checks

- Maven and frontend builds were not rerun because this slice only changed
  Python harness/docs/task state.

## Risks

- The 2200-line limit is a pragmatic hard stop above the current largest file.
  It prevents further growth but does not refactor the existing large Vue file.
