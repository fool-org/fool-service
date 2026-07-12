# Restore Inputquery View Name

## Prompt

Keep the improved Vue presentation, align old FoolFrame interaction logic,
render from View metadata before data, avoid business DTO coupling, control
file size and reuse, and commit every behavior atomically.

## Scope

- Restore old `inputquery` View identity from rendered View metadata.
- Hydrate collection `Name` from its linked list View instead of its model.
- Keep numeric `ViewId` compatibility and precedence when metadata names are
  unavailable or both request fields are present.

## Changed Files

- Backend View metadata, detail adaptation, request DTO/service, and focused
  tests under `fool-view/src/`.
- Vue lookup payload flow and focused tests under `frontend/src/`.
- `scripts/runtime_doctor.py` and `scripts/runtime_doctor_test.py`.
- `tasks.md` and `docs/migration/foolframe-parity.md`.
- This delivery evidence file.

## Validation

- `cd frontend && npm test -- --run`: 10 files and 152 tests passed.
- `cd frontend && npm run build`: passed.
- Dockerized JDK 17 focused Maven reactor test for
  `DataQueryServiceInputQueryTest`, `DataControllerInputQueryTest`,
  `ViewDataServiceTest`, and `ViewDataAdapterTest`: 38 tests passed.
- `python scripts/runtime_doctor_test.py`: 48 tests passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose build backend frontend`: passed.
- Rebuilt backend and frontend were deployed; `python scripts/runtime_doctor.py`
  passed all Docker auth, View/data, detail/new/save, child, lookup, report,
  and message checks after backend readiness.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

The first doctor invocation ran immediately after container recreation and hit
the Spring startup window (`Connection reset by peer` followed by proxy 502s).
After `/test` became ready, the complete rerun passed.

## Source Evidence

- FoolFrame `detailView.jade` initializes `viewName` from `view.Data.Name`.
- `detailview.js` passes that name, and each collection's `data-itemview`, to
  `setextype.js`; old Web posts it as lower-case `viewid`.
- Cloud-Social `soway.inputquery` forwards that value as `ViewName`.
- Server `DataFormator.IObjectProxyToDetail` assigns detail `Name` from
  `view.Name` and collection `Name` from `item.ListView.Name`.

## Risks

- Lookup still falls back to numeric ViewId when a migrated response omits its
  metadata name. Generic View rendering and data APIs remain ViewId-only.
