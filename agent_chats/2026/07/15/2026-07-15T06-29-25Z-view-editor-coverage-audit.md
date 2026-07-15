# View Editor Coverage Audit

## Prompt

Continue aligning old layout, style, and interactions in atomic commits. Start
from the rendered View, then inspect its data, and do not bind UI to business
DTOs.

## Scope

- Enumerated every current ViewItem before considering another frontend
  editor or widget.
- Corrected the audit join to use `view_model + model_property name`, matching
  the normalized catalog instead of treating the property name as an id.
- Compared the reachable property/edit types with old `setextype.js` and the
  shared Vue `MetadataFieldEditor` / `fieldInput` paths.
- Added no runtime source because the current catalog exposes no unsupported
  editor branch.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T06-29-25Z-view-editor-coverage-audit.md`

## Validation

- Normalized ViewItem/model-property join: 927/927 rows matched.
- Editable ViewItems: 465 rows across PropertyType
  `0/1/3/8/11/14/15/16/18/21`; all use ItemEditType `TextBox=1`.
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Evidence

- Old `setextype.js` selects enum, Boolean, date/time, numeric, and
  BusinessObject controls from PropertyType before its ordinary text fallback.
- Vue's shared editor covers enum Select, Boolean checkbox, datetime-local,
  constrained integer/long text, BusinessObject lookup, and plain text.
- PropertyType MD5 and Guid correctly stay on the old plain-text fallback.
- Collection-valued BusinessObject properties remain owned by the existing
  View-derived child-group path rather than scalar DTO-specific controls.
- MySQL retained admin id 1, 8 orders, and 4 order items during the audit.

## Risks And Follow-ups

- This is catalog coverage, not a claim that every possible FoolFrame enum
  value is reachable. Reopen editor implementation only when a real imported
  View introduces a currently absent property/edit combination.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.
