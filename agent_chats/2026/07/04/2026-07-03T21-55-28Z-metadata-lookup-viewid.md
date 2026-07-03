# Prompt

User called out that the migrated flow must render from View metadata first,
then query data through that View context, and that binding the page to concrete
business DTOs is wrong.

# Scope

Tighten the Vue metadata lookup path so BusinessObject candidate search follows
the currently rendered View id instead of carrying a `viewName` fallback from
the component layer.

# Changes

- Removed the `viewName` prop/default from `frontend/src/MetadataFieldEditor.vue`.
- Stopped forwarding `viewName` into `buildInputQueryRequest` from metadata
  lookup editors.
- Added a frontend test guard that the lookup component sends `viewId` and does
  not contain `viewName`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md` to mark the
  frontend ViewId lookup tightening.

# Validation

- `cd frontend && npm test -- --run`
  - 3 files passed, 48 tests passed.
- `cd frontend && npm run build`
  - `vue-tsc --noEmit` and `vite build` passed.

# Runtime Evidence

No Docker rebuild was required for this frontend-only source and test guard.
The previous runtime contract remains the same: the main workflow calls
`getlistview(ViewId)`, `querydata(ViewId)`, and `querydatadetail(ViewId)`, while
metadata lookup now also uses the rendered `ViewId`.

# Risks

The backend still accepts legacy `ViewName` on `inputquery` for protocol
compatibility. That fallback is intentionally retained at the DTO/API boundary,
but it is no longer used by the Vue metadata lookup component.

# Follow-ups

Continue auditing API-tool-only defaults separately from the primary Vue View
workflow so demo payloads do not get mistaken for the migration architecture.
