# Sudoku ListViewType Runtime Proof

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep View-first rendering/data binding, maximize reuse, control file size,
  and commit atomically.

## Scope

- Compared FoolFrame `HandlerGetListView`, which sets `ListViewType` from
  `item.ListView.ViewType`.
- Removed the migrated backend's hard-coded `ListViewType=0` output.
- Added Docker seed data for a real Sudoku View/template fixture.
- Extended the runtime doctor to prove the fixture through the Vue proxy.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/model/ViewItem.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `docker/mysql/init/006-view.sql`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-26-30Z-sudoku-list-view-type.md`

## Behavior

- `getlistview` now returns `ListViewType` from each configured child
  `ListViewId`'s View type.
- Docker seeds `OrderSudoku` with `TempFile=Sudoku` and panel `ViewFile`
  metadata for `List`, `linechart`, `Map`, `Item`, and `Group`.
- The seeded `OrderSudokuGroup` child View exposes one list child
  (`ListViewType=0`) and one detail/simple child (`ListViewType=1`) without
  adding a business DTO shortcut.

## Red Checks

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesConfiguredLegacyLinkedViewIds test`
  - Failed before implementation because `ViewItem#setListViewType` did not
    exist and `ViewAdapter` hard-coded `ListViewType=0`.
- `cd scripts && python -m unittest runtime_doctor_test.py`
  - Failed before implementation because `sudoku_view_metadata_ok` did not
    exist.
- `python scripts/runtime_doctor.py`
  - Failed before applying the new SQL/backend image at
    `view:sudoku-template-metadata`.

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewAdapterTest#viewInfoIncludesConfiguredLegacyLinkedViewIds,ViewDataServiceTest#getViewDataHydratesLinkedListViewTypeFromChildView test`
- `cd scripts && python -m unittest runtime_doctor_test.py`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- `docker compose up -d --build backend`
- `python scripts/runtime_doctor.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`

## Risks

- Existing Docker volumes do not replay `docker/mysql/init/*.sql`
  automatically. This slice used the idempotent SQL directly before runtime
  verification; fresh Compose volumes get the same seed during MySQL init.

## Follow-ups

- Use the seeded `OrderSudoku` fixture for browser-level Vue Sudoku screenshot
  checks if visual coverage is needed beyond runtime metadata proof.
