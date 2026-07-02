USE car_wash;

CREATE TABLE IF NOT EXISTS `SW_AUTH_USER` (
  `USER_UID` bigint NOT NULL AUTO_INCREMENT,
  `USER_UUID` varchar(36) DEFAULT NULL,
  `USER_LOGINNAME` varchar(255) DEFAULT NULL,
  `USER_PHONE` varchar(64) DEFAULT NULL,
  `USER_MAIL` varchar(255) DEFAULT NULL,
  `USER_FIRSTNAME` varchar(255) DEFAULT NULL,
  `USER_LASTNAME` varchar(255) DEFAULT NULL,
  `USER_SHOWNAME` varchar(255) DEFAULT NULL,
  `USER_TITLE` varchar(255) DEFAULT NULL,
  `USER_AVTAR` varchar(500) DEFAULT NULL,
  `USER_PWD` varchar(255) DEFAULT NULL,
  `USER_REGTIME` datetime DEFAULT NULL,
  `USER_LASTLOGINTIME` datetime DEFAULT NULL,
  `USER_LASTMODIFYTIME` datetime DEFAULT NULL,
  `USER_SEX` int DEFAULT NULL,
  `USER_DEFAULTVIEW` bigint DEFAULT NULL,
  PRIMARY KEY (`USER_UID`),
  UNIQUE KEY `uk_sw_auth_user_uuid` (`USER_UUID`),
  UNIQUE KEY `uk_sw_auth_user_loginname` (`USER_LOGINNAME`),
  UNIQUE KEY `uk_sw_auth_user_phone` (`USER_PHONE`),
  UNIQUE KEY `uk_sw_auth_user_mail` (`USER_MAIL`)
);

CREATE TABLE IF NOT EXISTS `auth_user` (
  `id` varchar(64) NOT NULL,
  `mobile` varchar(64) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `auth_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `auth_item` (
  `id` varchar(64) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `auth_type` int NOT NULL DEFAULT '0',
  `auth_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `auth_user_role` (
  `user_id` varchar(64) NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`),
  KEY `ix_auth_user_role_role` (`role_id`)
);

CREATE TABLE IF NOT EXISTS `auth_role_auth` (
  `role_id` bigint NOT NULL,
  `auth_id` varchar(64) NOT NULL,
  PRIMARY KEY (`role_id`, `auth_id`),
  KEY `ix_auth_role_auth_auth` (`auth_id`)
);

INSERT INTO `auth_user` (`id`, `mobile`, `name`, `password`)
VALUES ('admin', '', 'Admin', LOWER(MD5(CONCAT('admin', 'admin'))))
ON DUPLICATE KEY UPDATE
  `mobile` = VALUES(`mobile`),
  `name` = VALUES(`name`),
  `password` = VALUES(`password`);

INSERT INTO `auth_role` (`id`, `name`)
VALUES (1, 'Admin')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`);

INSERT INTO `auth_item` (`id`, `name`, `auth_type`, `auth_name`)
VALUES
  ('01', 'Views', 2, NULL),
  ('0101', 'OrderList', 1, 'OrderList')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `auth_type` = VALUES(`auth_type`),
  `auth_name` = VALUES(`auth_name`);

INSERT IGNORE INTO `auth_user_role` (`user_id`, `role_id`)
VALUES ('admin', 1);

INSERT IGNORE INTO `auth_role_auth` (`role_id`, `auth_id`)
VALUES
  (1, '01'),
  (1, '0101');
