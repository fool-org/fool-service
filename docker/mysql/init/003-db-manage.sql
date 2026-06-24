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
