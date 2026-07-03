# Row Items Before Values Fallbacks

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep the rendered page driven
by View metadata before querying data.

## Scope

- Remove remaining frontend row identity/save spots where `row.values` could
  beat legacy row `Items` metadata.
- Keep `row.values` as a compatibility fallback only.

## Changes

- `rowObjectId` now prefers `row.id`, then the first legacy row item
  `objId`/`fmtValue`, then `row.values`.
- `buildDraftsFromRow` now prefers matched or same-index row `Items` before
  falling back to `values`.
- The API-tools result table now keys rows through `rowObjectId(row,
  resultColumns)` instead of serializing `row.values`.
- Added a focused Vue helper test proving `Items` wins over mismatched
  `values`.

## Validation

- Passed: `cd frontend && npm test && npm run build`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- Rebuilt and restarted the Compose frontend:
  `docker compose up -d --no-deps --build frontend`.
- Passed: `python3 scripts/runtime_doctor.py`.

## Risks

- `row.values` remains as a fallback for older/generic responses that do not
  include legacy `Items`.

## Follow-ups

- Continue removing `values` fallbacks only when the route being migrated is
  proven to return legacy `Items`.
