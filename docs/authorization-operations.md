# Authorization Operations

This runbook covers the Phase 4 authorization and Agent safety controls defined
by `authorization-and-agent-risk-control.md`.

## Request Authentication

- Protected endpoints accept only `Authorization: Bearer <token>`.
- Legacy request-body `token` / `Token` values are ignored and cannot
  authenticate a request.
- Agent sessions store the owner user, application, and database scope. They do
  not store a reusable bearer token.
- A protected request without a valid bearer token fails closed with a stable
  reason code.

## Policy Freshness

`PolicyVersionQuery` computes a fingerprint from the explicit policy version
and the microsecond `UPDATED_AT` values of permissions, bindings, and data
policies. Authorization and Action Request rechecks query this fingerprint
directly; there is no TTL decision cache that can keep a stale HIGH action
executable.

When a policy changes, an existing preview or approval fails with
`POLICY_CHANGED` and returns to `PREVIEW_READY`. Generate a fresh preview and
repeat confirmation or approval after reviewing the new effective scope.

## Audit Integrity and Alerts

`FOOL_SECURITY_AUDIT_EVENT` is an append-only SHA-256 chain serialized through
the `primary` row in `FOOL_SECURITY_AUDIT_HEAD`. Each event stores its sequence,
previous hash, and event hash in the same transaction as the chain-head update.

The application verifies the chain every five minutes. An authorized operator
can also run:

```http
GET /api/v1/authz/audit-integrity
Authorization: Bearer <operator-token>
```

An integrity failure creates a deduplicated, unacknowledged CRITICAL
`AUDIT_INTEGRITY` row in `FOOL_SECURITY_ALERT`. Do not repair a mismatch by
rewriting hashes. Preserve a database snapshot, isolate the affected runtime,
identify the first reported event, and follow the incident-recovery process.

## Catalog and Permission Drift

Application startup compares executable Action Catalog entries with registered
`ControlledActionHandler` keys. Startup fails with
`ACTION_CATALOG_HANDLER_DRIFT` when either side has an unmatched executable
action. Non-executable catalog entries cannot be opened through configuration.

Review current bindings at least quarterly and after every privileged-role
change:

```bash
python scripts/authorization_review.py --strict \
  > artifacts/runs/<run_id>/authorization-review.json
```

Strict review fails for orphaned bindings, disabled permissions that remain
bound, and wildcard permissions. Privileged bindings are always listed for
human review.

## Runtime Validation

Run the normal runtime doctor, then the reversible security regression:

```bash
python scripts/runtime_doctor.py
python scripts/authorization_security_regression.py \
  --report-json artifacts/runs/<run_id>/security-regression.json
```

The security regression restores the changed binding timestamp and tampered
audit field exactly, and deletes its temporary Action Request. It proves:

- policy changes invalidate an already-previewed action on the next request;
- an audit-field modification is detected, raises a CRITICAL alert, and the
  exact field restoration makes the chain valid again;
- concurrent audit writes/verifications keep unique, gap-free chain sequences.

The browser role matrix uses four identities: ordinary user, department
administrator, independent approver, and system administrator. It must verify
both visible UI capability and the corresponding network response; hidden
buttons alone are not authorization evidence. HIGH actions still require
step-up and independent approval for system administrators.
