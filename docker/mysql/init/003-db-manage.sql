USE car_wash;

CREATE TABLE IF NOT EXISTS `DB_App` (
  `BO_Id` bigint NOT NULL AUTO_INCREMENT,
  `BO_AppName` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`BO_Id`),
  UNIQUE KEY `uk_db_app_name` (`BO_AppName`)
);

CREATE TABLE IF NOT EXISTS `WorkDataBase` (
  `DBID` bigint NOT NULL AUTO_INCREMENT,
  `DBName` varchar(255) NOT NULL DEFAULT '',
  `DBYear` varchar(32) NOT NULL DEFAULT '',
  `DBSysName` varchar(255) NOT NULL DEFAULT '',
  `IsActive` tinyint(1) NOT NULL DEFAULT '0',
  `DBNo` varchar(32) NOT NULL,
  `pwd1` varbinary(8) DEFAULT NULL,
  `pwd2` varbinary(8) DEFAULT NULL,
  `pwd3` varbinary(8) DEFAULT NULL,
  `pwd4` varbinary(8) DEFAULT NULL,
  `pwd5` varchar(512) DEFAULT NULL,
  `UserName` varchar(255) DEFAULT NULL,
  `CompanyName` varchar(255) DEFAULT NULL,
  `ServerIp` varchar(255) DEFAULT NULL,
  `IsLocal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`DBID`),
  UNIQUE KEY `uk_work_database_no` (`DBNo`),
  UNIQUE KEY `uk_work_database_name_year` (`DBName`, `DBYear`)
);

CREATE TABLE IF NOT EXISTS `DB_AppDB` (
  `App_Id` bigint NOT NULL,
  `DBNo` varchar(32) NOT NULL,
  PRIMARY KEY (`App_Id`, `DBNo`),
  KEY `ix_db_appdb_dbno` (`DBNo`)
);

CREATE TABLE IF NOT EXISTS `DS_DataSourceSet` (
  `DS_Key` varchar(255) NOT NULL,
  `DS_DBNo` varchar(32) NOT NULL,
  PRIMARY KEY (`DS_Key`),
  KEY `ix_datasource_dbno` (`DS_DBNo`)
);

INSERT INTO `DB_App` (`BO_AppName`)
VALUES ('fool-service')
ON DUPLICATE KEY UPDATE
  `BO_AppName` = VALUES(`BO_AppName`);

INSERT INTO `WorkDataBase` (
  `DBName`, `DBYear`, `DBSysName`, `IsActive`, `DBNo`,
  `pwd1`, `pwd2`, `pwd3`, `pwd4`, `pwd5`,
  `UserName`, `CompanyName`, `ServerIp`, `IsLocal`
)
VALUES (
  'car_wash', '2026', 'car_wash', 1, '01',
  0x6F6475F2B354CF65, 0x0504020103000607, 0xAB026FFFE5372C0D, 0x0301050700060204, '6OzKHTvsbujwi/x8c33cAw==',
  'root', 'Docker', 'mysql:3306', 1
)
ON DUPLICATE KEY UPDATE
  `DBName` = VALUES(`DBName`),
  `DBYear` = VALUES(`DBYear`),
  `DBSysName` = VALUES(`DBSysName`),
  `IsActive` = VALUES(`IsActive`),
  `pwd1` = VALUES(`pwd1`),
  `pwd2` = VALUES(`pwd2`),
  `pwd3` = VALUES(`pwd3`),
  `pwd4` = VALUES(`pwd4`),
  `pwd5` = VALUES(`pwd5`),
  `UserName` = VALUES(`UserName`),
  `CompanyName` = VALUES(`CompanyName`),
  `ServerIp` = VALUES(`ServerIp`),
  `IsLocal` = VALUES(`IsLocal`);

INSERT IGNORE INTO `DB_AppDB` (`App_Id`, `DBNo`)
SELECT `BO_Id`, '01'
  FROM `DB_App`
 WHERE `BO_AppName` = 'fool-service';

INSERT INTO `DS_DataSourceSet` (`DS_Key`, `DS_DBNo`)
VALUES ('car_wash', '01')
ON DUPLICATE KEY UPDATE
  `DS_DBNo` = VALUES(`DS_DBNo`);
