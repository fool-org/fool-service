# FoolFrame Server Coverage

Date: 2026-07-15

Status: Scope complete

This inventory closes the server side of the migration contract defined in
`foolframe-parity.md`. It covers 469 compiled C# source entries across the 12
production projects referenced by `Soway.Server.sln`; `SCPB07.TESTS` is test
input and is not counted as a production migration surface.

## Completion Boundary

The migration is complete when behavior reachable through the old Web route
table, the public `IDataService` contract, installed metadata, or a configured
runtime module has a Spring/Java/Vue replacement and repeatable validation.
Completion does not require carrying forward an obsolete host or an unused
implementation detail when the target architecture deliberately replaces it.

The locked public boundary contains:

- 25 registered Express routes in `src/Web/app.js`;
- 25 `IDataService` operations in `Soway.Server/IDataService.cs`;
- 469 compiled C# source entries in the production Server projects;
- 43 old application frontend assets, covered separately by
  `foolframe-frontend-assets.md`;
- 118 imported runtime Views, covered by `foolframe-view-matrix.md`.

Run `python scripts/legacy_migration_contract.py --require-legacy` to compare
the tracked contract with the adjacent `../FoolFrame` checkout and the current
Spring routes, Vue page checks, coverage documents, and View matrix.

## Project Inventory

| FoolFrame project | Compiled C# files | Current owner | Completion evidence |
| --- | ---: | --- | --- |
| `SCPB01-Soway.Data` | 45 | `fool-common` (53 Java files) | Data annotations, property types, tree/graph helpers, expression evaluation, dynamic and collection contracts are covered by focused tests. |
| `SCPB02-Soway.DB` | 24 | `fool-dao` (21 Java files) | DAO mapping, SQL generation, transaction carriers, enum conversion and global connection routing are covered; SQL Server command generation is replaced by JDBC/MySQL. |
| `SCPB03 -Soway.DB.Manage` | 15 | `fool-db-manage` (16 Java files) | Database/source catalogs, legacy password payloads, routed SQL execution and working-database lifecycle are covered. |
| `SCPB05-Soway.Model` | 115 | `fool-model` (33), `fool-view` (50), `fool-app-manage` (52) | Model/property/relation metadata, dynamic mutation, triggers, operations, View generation, module discovery and schema installation are covered by module tests and runtime acceptance. |
| `SCPB07-Soway.AppManage` | 5 | `fool-app-manage` | Application/store-database definitions and lookup/create behavior are covered. |
| `SCPB08-Soway.AppManage` | 2 | `fool-app-manage` | Application installation, metadata persistence, DDL, menus, roles and routed transaction boundaries are covered. |
| `SCPB09-SOWAY.EVENT` | 20 | `fool-event` (45 Java files) | Event definitions, object matching, recipients, records, scheduler and message delivery are covered. |
| `SWDQ01-Soway.Query` | 46 | `fool-query` (49 Java files) | Query tables/columns, joins, filters, parameters, SQL generation and JDBC execution are covered. |
| `SWRPT01-Soway.Report` | 31 | `fool-report` (31 Java files) | Report definitions, matrices, static totals and flat-grid rendering are covered. |
| `SWUA01-SOWAY.ORM.AUTH` | 5 | `fool-auth` (38 Java files) | User/login/hash behavior and legacy wrappers are covered. |
| `SWUA02-SOWAY.ORM.AUTH` | 13 | `fool-auth`, `fool-app-manage` | Menu, role, company, department and authorized-user metadata/factories are covered. |
| `Soway.Server` | 148 | `fool-auth`, `fool-view`, `business-application`, Vue | All 25 `IDataService` operations and all old Web proxy routes have target handlers or intentional legacy no-op responses; runtime doctor exercises the public workflow. |

Java file counts are current owner-module inventories, not one-to-one class
translations and not additive across rows that share a target module.

## Intentional Platform Replacements

- WCF hosting, `XmlDataService`, Express proxying, Jade, Angular and RequireJS
  are replaced by Spring REST, Nginx, Vue 3 and Vite.
- SQL Server-specific contexts and command generators are replaced by the
  Docker MySQL/JDBC runtime. Legacy metadata, query and mutation semantics are
  preserved at the framework boundary rather than by retaining two SQL stacks.
- Thrift-generated token/session adapters are replaced by the authenticated
  Spring/Redis boundary. Old Web request aliases remain at the HTTP boundary.
- Old members that throw `NotImplementedException`, expose unsupported setters,
  or intentionally return a no-op keep that observable behavior where the
  public contract reaches them; they do not justify speculative new features.
- Placeholder/test/debug files with no reachable production behavior are
  inventory evidence, not standalone product requirements.

## Reopen Rule

Migration status returns to incomplete only when one of these occurs:

- the adjacent FoolFrame route, service-operation, or production-project
  snapshot changes;
- a current imported View or configured module reaches an unsupported legacy
  behavior;
- a source/runtime comparison demonstrates a concrete observable mismatch;
- a required migration gate stops passing.

Absent one of those conditions, new framework, Agent, security, or product
work is post-migration development rather than unfinished FoolFrame migration.
