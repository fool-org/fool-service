# 2026-07-03T04:23:20Z Legacy Auth Menu Seed

## Prompt

- Continue the active goal: Docker runtime, FoolFrame migration parity, Vue
  frontend, and timely atomic commits.

## Scope

- Seed legacy admin auth/menu rows for the Docker `OrderList` smoke workflow.
- Patch the running Docker MySQL volume to match the edited init SQL.
- Verify the modern auth menu API still returns the Vue-visible `OrderList`
  menu.

## Changed Files

- `docker/mysql/init/007-auth.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-23-20Z-legacy-auth-menu-seed.md`

## Validation

- RED: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT COUNT(*) AS legacy_menu_count FROM SW_APP_AUTH_MENU; SELECT COUNT(*) AS legacy_menu_subitem_count FROM SW_APP_AUTH_MENU_SubItems; SELECT COUNT(*) AS legacy_role_count FROM SW_APP_AUTH_ROLE; SELECT COUNT(*) AS legacy_role_menu_count FROM SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE; SELECT COUNT(*) AS legacy_user_count FROM SW_APP_AUTH_USER; SELECT COUNT(*) AS legacy_role_user_count FROM SW_APP_AUTH_ROLE_SW_APP_AUTH_USER;"`
  - All legacy auth/menu counts were `0`.
- GREEN: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SELECT USER_UID,USER_LOGINNAME,USER_SHOWNAME,USER_DEFAULTVIEW FROM SW_AUTH_USER WHERE USER_UID = 1; SELECT APP_AUTH_ID,APP_AUTH_USERID,APP_AUTH_USERLOGINNAME FROM SW_APP_AUTH_USER WHERE APP_AUTH_ID = 1; SELECT AUTH_ROLE_ID,AUTH_ROLE_NAME FROM SW_APP_AUTH_ROLE WHERE AUTH_ROLE_ID = 1; SELECT AUTH_MENU_ID,AUTH_MENU_TEXT,AUTH_MENU_VIEWID,AUTH_MENU_VISIABLE,AUTH_MENU_ENABLE,AUTH_MENU_INDEX FROM SW_APP_AUTH_MENU ORDER BY AUTH_MENU_ID; SELECT SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID AS parent,SW_APP_AUTH_MENU_SUBITEMS_ITEM AS child FROM SW_APP_AUTH_MENU_SubItems ORDER BY parent,child; SELECT SW_APP_AUTH_ROLE_ID,SW_APP_AUTH_USER_ID FROM SW_APP_AUTH_ROLE_SW_APP_AUTH_USER; SELECT SW_APP_AUTH_MENU_ID,SW_APP_AUTH_ROLE_ID FROM SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE ORDER BY SW_APP_AUTH_MENU_ID;"`
  - Returned admin user, authorized user, Admin role, Views/OrderList menus,
    submenu relation `1 -> 2`, role-user `1 -> 1`, and role-menu rows for both
    menus.
- `POST /api/v1/auth/login` followed by `POST /api/v1/auth/auth-menus`
  - Returned modern menu tree with `Views` and `OrderList`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Skipped

- Did not seed legacy company/department tables; the Docker smoke auth flow only
  needs the admin user, role, menu, and relations.

## Risks

- Existing Docker volume was patched manually; fresh volumes get the same rows
  from `007-auth.sql`.
