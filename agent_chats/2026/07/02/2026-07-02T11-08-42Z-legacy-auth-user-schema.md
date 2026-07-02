# Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

# Scope

- Narrow Docker database parity slice for legacy `SWUA01-SOWAY.ORM.AUTH.User`.
- Keep the modern Vue auth API and backend code unchanged.

# Changes

- Added Docker MySQL schema for legacy `SW_AUTH_USER`, matching the FoolFrame
  `USER_` column prefix and key groups from `../FoolFrame/src/Server/SWUA01-SOWAY.ORM.AUTH/User.cs`.
- Corrected the Docker-seeded modern `auth_user.admin` password hash to match
  `AuthService.passwordHash(id, password)`.
- Updated `docs/migration/foolframe-parity.md` to mark `SW_AUTH_USER` as covered.

# Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/007-auth.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT id,password,LOWER(MD5(CONCAT(id,'admin'))) expected FROM auth_user WHERE id='admin'; SHOW COLUMNS FROM SW_AUTH_USER;"`
  - Confirmed `SW_AUTH_USER.USER_UID`, `USER_UUID`, `USER_LOGINNAME`,
    `USER_PHONE`, `USER_MAIL`, `USER_FIRSTNAME`, `USER_LASTNAME`,
    `USER_SHOWNAME`, `USER_TITLE`, `USER_AVTAR`, `USER_PWD`,
    `USER_REGTIME`, `USER_LASTLOGINTIME`, `USER_LASTMODIFYTIME`,
    `USER_SEX`, and `USER_DEFAULTVIEW`.
  - Confirmed admin password equals `LOWER(MD5(CONCAT(id,'admin')))`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"userId":"admin","password":"admin"}' http://localhost:8080/api/v1/auth/login`
  - Returned `{"code":0,"message":"success",...}`.

# Runtime Evidence

- `docker compose ps`
  - backend and frontend are up.
  - MySQL and Redis are healthy.

# Risks

- Full Maven reactor and frontend build were not rerun because this change only
  touched Docker SQL and migration docs.

# Follow-ups

- Continue closing the remaining `car_wash` schema gaps listed in
  `docs/migration/foolframe-parity.md`.
