USE car_wash;

CREATE TABLE IF NOT EXISTS `SW_SYS_MODEL` (
  `MODEL_ID` bigint NOT NULL AUTO_INCREMENT,
  `MODEL_NAME` varchar(255) DEFAULT NULL,
  `MODEL_CLASS` varchar(500) DEFAULT NULL,
  `MODEL_CONTYPE` int DEFAULT NULL,
  `MODEL_DATABASETABLE` varchar(255) DEFAULT NULL,
  `MODEL_MODULE` varchar(255) DEFAULT NULL,
  `MODEL_AUTOID` tinyint(1) NOT NULL DEFAULT '0',
  `MODEL_CON` text,
  PRIMARY KEY (`MODEL_ID`),
  KEY `ix_sw_sys_model_class` (`MODEL_CLASS`),
  KEY `ix_sw_sys_model_name_class` (`MODEL_NAME`, `MODEL_CLASS`),
  KEY `ix_sw_sys_model_module` (`MODEL_MODULE`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_EMUNVALUE` (
  `EMUN_STR` varchar(255) DEFAULT NULL,
  `EMUN_VALUE` int DEFAULT NULL,
  `SW_SYS_MODEL_EnumValuesMODEL_ID` bigint DEFAULT NULL,
  KEY `ix_sw_sys_emunvalue_model` (`SW_SYS_MODEL_EnumValuesMODEL_ID`),
  KEY `ix_sw_sys_emunvalue_value` (`EMUN_VALUE`)
);

CREATE TABLE IF NOT EXISTS `fool_sys_model` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `text` varchar(255) DEFAULT NULL,
  `remark` text,
  `model_type` int DEFAULT NULL,
  `class_name` varchar(500) DEFAULT NULL,
  `table_name` varchar(255) DEFAULT NULL,
  `auto_sys_id` tinyint(1) NOT NULL DEFAULT '0',
  `id_property` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fool_sys_model_name` (`name`),
  KEY `ix_fool_sys_model_table_name` (`table_name`),
  KEY `ix_fool_sys_model_id_property` (`id_property`)
);

CREATE TABLE IF NOT EXISTS `fool_sys_model_enum` (
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `remark` text,
  `owner` bigint DEFAULT NULL,
  KEY `ix_fool_sys_model_enum_owner` (`owner`),
  KEY `ix_fool_sys_model_enum_value` (`value`)
);

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_enum` ADD COLUMN `owner` bigint DEFAULT NULL AFTER `remark`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_enum'
    AND COLUMN_NAME = 'owner'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model` ADD COLUMN `auto_sys_id` tinyint(1) NOT NULL DEFAULT ''0'' AFTER `table_name`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model'
    AND COLUMN_NAME = 'auto_sys_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `SW_SYS_RELATION` (
  `SW_SYS_RELATION_TYPE` int DEFAULT NULL,
  `SW_SYS_RELATION_SOURCEPROPERTY` bigint DEFAULT NULL,
  `SW_SYS_RELATION_TARGETPROPERTY` bigint DEFAULT NULL,
  `SW_SYS_RELATION_TABLE` varchar(255) DEFAULT NULL,
  `SW_SYS_RELATION_SOURCECOL` varchar(255) DEFAULT NULL,
  `SW_SYS_RELATION_TARGETCOL` varchar(255) DEFAULT NULL,
  `SW_SYS_RELATION_CANBENULL` tinyint(1) DEFAULT '0',
  KEY `ix_sw_sys_relation_table` (`SW_SYS_RELATION_TABLE`),
  KEY `ix_sw_sys_relation_source_property` (`SW_SYS_RELATION_SOURCEPROPERTY`),
  KEY `ix_sw_sys_relation_target_property` (`SW_SYS_RELATION_TARGETPROPERTY`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_PROPERTY` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `PROPERTY_TYPE` int DEFAULT NULL,
  `PROPERTY_CONTYPE` int DEFAULT NULL,
  `PROPERTY_NAME` varchar(255) DEFAULT NULL,
  `PROPERTY_MODEL` bigint DEFAULT NULL,
  `PROPERTY_ISARRAY` tinyint(1) NOT NULL DEFAULT '0',
  `PROPERTY_COLNAME` varchar(255) DEFAULT NULL,
  `PROPERTY_PROPERTYNAME` varchar(255) DEFAULT NULL,
  `PROPERTY_MULTIMAP` tinyint(1) NOT NULL DEFAULT '0',
  `PROPERTY_IXGRPOUP` varchar(255) DEFAULT NULL,
  `PROPERTY_ISCHECK` tinyint(1) NOT NULL DEFAULT '0',
  `PROPERTY_GENERATIONTYPE` int DEFAULT NULL,
  `PROPERTY_ALLOWDBNULL` tinyint(1) NOT NULL DEFAULT '1',
  `PROPERTY_CANGET` tinyint(1) NOT NULL DEFAULT '1',
  `PROPERTY_CANSET` tinyint(1) NOT NULL DEFAULT '1',
  `PROPERTY_FILTER` text,
  `PROPERTY_SOURCE` text,
  `PROPERTY_FORMAT` text,
  `PROPERTY_SQLCON` text,
  `SW_SYS_MODEL_PropertiesSysId` bigint DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_property_owner` (`SW_SYS_MODEL_PropertiesSysId`),
  KEY `ix_sw_sys_property_property_model` (`PROPERTY_MODEL`),
  KEY `ix_sw_sys_property_name` (`PROPERTY_PROPERTYNAME`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_MULTIMAP` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `MAP_NAME` varchar(255) DEFAULT NULL,
  `MAP_COLNAME` varchar(255) DEFAULT NULL,
  `SW_SYS_PROPERTY_DBMapsSysId` bigint DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_multimap_owner` (`SW_SYS_PROPERTY_DBMapsSysId`),
  KEY `ix_sw_sys_multimap_name` (`MAP_NAME`)
);

CREATE TABLE IF NOT EXISTS `fool_sys_model_property` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `remark` text,
  `property_model` bigint DEFAULT NULL,
  `is_collection` tinyint(1) DEFAULT NULL,
  `owner` bigint DEFAULT NULL,
  `filter` text,
  `format` text,
  `column` varchar(255) DEFAULT NULL,
  `property_type` int DEFAULT NULL,
  `allow_db_null` tinyint(1) DEFAULT '1',
  `is_check` tinyint(1) DEFAULT '0',
  `ix_group` varchar(255) DEFAULT NULL,
  `generation_type` int DEFAULT NULL,
  `generation_expression` varchar(255) DEFAULT NULL,
  `default_value` varchar(255) DEFAULT NULL,
  `multi_map` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ix_fool_sys_model_property_owner` (`owner`),
  KEY `ix_fool_sys_model_property_property_model` (`property_model`),
  KEY `ix_fool_sys_model_property_column` (`column`)
);

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `property_type` int DEFAULT NULL AFTER `column`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'property_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `allow_db_null` tinyint(1) DEFAULT ''1'' AFTER `property_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'allow_db_null'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `is_check` tinyint(1) DEFAULT ''0'' AFTER `allow_db_null`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'is_check'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `ix_group` varchar(255) DEFAULT NULL AFTER `is_check`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'ix_group'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `generation_type` int DEFAULT NULL AFTER `ix_group`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'generation_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `generation_expression` varchar(255) DEFAULT NULL AFTER `generation_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'generation_expression'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `default_value` varchar(255) DEFAULT NULL AFTER `generation_expression`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'default_value'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `multi_map` tinyint(1) DEFAULT ''0'' AFTER `default_value`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'multi_map'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
