# Legacy Encrypt Util

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added legacy `EncryptUtil.toMD5` in `fool-common`.
- Preserved the legacy UTF-16LE input bytes and non-padded per-byte hex output.
- Reused the new common helper from `LoginFactory.toMD5` instead of keeping a
  second copy of the algorithm.
- Updated the migration parity document.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/data/ds/EncryptUtil.java`
- `fool-common/src/test/java/org/fool/framework/common/data/ds/EncryptUtilTest.java`
- `fool-auth/src/main/java/org/fool/framework/auth/foolframework/auth/LoginFactory.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-auth -am -Dtest=LegacyLoginFactoryTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`
- `git diff --check`

## Skipped Checks

- Full root `mvn test` was not run; this slice only moves the existing legacy
  MD5 helper into `fool-common` and keeps focused auth coverage.

## Risks And Follow-Ups

- `BasicEnum` remains unmigrated because the legacy constructor writes an
  `enum.txt` side-effect file and needs a separate decision.
