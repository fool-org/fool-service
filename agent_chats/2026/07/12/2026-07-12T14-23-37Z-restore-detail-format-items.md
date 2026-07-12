# Restore Detail Format Items

## Prompt

Continue aligning old page layout, style, and interaction, with every behavior
committed atomically.

## Scope

- Keep Format ViewItems in detail simple fields and collection groups.
- Preserve existing list-table Format-to-row-class behavior.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- This delivery evidence file.

## Validation

- Dockerized JDK 17 Maven reactor test for `ViewDataAdapterTest`: 14 tests
  passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose build backend`: passed.
- Rebuilt backend deployed; `python scripts/runtime_doctor.py` passed all
  Docker auth, View/data, detail/new/save, child, lookup, report, and message
  checks.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

## Source Evidence

- Both typed and dynamic overloads of FoolFrame
  `DataFormator.IObjectProxyToDetail` iterate all non-array ViewItems for
  `SimpleData` and all array ViewItems for collection groups without checking
  `EditType`.
- FoolFrame list rendering separately treats edit type 10 as the row CSS class
  and omits it from visible list cells. That list-only behavior remains intact.

## Risks

- The current Docker seed has no Format ViewItem, so this legacy edge is
  pinned by focused adapter tests while the full runtime doctor guards broader
  View/detail regressions.
