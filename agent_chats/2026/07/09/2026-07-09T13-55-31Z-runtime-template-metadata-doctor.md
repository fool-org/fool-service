# Runtime Template Metadata Doctor

# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, maximize
reuse, follow the View-first render path before data binding, and avoid binding
Vue pages to concrete business DTOs.

# Scope

- Docker seed metadata for legacy View template filenames.
- Runtime doctor coverage for `getlistview.TempFile` and item `ViewFile`.
- Task-state and migration evidence for this runtime proof slice.

# Changes

- Seeded `SW_SYS_VIEW_FILE` rows for a runtime View template and list item
  include, then linked `OrderList` and the `Order ID` View item to those rows.
- Added `view_template_metadata_ok` to the runtime doctor and required it in
  the `view:getlistview` check.
- Covered the helper and `api_checks` flow in `scripts/runtime_doctor_test.py`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- Red:
  `python scripts/runtime_doctor_test.py RuntimeDoctorTest.test_view_template_metadata_requires_legacy_render_fields`
  failed with `ImportError: cannot import name 'view_template_metadata_ok'`.
- Green:
  `python scripts/runtime_doctor_test.py RuntimeDoctorTest.test_view_template_metadata_requires_legacy_render_fields`
  passed.
- Green:
  `python scripts/runtime_doctor_test.py`
  passed `Ran 32 tests`.
- Red runtime before backend restart:
  `python scripts/runtime_doctor.py --skip-compose`
  failed only `view:getlistview`, proving the doctor catches missing template
  metadata.
- Green runtime after `docker compose up -d --force-recreate backend`:
  `python scripts/runtime_doctor.py --skip-compose`
  passed all checks.

# Runtime Evidence

- `POST http://localhost:8081/api/v1/view/getlistview` with `{"ViewId":100}`
  returned `TempFile=viewWithChart`.
- The same response returned item `ID=1001` with
  `ViewFile=./includes/List`.
- `docker compose ps backend` showed the backend recreated and running from the
  current image.

# Risks

- Vue still uses the generic metadata renderer for these template names. Full
  custom template behavior should continue from concrete FoolFrame view files
  before adding frontend-specific branches.

# Follow-ups

- Continue comparing `../FoolFrame/src/Web/views/*` render templates, then
  map each concrete template to reusable Vue render strategy rather than
  binding pages directly to data DTOs.
