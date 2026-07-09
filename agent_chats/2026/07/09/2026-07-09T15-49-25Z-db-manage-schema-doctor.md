# DB Manage Schema Doctor

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep file size and reuse under control.
- Stay focused on View-first/runtime parity instead of adding concrete DTO
  shortcuts.

## Scope

- Compared the runtime schema catalog against Docker init SQL.
- Found DB-management base tables already seeded by
  `docker/mysql/init/003-db-manage.sql` but not guarded by the runtime doctor
  schema catalog.
- Added catalog/test coverage only; no Docker SQL or business runtime behavior
  changed.

## Changed Files

- `scripts/runtime_schema.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-49-25Z-db-manage-schema-doctor.md`

## Validation

- Red: `python scripts/runtime_doctor_test.py RuntimeDoctorTest.test_legacy_core_schema_requires_view_first_and_auth_shell_columns`
  failed because `db_manage_columns` was missing from
  `LEGACY_CORE_SCHEMA_COLUMNS`.
- Green: same focused test passed after adding `DB_App`, `WorkDataBase`,
  `DB_AppDB`, and `DS_DataSourceSet` columns to `scripts/runtime_schema.py`.
- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py`
- `git diff --check`

## Runtime Evidence

- `scripts/runtime_doctor.py` passed with backend/frontend/mysql/redis compose
  services up, MySQL legacy core schema present, and the View/data/auth/report/
  message smoke checks passing through the Vue/backend proxy.

## Risks

- This is a schema drift guard only. It does not claim complete DB-management
  feature parity or add new DB-management handlers.

## Follow-ups

- Continue DB-management behavior only when a real migrated module requires
  it beyond the seeded base schema.
