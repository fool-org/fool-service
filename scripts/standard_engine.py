#!/usr/bin/env python3
"""Repository standard catalog for fool-service harness checks."""

from __future__ import annotations

from dataclasses import asdict, dataclass


@dataclass(frozen=True)
class Standard:
    id: str
    title: str
    owner_doc: str
    enforcement: str
    evidence: str
    repair_path: str
    revision_trigger: str


STANDARDS: dict[str, Standard] = {
    "STD-HARNESS-001": Standard(
        id="STD-HARNESS-001",
        title="Agent entrypoints and source-of-truth docs stay discoverable",
        owner_doc="docs/standards/STD-HARNESS-001.md",
        enforcement="python scripts/check_repo_harness.py",
        evidence="AGENTS.md, docs/validation.md, docs/standards/README.md",
        repair_path=(
            "Restore the missing entrypoint, command, or marker and link it "
            "from the repository harness docs."
        ),
        revision_trigger=(
            "Update when a new source-of-truth surface replaces AGENTS.md, "
            "docs/validation.md, or docs/standards/."
        ),
    ),
    "STD-VALIDATION-001": Standard(
        id="STD-VALIDATION-001",
        title="Validation stays layered by change type",
        owner_doc="docs/standards/STD-VALIDATION-001.md",
        enforcement="python scripts/check_repo_harness.py and focused local commands",
        evidence="docs/validation.md matrix, command output, optional JSON report",
        repair_path=(
            "Add the missing command to the matrix or run the heavier "
            "escalation gate when a change crosses runtime boundaries."
        ),
        revision_trigger=(
            "Update when Maven, frontend, Docker, or migration validation "
            "commands change."
        ),
    ),
    "STD-EVIDENCE-001": Standard(
        id="STD-EVIDENCE-001",
        title="Meaningful changes include durable delivery evidence",
        owner_doc="docs/standards/STD-EVIDENCE-001.md",
        enforcement="review plus python scripts/check_repo_harness.py",
        evidence="agent_chats entry, validation commands, artifact paths, risks",
        repair_path=(
            "Create or update the matching agent_chats entry, and record any "
            "skipped validation with the reason and residual risk."
        ),
        revision_trigger=(
            "Update when review repeatedly cannot reconstruct why a change was "
            "safe to ship."
        ),
    ),
    "STD-MIGRATION-001": Standard(
        id="STD-MIGRATION-001",
        title="FoolFrame migration parity stays traceable",
        owner_doc="docs/standards/STD-MIGRATION-001.md",
        enforcement="docs/migration/foolframe-parity.md review and focused tests",
        evidence="migration parity notes, Maven/frontend test output, Docker smoke checks",
        repair_path=(
            "Update the parity document in the same change that migrates or "
            "defers a legacy FoolFrame workflow."
        ),
        revision_trigger=(
            "Update when a legacy workflow is replaced, descoped, or split into "
            "a new migration phase."
        ),
    ),
}


def standard_catalog_payload() -> dict[str, dict[str, str]]:
    return {
        standard_id: asdict(standard)
        for standard_id, standard in sorted(STANDARDS.items())
    }


def standard_reference(standard_id: str) -> dict[str, str]:
    standard = STANDARDS[standard_id]
    return {
        "standard_id": standard.id,
        "standard_title": standard.title,
        "standard_doc": standard.owner_doc,
        "standard_evidence": standard.evidence,
        "repair_path": standard.repair_path,
    }


def main() -> int:
    import json

    print(json.dumps(standard_catalog_payload(), indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
