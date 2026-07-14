# Legacy Itemview Data Route

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the Sudoku Item request from FoolFrame's rendered route into
  `src/Web/public/javascripts/app/subitem.js` before changing the migrated API.
- Confirmed the old client posts `/itemview` with `id`, `objid`, and the
  original `idxep` spelling.
- Added `/api/v1/data/itemview` as an alias of the existing
  `querydatadetail` controller method, reusing its request compatibility and
  detail-query service path without a new DTO or business branch.
- Added annotation-level regression coverage and a runtime-doctor request that
  derives real View/object identifiers from the authenticated runtime flow.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
- `scripts/runtime_doctor.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T20-54-44Z-legacy-itemview-route.md`

## Validation

- `python -m py_compile scripts/runtime_doctor.py`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Host `mvn -pl fool-view -am -Dtest=DataControllerLegacyQueryDataDetailTest
  -Dsurefire.failIfNoSpecifiedTests=false test` stopped before tests because
  the local Maven JDK cannot target Java 17: `invalid target release: 17`.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2
  -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am
  -Dtest=DataControllerLegacyQueryDataDetailTest
  -Dsurefire.failIfNoSpecifiedTests=false test` passed the focused 5/5 tests
  and all nine required modules.
- Built implementation commit `25136a0a` from a clean archive and deployed it
  as tagged backend image
  `sha256:06c8105a09f472a27d221c5d17991870d074b5a9c30231bc3c5dc0a00139773d`.
  The running frontend remained the previously verified image
  `sha256:a0ab8a20af2e84b7013de6c50e57346a9b24e0bbcf54f0296914e351f14e4358`.
- `python scripts/runtime_doctor.py` passed all 68 checks. The new
  `data:itemview-legacy-web-route` check authenticated as the Docker admin,
  loaded the View and a row identifier, then posted the old
  `id`/`objid`/`idxep` shape through the frontend proxy.
- Compose services were healthy and `db-migrate` remained `Exited (0)`.
  MySQL held eight orders and four order items before and after verification;
  order 1001 remained `BTC-USDT`, state `0`, customer `3001`, amount
  `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- This restores only the old route surface. It deliberately reuses the loaded
  View-driven detail query rather than introducing an Item-specific DTO.
- Concurrent Agent Session work in Maven files, `fool-agent/`, README,
  `docs/agent-sessions.md`, and its own delivery evidence remains unrelated and
  untouched.
