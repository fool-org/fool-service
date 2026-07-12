# Restore Detail Parent ID

## Prompt

Keep the improved Vue presentation, but align interaction logic with
FoolFrame; query View metadata before data, avoid business DTO coupling,
control file size and reuse, and commit each behavior atomically.

## Scope

- Restore an existing child object's Owner from generic model and relation
  metadata when it is loaded directly by id.
- Emit the dynamic Owner id as legacy detail `Data.ParentId`.
- Declare the seeded Docker `OrderItem -> Order` default-owner relation in
  both model catalogs.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelOwnerLoader.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `docker/mysql/init/006-view.sql`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T14-37-08Z-restore-detail-parent-id.md`

## Validation

- Dockerized JDK 17 `ModelDataServiceTest`: 38 tests passed.
- Dockerized JDK 17 `ViewDataAdapterTest`: 15 tests passed.
- `docker compose up --force-recreate db-migrate`: completed with exit code 0.
- `docker compose build backend`: passed; the 15-module Java 17 package
  reactor completed successfully.
- `docker compose up -d --no-deps backend`: rebuilt backend deployed on port
  8080.
- `python scripts/runtime_doctor.py`: all checks passed across Compose, schema,
  auth, View/data, detail/new, save, child, lookup, report, and message flows.
- Real child-detail request through the frontend proxy returned
  `ObjId=2001`, `Model=OrderItem`, and `ParentId=1001`.
- Both `fool_sys_model.default_owner` and
  `SW_SYS_MODEL.MODEL_DEFAULTOWNER` are `100` for `OrderItem`.
- `python scripts/check_repo_harness.py`: passed, including the 2100-line
  source-file limit.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy,
  and `db-migrate` is `Exited (0)`.

The host Maven runtime is Java 8, so Java tests use the repository's JDK 17
Maven container with the shared local Maven cache.

## Source Evidence

- FoolFrame `DataFormator.IObjectProxyToDetail` writes
  `objectProxy.Owner.ID` to `ParentId`.
- FoolFrame `ObjectProxyClass.Owner` lazily calls `dbContext.GetParent`.
- FoolFrame `dbContext.GetParent` requires `Model.Owner`, locates the owner's
  collection property, reads its relation metadata, and returns no parent when
  owner metadata is absent.

## Risks

- Only models with explicit default-owner and collection relation metadata
  gain a parent, matching FoolFrame; no relation inference is attempted.
- The restored parent loads scalar fields but not its child collections,
  matching FoolFrame's non-detail parent lookup and avoiding recursive graph
  expansion.
