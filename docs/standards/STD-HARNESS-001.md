# STD-HARNESS-001: Agent Entrypoints And Source-Of-Truth Docs Stay Discoverable

## Intent

An agent should be able to enter the repository, find the authoritative docs,
run the minimum validation command, and understand where to record delivery
evidence without reading the entire project.

## Scope

- `AGENTS.md`
- `docs/validation.md`
- `docs/standards/`
- `scripts/check_repo_harness.py`
- `.github/workflows/repo-harness.yml`

## Automatic Enforcement

`python scripts/check_repo_harness.py` checks required files, headings, markers,
standard docs, command surfaces, CI wiring, Java package boundaries, Vue
View/data rendering boundaries, migration parity markers, and the source file
line budget.

## Evidence

The checker reports the files it inspected, the active standard catalog, errors,
and warnings. Use `--report-json` when evidence needs to be stored under
`artifacts/runs/<run_id>/`.

## Repair Path

Restore the missing entrypoint, command, heading, marker, standard document,
package declaration, Vue View/data boundary, parity marker, or workflow step.
Split oversized source files before adding more behavior. If a source-of-truth
surface intentionally
moves, update `AGENTS.md`, `docs/validation.md`,
`docs/standards/README.md`, and `scripts/standard_engine.py` in the same
change.

## Revision Trigger

Revise this standard when the repository adopts a different first-read
entrypoint, validation owner, CI surface, or standard catalog.
