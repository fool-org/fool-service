# Installation and Initialization

This document is the source of truth for the fool-service installation flow.
`tasks.md` tracks delivery status; migration parity remains in
`docs/migration/foolframe-parity.md`.

## Responsibilities

The startup installation boundary has three stages:

1. **System initialization** runs after the database migration prerequisite.
   It discovers configured framework model packages, installs module/model
   metadata, creates missing model and relation tables, and creates missing
   default views.
2. **Default application installation** optionally runs after system
   initialization and reconciles the configured application, store-database
   relation, application-authorized administrator, system menus, and
   administrator role through `AppInstaller.createApp(...)`.
3. **Application installation** provisions any additional application through the
   existing `AppInstaller.createApp(...)` flow: application metadata, system
   and authorization modules, authorized user, store-database user modules,
   default system views, menus, and the administrator role.

Docker schema migrations remain responsible for creating and repairing the
metadata tables needed before Java initialization can run. Java initialization
does not replay or replace `docker/mysql/init/*.sql`. The database migration
also owns the login credential for the configured administrator; application
installation links that existing identity into application authorization.

For an empty Docker MySQL volume, the effective order is:

1. MySQL entrypoint creates the configured database and applies the ordered SQL
   catalog.
2. `db-migrate` replays the same repeat-safe catalog and must exit successfully.
3. Spring Boot installs missing framework metadata, tables, and default Views.
4. The enabled default-application stage creates or reuses the application,
   database relation, administrator authorization, menus, and role.

## System Initialization Order

The startup path executes these phases in order:

1. Discover `@Table` model types from the configured root and dependency
   packages through `ReflectiveAppModuleSource`.
2. Install missing module, model, property, relation, enum, operation, trigger,
   and declared View metadata into the metadata database.
3. Generate and execute idempotent model/relation DDL against the data
   database.
4. Generate and persist missing default item/list Views after installed model
   ids are available.
5. Emit one summary containing discovered model, metadata, DDL, and View
   counts.

An error in any phase fails startup. Starting with partially repaired metadata
is less safe than surfacing the installation failure.

## Configuration

The Spring Boot auto-configuration prefix is `fool.app.initialization`.

| Property | Purpose |
| --- | --- |
| `enabled` | Enables the startup runner. It is enabled by the Docker application profile. |
| `module-name` | Name of the root system module. |
| `module-remark` | Human-readable root-module description. |
| `module-version` | Installed module version. |
| `root-package` | Root package scanned for system models. |
| `dependency-packages` | Additional system packages scanned and installed before the root module. |
| `metadata-connection` | Optional legacy/JDBC route for metadata; blank uses the primary `DataSource`. |
| `data-connection` | Optional legacy/JDBC route for model tables; blank uses the primary `DataSource`. |
| `default-application-enabled` | Installs and reconciles the default application after system initialization. |
| `default-application-id` / `default-application-key` | Default application identity and login key. |
| `default-application-name` / `default-application-version` | Default application display metadata. |
| `default-administrator-id` | Existing login identity linked as the application administrator. |
| `default-database-id` / `default-database-name` | Store-database identity exposed by application login. |
| `default-view-id` | Default application landing View. |

The default package catalog covers model, View, application/auth, database,
and event framework metadata. Business packages are opt-in and belong in an
application-specific installation plan.

## Idempotency and Reconciliation

- Modules are matched by module name.
- Models are matched by Java class name.
- Code-owned property structure is repaired when its reflected type, mapping,
  key, nullability, or owner metadata drifts; presentation fields remain
  operator-managed.
- Relations use null-safe matching because legacy collection relations may not
  have a source column. Views reuse their existing name-based check.
- Generated table DDL uses create-if-missing semantics.
- A pre-existing one-to-many relation column is treated as an already-applied
  step, so initialization can resume after a partially completed MySQL DDL run.
- Existing metadata is preserved; startup initialization does not delete
  removed classes or overwrite operator-managed records.
- Application, store database, application/database relation, authorized user,
  menus, roles, and their relations are matched before insertion, so a restart
  resumes a partially completed default-application installation.

Destructive reconciliation, module upgrades, and removing stale metadata are
separate migration operations and are intentionally outside automatic startup.

## Acceptance

- Starting the Docker backend after `db-migrate` installs framework models that
  are not present in the static Market seed.
- A second backend start creates no duplicate module, model, View, property, or
  relation rows, application/menu/role rows, or authorization relations and
  performs no no-op property rewrites.
- Focused tests prove phase ordering, configured package discovery, default
  disabled library behavior, and enabled runner wiring.
- `python scripts/check_repo_harness.py` remains green.
