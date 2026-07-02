USE car_wash;

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
