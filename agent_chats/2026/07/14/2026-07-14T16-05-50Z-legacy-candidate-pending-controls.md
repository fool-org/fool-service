# Legacy Candidate Pending Controls

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `detailView.jade` and `querylistdata.js` with the Vue existing-item
  picker.
- Removed the Vue-only pending lock from picker Find, Select, pagination,
  header close, backdrop dismissal, and Cancel interactions.
- Kept candidate View loading, detail save, child mutation, and report pending
  boundaries unchanged.
- Reused the existing View metadata, shared table, and shared paginator; added
  no state, request path, or DTO binding.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T16-05-50Z-legacy-candidate-pending-controls.md`

## Validation

- `cd frontend && npm test` passed: 15 files, 180 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Backend `/test` passed.
- `python scripts/runtime_doctor.py` passed all 67 checks.
- `ViewDetailPanel.vue` shrank from 467 to 464 lines; the new focused contract
  test is 17 lines instead of extending the 1,600-line payload test.
- Deployed frontend image:
  `sha256:2d110390d9ab522530b5247621453d51fea2a14168ddde651eb6d5967728e0d1`.
- Authorized Docker browser acceptance logged in as `admin`, opened
  `/view102/1001`, and entered the metadata-driven Items candidate picker.
  After an initial query loaded four rows, the backend was paused and a second
  `querydata` request was started. At 650ms Find, Close, Cancel, all four Select
  commands, and Page 1 remained enabled.
- While candidate requests were pending, Cancel closed immediately; selecting
  `2002 / Existing fee` closed the picker and staged the row locally; clicking
  Page 1 kept the picker open; Close then dismissed it. A reload removed the
  unsaved staged row, and MySQL still mapped item `2002` to order `1002`.
- Physical pointer clicks on the modal backdrop closed the picker both normally
  and during a pending query, covering PrimeVue's mousedown/up mask contract.
- The backend was unpaused after each request. The final page stayed on
  `/view102/1001` with no picker or error dialog; Compose, backend `/test`, and
  all 67 runtime-doctor checks passed afterward.

## Risks And Follow-ups

- `docs/superpowers/` is unrelated untracked work and remains untouched.
