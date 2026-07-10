# Remove Dead SelectStream Placeholder

## Prompt

Continue the FoolFrame migration while controlling file size and maximizing
reuse.

## Scope

- Audit backend placeholder implementations before treating them as parity
  gaps.
- Remove `fool-dao`'s unused `SelectStream` rather than implementing a custom
  proxy for the JDK `Stream` interface.
- Record the result in the migration and task sources of truth.

## Changed Files

- `fool-dao/src/main/java/org/fool/framework/dao/SelectStream.java` (deleted)
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-47-10Z-dead-select-stream.md`

## Evidence

- Repository search found no `SelectStream` call site.
- `../FoolFrame/src` contains no `SelectStream` counterpart.
- The deleted 210-line class implemented every `Stream` method as `null`, an
  empty result, or a no-op.

## Validation

- `docker compose build backend` passed the complete 15-module Java 17 Maven
  reactor package build.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- Host `mvn -pl fool-dao -am test` could not run because the host JDK reports
  `invalid target release: 17`; the Java 17 Docker build is the replacement
  compilation gate for this deletion.

## Risk

No runtime behavior changes because the deleted class had no callers. Tests
inside the backend image build are skipped by the existing Dockerfile package
command.

## Follow-up

Continue the backend placeholder audit one candidate at a time. Do not convert
guard-path `null` values or legacy-matching unsupported operations into new
abstractions without a live caller and a FoolFrame behavior to migrate.
