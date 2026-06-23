# Standard Engine

The Standard Engine turns repeated engineering judgement into versioned
repository artifacts. It is not a service. It is the combination of standards,
mechanical checks, validation output, and delivery evidence that makes quality
decisions traceable.

## Standard Object Shape

Every standard defines:

- `id`: stable handle used in docs, reports, and evidence.
- `intent`: the judgement the standard protects.
- `scope`: files, workflows, or runtime paths covered.
- `automatic enforcement`: deterministic check, test, or review gate.
- `evidence`: output or artifact that proves the standard.
- `repair path`: how to fix a failure without routing around it.
- `revision trigger`: when production failures or review findings should
  update the standard.

## Active Standards

- `STD-HARNESS-001` - agent entrypoints and source-of-truth docs stay
  discoverable.
- `STD-VALIDATION-001` - validation stays layered by change type.
- `STD-EVIDENCE-001` - meaningful changes include durable delivery evidence.
- `STD-MIGRATION-001` - FoolFrame migration parity stays traceable.

## Runtime Entry Points

- `python scripts/check_repo_harness.py`
- `python scripts/standard_engine.py`

`check_repo_harness.py` validates the required standard docs and command
surfaces. `standard_engine.py` emits the machine-readable standard catalog used
by reports and future contract checks.
