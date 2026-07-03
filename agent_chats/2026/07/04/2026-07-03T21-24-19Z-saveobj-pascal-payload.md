# SaveObj Pascal Payload Compatibility

## Prompt

- Continue the migration toward a Docker-backed Vue frontend.
- Keep the flow View-first: render the View page, query/save data through that
  View context, and avoid binding migrated endpoints to concrete business DTOs.
- Watch source size and reuse existing DTO/protocol boundaries.

## Scope

- Audited the legacy Web save path in `../FoolFrame/src/Web/Cloud-Social/soway.js`
  and `../FoolFrame/src/Web/public/javascripts/app/detailview.js`.
- Added Jackson aliases to the existing generic `SaveObjRequest` DTO so
  `/saveobj` accepts FoolFrame's Pascal/mixed payload shape.
- Added focused DTO coverage for the legacy top-level `SaveObj` object and
  nested `Propertyies`, `Itemproperties`, `Items`, `AddedItems`, `DelteItems`,
  `IsExist`, `ItemId`, `Key`, and `Value` fields.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/SaveObjRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/dto/SaveObjRequestTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T21-24-19Z-saveobj-pascal-payload.md`

## Validation

- Passed:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false test`
- Passed:
  `python3 scripts/check_repo_harness.py`
- Passed:
  `git diff --check`
- Passed:
  `docker compose up -d --build backend`
- Passed:
  `python3 scripts/runtime_doctor.py`

## Runtime Evidence

- The legacy source sends `postandget('saveobj', { Token: token, SaveObj: obj })`.
- The legacy detail page builds the saved object with generic View fields such
  as `Propertyies`, `Itemproperties`, `Items`, `AddedItems`, `DelteItems`, and
  `IsExist`.
- Docker backend was rebuilt and restarted from the edited backend source;
  `runtime_doctor.py` passed backend `/test`, frontend-proxied View metadata,
  `querydata(ViewId)`, `inputquery(ViewId)`, and report metadata smoke checks.

## Skipped Checks And Risks

- No live `/saveobj` write smoke was run for this slice to avoid mutating the
  Docker seed data; the change is isolated to request deserialization and is
  covered by focused DTO tests plus the full `fool-view` module test run.
