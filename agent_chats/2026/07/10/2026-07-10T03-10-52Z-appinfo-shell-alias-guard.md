# AppInfo Shell Alias Guard

## Prompt
- Continue the Docker/FoolFrame/Vue migration with atomic commits and maximum
  reuse.

## Scope
- Matched old FoolFrame shell usage in `layout.jade` and `default.jade`, which
  read AppInfo fields such as `App.AppVer` and `App.AppPowerBy`.
- Tightened the Docker runtime doctor so `getapp` and `getmain` must expose the
  existing legacy AppInfo aliases through the Vue proxy:
  `AppName`, `AppVer`, `AppNote`, `AppPowerBy`, `AppPowerUrl`, `AppLogoUrl`,
  `DefaultViewId`, and `AppId`.
- Reused the existing `AuthService.LegacyAppInfo` DTO; no production code was
  changed.
- Updated migration parity and task-state docs.

## Changed Files
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T03-10-52Z-appinfo-shell-alias-guard.md`

## Validation
- `python scripts/runtime_doctor_test.py` passed: 46 tests.
- `python scripts/runtime_doctor.py` passed against Docker; `auth:getapp` and
  `auth:getmain` now report legacy AppInfo alias coverage.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- Direct Vue-proxy checks before the guard showed `getapp` and `getmain`
  already returned:
  `AppName`, `AppVer`, `AppNote`, `AppPowerBy`, `AppPowerUrl`, `AppLogoUrl`,
  `DefaultViewId`, and `AppId`.

## Risks
- This is a guard-only change. It can fail the runtime doctor if future Docker
  seeds or DTO serialization drop old shell fields.
