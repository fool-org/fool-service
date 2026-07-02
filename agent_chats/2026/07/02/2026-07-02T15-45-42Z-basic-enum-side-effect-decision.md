# BasicEnum Side-Effect Decision

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Closed the remaining migration note for the legacy `BasicEnum` constructor's
  `enum.txt` debug file side effect.
- The Java migration keeps the value registry and lookup behavior, and
  intentionally does not write `enum.txt` into the process working directory.

## Changed Files

- `docs/migration/foolframe-parity.md`

## Validation

- Source check:
  `fool-common/src/main/java/org/fool/framework/common/data/ds/BasicEnum.java`
  already records the skipped debug dump with a `ponytail:` comment.
- Prior behavior evidence:
  `agent_chats/2026/07/02/2026-07-02T14-31-20Z-legacy-basic-enum.md`
  recorded the `fool-common` BasicEnum registry migration and validation.
- Harness check:
  `python scripts/check_repo_harness.py`
- Whitespace check:
  `git diff --check`

## Risks And Follow-Ups

- Add the legacy `enum.txt` side effect only if a compatibility test or
  runtime integration proves that file is consumed.
