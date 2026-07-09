# Legacy Web saverpt Payload

## Prompt

Continue the FoolFrame migration with Docker, Vue, atomic commits, maximum
reuse, and View-first data flow.

## Scope

- Compared `../FoolFrame/src/Web/routes/index.js`,
  `../FoolFrame/src/Web/Cloud-Social/soway.js`, and
  `../FoolFrame/src/Web/views/view.jade`.
- Reused the existing `/api/v1/report/saverpt` endpoint and
  `MakeReportRequest` aliases.
- Added Docker runtime-doctor coverage for the old Web report-definition
  payload: `viewid`, `cols`, `exp`, and `reportname`.

## Changed Files

- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `python scripts/runtime_doctor_test.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `python scripts/runtime_doctor.py`

## Skipped Checks

- Full root `mvn test` was not rerun because this slice only adds runtime
  coverage for existing report DTO alias behavior.
- `docker compose up -d --build` was not rerun because no backend runtime code
  changed; the current Docker stack was verified by `runtime_doctor.py`.

## Risks

- No business implementation was changed; this slice guards existing shared
  report DTO alias behavior and the legacy no-op `saverpt` surface.
