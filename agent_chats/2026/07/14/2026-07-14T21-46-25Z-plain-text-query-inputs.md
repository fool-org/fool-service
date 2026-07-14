# Plain Text Query Inputs

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the main `view.jade` and select-existing `detailView.jade` query
  controls before changing Vue; both old inputs use `type="text"`.
- Restored that type in the existing shared-workflow Vue surfaces instead of
  retaining the native-search clear affordance and browser-specific semantics.
- Preserved the intended prompt, accessible name, Enter submission, Find
  button, keyword state, View-first metadata loading, and `querydata` payload.
- Added no component, abstraction, DTO, request branch, style, or dependency.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewListPanel.test.ts`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T21-46-25Z-plain-text-query-inputs.md`

## Validation

- `cd frontend && npm test` (20 files, 205 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py` was blocked by concurrent installation
  work growing `AppManageMigrationTest.java` to 2218 lines over the 2100-line
  repository limit; this slice does not touch that file.
- Built exact implementation commit `9a68d879` from a clean archive as image
  `sha256:c3221058d8f9ef46bd230d84ffbe77ade8a6c71a6a1509d83cc01cc080f4a4b4`.
  The image contains entry bundle `index-Da9H0e9D.js`, list chunk
  `ViewListPanel-BdXrDPu5.js`, and detail chunk `ViewDetailPanel-BKUy_BG6.js`.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin` through HTTP 200.
- On `/view101`, the main query control reported DOM type `text`; Enter sent an
  HTTP-200 `querydata` request with loaded View id 101 and keyword `BTC`.
- On `/view102/1001`, Add opened the loaded `选择 Items` dialog. Its query
  control reported DOM type `text`; Enter sent an HTTP-200 `querydata` request
  with loaded candidate View id 101 and keyword `Legacy`, and the dialog stayed
  open.
- Logout returned HTTP 200 and removed the local token. The ignored browser
  screenshot is at
  `artifacts/runs/20260715-plain-text-query-inputs/candidate-query.png`.

## Risks And Follow-ups

- The legacy Jade attributes misspelled `placeholder` as `nplaceholder`; the
  existing intended `输入条件` prompt remains deliberately preserved, as does
  the already-aligned Enter interaction.
- Concurrent Agent Session, installation, app-manage, Maven, and report
  condition work remains unrelated and unstaged.
