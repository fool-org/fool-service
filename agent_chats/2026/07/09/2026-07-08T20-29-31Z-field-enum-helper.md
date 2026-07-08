# Field Enum Helper

## Prompt

Keep the Vue migration focused on View-first rendering and data loading, while
controlling file size and code reuse.

## Scope

- Moved metadata-driven field enum option loading out of `App.vue` and into
  `useFieldEnums`.
- Kept enum lookup keyed by rendered View field `PrpModelId`, with model-id
  caching so duplicate enum fields do not repeat `getenums` calls.
- Kept the manual API Tools `getenums` panel separate from the automatic
  metadata editor option loading.
- Reduced `App.vue` from 1941 to 1922 lines while keeping the View-first
  detail and child editor behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/useFieldEnums.ts`
- `frontend/src/useFieldEnums.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-29-31Z-field-enum-helper.md`

## Validation

- GREEN: `cd frontend && npm test`
- GREEN: `cd frontend && npm run build`
- GREEN: `docker compose up -d --build frontend`
- GREEN: `python scripts/runtime_doctor.py`
- GREEN: `curl -s -o /tmp/fool_frontend.html -w 'frontend %{http_code} %{size_download}\n' http://localhost:8081/`
  returned `frontend 200 399`, with `/assets/index-pkKnb8oi.js`.
- GREEN: `curl -s -o /tmp/fool_backend_test.json -w 'backend %{http_code} %{size_download}\n' http://localhost:8080/test`
  returned `backend 200 465`.
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`

## Risks

- This slice only extracts enum option loading reuse. It does not address the
  larger legacy owner-expression gap for `#.` dynamic data expressions.
