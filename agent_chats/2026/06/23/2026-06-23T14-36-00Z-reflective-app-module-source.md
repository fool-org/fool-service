# Reflective App Module Source

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModuleSource.cs` and `AssemblyModelFactory.cs`.
- Legacy behavior discovers module models from reflected business types, maps table/property metadata, creates enum models, tracks base-model and collection-property dependencies, and orders models before installation.
- Migrated the first Java equivalent for typed model classes: `ReflectiveAppModuleSource`.

## Changes

- Added `ReflectiveAppModuleSource`.
- Builds an `AppModuleDefinition` from a provided module name, remark, version, and Java model classes.
- Can also discover reflective model classes from a package using the supplied `ClassLoader`, scanning both file and jar classpath resources.
- Package-scanned sources create referenced package modules when reflected fields or base classes point at `@Table`/enum types outside the root package.
- Creates basic legacy `One2Many` relation metadata for reflective collection properties that point at another table model.
- Creates legacy `Recurve` relation metadata for reflective collection properties that point back at their owner model.
- Creates legacy bidirectional `Many2Many` relation metadata when a collection property's target model has exactly one collection property pointing back to the source model.
- Adds Java `@ReferToProperty` and `@MultiType` annotations for legacy relation-attribute parity.
- Creates legacy `ReferToProperyAttrbute` relation metadata by using the annotated target property column instead of generated owner/property id-column names.
- Creates legacy `MultiTypeAttribute` many-to-many relation metadata for annotated collections even when the target model has no reciprocal collection property.
- Adds the legacy `ObjectWithSubItem<>` marker base type and creates one-to-many relation metadata that targets the child model's `Parent` property column instead of a generated child id column.
- Adds Java `GenerationType` and `EncryptType` metadata for legacy `ColumnAttribute` parity.
- Persists reflective column key-group, generation-type, format, MD5, and RadomDECS metadata through `SW_SYS_PROPERTY`.
- Treats grouped keys as unique-key metadata rather than default model id properties, matching legacy `AutoSysId` selection.
- Converts Java types into `Model` metadata:
  - `@Table` classes become `ModelType.DYNAMIC` models with table/class names.
  - enums become `ModelType.ENUM` models.
  - `@Column` fields map to model properties.
  - `@Id` fields become checked non-null `IdentifyId` properties and the model id property.
  - primitive/wrapper/string/date/time/decimal/UUID types map to existing `PropertyType` values.
  - enum fields point to enum models.
  - collection fields point to element models and become `BusinessObject` collection properties.
  - table-annotated superclasses become `baseModel`.
- Reuses `StaticAppModuleSource` for legacy-style dependency ordering.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `ReflectiveAppModuleSource` did not exist.
- Red for package scanning:
  - Same focused command failed compiling because the package-scanning constructor did not exist.
- Red for referenced package modules:
  - Same focused command failed with `expected:<[org.fool.framework.app.reference.shared, REF_ORDER]> but was:<[REF_ORDER]>`.
- Red for reflective collection relations:
  - Same focused command failed with `NullPointerException` because `ReflectiveOrder` had no generated `relations` for its `lines` collection.
- Red for reflective recursive relations:
  - Same focused command failed with `expected:<1> but was:<0>` because `ReflectiveTreeNode.children` had no generated `Recurve` relation.
- Red for recursive AutoSysId target columns:
  - Same focused command failed with `expected:<...children[_]SYSID> but was:<...children[]SYSID>`.
- Red for bidirectional many-to-many relations:
  - Same focused command failed with `AppManageMigrationTest.reflectiveModuleSourceCreatesLegacyManyToManyRelationForBidirectionalCollections`, `expected:<Many2Many> but was:<One2Many>`, finished at `2026-06-23T15:19:42Z`.
- Red for relation attribute annotations:
  - Same focused command failed compiling because `MultiType` and `ReferToProperty` annotations did not exist, finished at `2026-06-23T15:27:59Z`.
- Red for `ObjectWithSubItem<>` parent target-property parity:
  - Same focused command failed compiling because `org.fool.framework.common.data.ObjectWithSubItem` did not exist, finished at `2026-06-23T15:34:56Z`.
- Red for legacy `ColumnAttribute` metadata parity:
  - Same focused command failed compiling because `EncryptType`, `GenerationType`, and extended `Column` metadata attributes did not exist, finished at `2026-06-23T15:44:15Z`.
- Green:
  - Same focused command passed.
  - `AppManageMigrationTest`: 28 tests, 0 failures, 0 errors.

## Remaining

- Deeper relation attribute parity for one-to-many, many-to-many, recursive parent-property, and reference-property edge cases.
- Deeper column/property metadata parity for multi-column `DBMaps`, `NoMap`, parent overrides, generated expressions, and default values.
- Arbitrary classpath/jar dependency enumeration beyond model-type references.
- Broader annotation parity with legacy display, encryption, generated value, nullability, and multi-map metadata.

## Verification

- Focused TDD command:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -Dtest=AppManageMigrationTest -DfailIfNoTests=false test`
  - Red for recursive relation: `AppManageMigrationTest`: 22 tests, 1 failure, expected one relation but got zero, finished at `2026-06-23T15:08:36Z`.
  - Red for recursive AutoSysId target column: `AppManageMigrationTest`: 23 tests, 1 failure, expected `_SYSID` separator, finished at `2026-06-23T15:13:21Z`.
  - Red for bidirectional many-to-many relation: `AppManageMigrationTest`: 24 tests, 1 failure, expected `Many2Many` but got `One2Many`, finished at `2026-06-23T15:19:42Z`.
  - Red for relation attribute annotations: test compilation failed because `MultiType` and `ReferToProperty` annotations did not exist, finished at `2026-06-23T15:27:59Z`.
  - Red for `ObjectWithSubItem<>` parent target-property parity: test compilation failed because `ObjectWithSubItem` did not exist, finished at `2026-06-23T15:34:56Z`.
  - Red for legacy `ColumnAttribute` metadata parity: test compilation failed because `EncryptType`, `GenerationType`, and extended `Column` metadata attributes did not exist, finished at `2026-06-23T15:44:15Z`.
  - Result: `AppManageMigrationTest`: 28 tests, 0 failures, 0 errors, finished at `2026-06-23T15:44:50Z`.
- Full Maven package against Compose MySQL:
  - `docker run --rm -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3307/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 modules, finished at `2026-06-23T15:52:54Z`.
- Backend compose image build:
  - `docker compose build --pull=false backend`
  - Result: `backend  Built`; Dockerfile Maven build finished at `2026-06-23T15:47:31Z`; image `sha256:d4118df89a71f13e72e9f8e11e61d19ab0c84db7e65fbf8c87412b6a62292712`, created `2026-06-23T15:47:32.383842095Z`.
  - Note: earlier transient attempts failed while resolving Docker Hub base image metadata with `net/http: TLS handshake timeout`; the later retry completed through the standard Dockerfile path.
- Schema regression and fix:
  - First `POST http://localhost:8080/api/v1/data/query-list` after the new backend image returned HTTP 200 with `code=-1`.
  - Backend root cause was `Unknown column 'generation_type' in 'field list'` while loading `fool_sys_model_property`.
  - Added `generation_type` to `docker/mysql/init/005-model.sql` and applied the same idempotent `ALTER TABLE` to the existing Compose MySQL volume.
  - `SHOW COLUMNS FROM fool_sys_model_property LIKE 'generation_type'` confirmed `generation_type int YES NULL`.
- Runtime smoke after `docker compose up -d backend`:
  - `GET http://localhost:8080/test`: HTTP 200 with 2 seeded order rows.
  - `POST http://localhost:8080/api/v1/view/get-view`: HTTP 200, `code=0`, `OrderList`.
  - `POST http://localhost:8080/api/v1/data/query-list`: HTTP 200, `code=0`, 2 data rows.
  - `HEAD http://localhost:8081/`: HTTP 200, `text/html`.
  - `docker compose ps`: backend/frontend/mysql/redis running; MySQL and Redis healthy.
  - `docker compose images backend`: `fool-service-backend:latest`, image id `d4118df89a71`, created 3 minutes before the smoke.
  - Backend logs showed the schema failure at `2026-06-23T15:48:00+0800`, then successful `query-list` requests at `2026-06-23T15:49:08+0800`, `2026-06-23T15:50:53+0800`, and `2026-06-23T15:51:20+0800`. The latest 12 log lines contain only the successful query path and final `code=0` response.
- Repository gates:
  - `python scripts/check_repo_harness.py`: `Repository harness validation passed.`
  - `git diff --check`: passed.
