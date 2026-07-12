# Restore Detail Child View Items

## Prompt

Align Vue interaction logic with FoolFrame, load View metadata before data,
avoid concrete business DTO binding, control file size and reuse, and commit
each behavior atomically.

## Scope

- Load each detail collection's configured List View metadata.
- Build child columns and row values only from that View's ordered ViewItems.
- Remove the child-model property fallback from detail rendering.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- Focused service and adapter tests under `fool-view/src/test/`.
- `scripts/runtime_doctor.py` and `scripts/runtime_doctor_test.py`.
- `tasks.md` and `docs/migration/foolframe-parity.md`.
- This delivery evidence file.

## Validation

- Dockerized JDK 17 focused Maven reactor tests for `ViewDataServiceTest`,
  `ViewDataAdapterTest`, and `DataQueryServiceDetailTest`: 29 tests passed.
- `python scripts/runtime_doctor_test.py`: 48 tests passed.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose build backend`: passed.
- Rebuilt backend deployed; `python scripts/runtime_doctor.py` passed all
  checks, including `data:detail-items-follow-list-view` against live Docker
  View/detail metadata.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

## Source Evidence

- FoolFrame `DataFormator.IObjectProxyToDetail` assigns collection `Name` from
  `item.ListView.Name`, then iterates `item.ListView.Items` for both
  `PropertyDataItems.Properties` and every child row's `Values`.
- Each old property entry carries the ViewItem name, property id/model/type,
  readonly flag, and edit type. The migrated adapter previously enumerated the
  child model's full property list instead.

## Risks

- A collection with a missing linked List View now returns no inferred child
  columns. This is intentional: invalid metadata must not expose a business
  model DTO as a rendering fallback.
