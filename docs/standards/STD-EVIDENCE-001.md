# STD-EVIDENCE-001: Meaningful Changes Include Durable Delivery Evidence

## Intent

Another agent or reviewer should be able to reconstruct what changed, why it
changed, what was validated, and what risk remains.

## Scope

Meaningful changes under:

- Java modules
- `frontend/`
- `docker/`
- `docs/migration/`
- `docs/standards/`
- `scripts/`
- CI workflows

## Automatic Enforcement

`review plus python scripts/check_repo_harness.py` enforces the presence of the
ledger guidance and checks that the delivery evidence surface is discoverable.
Review enforces whether a specific code change needs a ledger entry.

## Evidence

An `agent_chats/YYYY/MM/DD/YYYY-MM-DDTHH-MM-SSZ-topic.md` entry should record
prompt, scope, changed files, validation commands, runtime artifact paths,
skipped checks, risks, follow-ups, and linked commits when available.

## Repair Path

Create or update the matching ledger entry in the same logical change. If
validation is skipped or downgraded, state the exact command, reason, residual
risk, and next validation command.

## Revision Trigger

Revise this standard when repeated reviews cannot reconstruct delivery evidence
or when the ledger schema changes.
