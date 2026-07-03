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
  `MODEL_DEFAULTOWNER` bigint DEFAULT NULL,
  PRIMARY KEY (`MODEL_ID`),
  KEY `ix_sw_sys_model_class` (`MODEL_CLASS`),
  KEY `ix_sw_sys_model_name_class` (`MODEL_NAME`, `MODEL_CLASS`),
  KEY `ix_sw_sys_model_module` (`MODEL_MODULE`),
  KEY `ix_sw_sys_model_default_owner` (`MODEL_DEFAULTOWNER`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_CON` (
  `SW_SYS_CON_DATASOURCE` varchar(255) NOT NULL,
  `SW_SYS_CON_INITALCATALOG` varchar(255) NOT NULL,
  `SW_SYS_CON_USERNAME` varchar(255) NOT NULL,
  `SW_SYS_CON_PASSWORD` text,
  `SW_SYS_CON_INTEGRATEDSECURITY` tinyint(1) NOT NULL DEFAULT '0',
  `SW_SYS_CON_ISLOACL` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (
    `SW_SYS_CON_DATASOURCE`,
    `SW_SYS_CON_INITALCATALOG`,
    `SW_SYS_CON_USERNAME`
  )
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
  `default_owner` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fool_sys_model_name` (`name`),
  KEY `ix_fool_sys_model_table_name` (`table_name`),
  KEY `ix_fool_sys_model_id_property` (`id_property`),
  KEY `ix_fool_sys_model_default_owner` (`default_owner`)
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
    'ALTER TABLE `SW_SYS_MODEL` ADD COLUMN `MODEL_DEFAULTOWNER` bigint DEFAULT NULL AFTER `MODEL_CON`, ADD KEY `ix_sw_sys_model_default_owner` (`MODEL_DEFAULTOWNER`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_MODEL'
    AND COLUMN_NAME = 'MODEL_DEFAULTOWNER'
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

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_model` ADD COLUMN `default_owner` bigint DEFAULT NULL AFTER `id_property`, ADD KEY `ix_fool_sys_model_default_owner` (`default_owner`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model'
    AND COLUMN_NAME = 'default_owner'
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

CREATE TABLE IF NOT EXISTS `SW_SYS_OPERATION` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_MODEL_OperationsMODEL_ID` bigint DEFAULT NULL,
  `SW_MODEL_OPERATION_NAME` varchar(255) DEFAULT NULL,
  `SW_MODEL_OPERATION_FILTER` text,
  `SW_MODEL_OPERATION_BASETYPE` int DEFAULT NULL,
  `SW_MODEL_OPERATION_ARGMODEL` bigint DEFAULT NULL,
  `SW_MODEL_OPERATION_ARGFILTER` text,
  `SW_MODEL_OPERATION_INVOKEDLL` varchar(500) DEFAULT NULL,
  `SW_MODEL_OPERATION_INVOKECLASS` varchar(500) DEFAULT NULL,
  `SW_MODEL_OPERATION_INVOKEMETHOD` varchar(255) DEFAULT NULL,
  `SW_MODEL_OPERATION_RETURNMODEL` bigint DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_operation_owner` (`SW_SYS_MODEL_OperationsMODEL_ID`),
  KEY `ix_sw_sys_operation_name` (`SW_MODEL_OPERATION_NAME`),
  KEY `ix_sw_sys_operation_arg_model` (`SW_MODEL_OPERATION_ARGMODEL`),
  KEY `ix_sw_sys_operation_return_model` (`SW_MODEL_OPERATION_RETURNMODEL`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_OPERATION_PARAM` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_OPERATION_ParamsSysId` bigint DEFAULT NULL,
  `SW_SYS_OPERATION_PARAM_NAME` varchar(255) DEFAULT NULL,
  `SW_SYS_OPERATION_PARAM_VIEW` bigint DEFAULT NULL,
  `SW_SYS_OPERATION_PARAM_FILTER` text,
  `SW_SYS_OPERATION_PARAM_VALUE` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_operation_param_owner` (`SW_SYS_OPERATION_ParamsSysId`),
  KEY `ix_sw_sys_operation_param_name` (`SW_SYS_OPERATION_PARAM_NAME`),
  KEY `ix_sw_sys_operation_param_view` (`SW_SYS_OPERATION_PARAM_VIEW`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_COMMANDS` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_OPERATION_CommandsSysId` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_TYPE` int DEFAULT NULL,
  `SW_SYS_COMMAND_PROPERTY` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_EXP` text,
  `SW_SYS_COMMAND_ARGMODEL` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_ARGEXP` text,
  `SW_SYS_COMMAND_ARGID` text,
  `SW_SYS_COMMAND_INDEX` int NOT NULL DEFAULT '0',
  `SW_SYS_COMMAND_PROPERTY_EXP` text,
  `SW_SYS_COMMAND_TEMPVALUE` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_commands_owner` (`SW_SYS_OPERATION_CommandsSysId`),
  KEY `ix_sw_sys_commands_type` (`SW_SYS_COMMAND_TYPE`),
  KEY `ix_sw_sys_commands_property` (`SW_SYS_COMMAND_PROPERTY`),
  KEY `ix_sw_sys_commands_arg_model` (`SW_SYS_COMMAND_ARGMODEL`),
  KEY `ix_sw_sys_commands_index` (`SW_SYS_COMMAND_INDEX`)
);

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_OPERATION` ADD COLUMN `SW_SYS_MODEL_OperationsMODEL_ID` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_operation_owner` (`SW_SYS_MODEL_OperationsMODEL_ID`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_OPERATION'
    AND COLUMN_NAME = 'SW_SYS_MODEL_OperationsMODEL_ID'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_OPERATION_PARAM` ADD COLUMN `SW_SYS_OPERATION_ParamsSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_operation_param_owner` (`SW_SYS_OPERATION_ParamsSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_OPERATION_PARAM'
    AND COLUMN_NAME = 'SW_SYS_OPERATION_ParamsSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_COMMANDS` ADD COLUMN `SW_SYS_OPERATION_CommandsSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_commands_owner` (`SW_SYS_OPERATION_CommandsSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_COMMANDS'
    AND COLUMN_NAME = 'SW_SYS_OPERATION_CommandsSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `SW_SYS_MODEL_TRIGGER` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_MODEL_TriggersMODEL_ID` bigint DEFAULT NULL,
  `SW_MODEL_TRIGGER_ARGMODEL` bigint DEFAULT NULL,
  `SW_MODEL_TRIGGER_TYPE` int DEFAULT NULL,
  `SW_MODEL_TRIGGER_FILTER` text,
  `SW_MODEL_TRIGGER_ARGFILTER` text,
  `SW_MODEL_TRIGGER_OPERATIONTYPE` int DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKEDLL` varchar(500) DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKECLASS` varchar(500) DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKEMETHOD` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_model_trigger_owner` (`SW_SYS_MODEL_TriggersMODEL_ID`),
  KEY `ix_sw_sys_model_trigger_arg_model` (`SW_MODEL_TRIGGER_ARGMODEL`),
  KEY `ix_sw_sys_model_trigger_type` (`SW_MODEL_TRIGGER_TYPE`),
  KEY `ix_sw_sys_model_trigger_operation_type` (`SW_MODEL_TRIGGER_OPERATIONTYPE`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_MODEL_TRIGGER_COMMANDS` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_MODEL_TRIGGER_CommandsSysId` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_TYPE` int DEFAULT NULL,
  `SW_SYS_COMMAND_PROPERTY` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_EXP` text,
  `SW_SYS_COMMAND_ARGMODEL` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_ARGEXP` text,
  `SW_SYS_COMMAND_ARGID` text,
  `SW_SYS_COMMAND_Index` int NOT NULL DEFAULT '0',
  `SW_SYS_COMMAND_PROPERTY_EXP` text,
  `SW_SYS_COMMAND_TEMPVALUE` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_model_trigger_commands_owner` (`SW_SYS_MODEL_TRIGGER_CommandsSysId`),
  KEY `ix_sw_sys_model_trigger_commands_type` (`SW_SYS_COMMAND_TYPE`),
  KEY `ix_sw_sys_model_trigger_commands_property` (`SW_SYS_COMMAND_PROPERTY`),
  KEY `ix_sw_sys_model_trigger_commands_arg_model` (`SW_SYS_COMMAND_ARGMODEL`),
  KEY `ix_sw_sys_model_trigger_commands_index` (`SW_SYS_COMMAND_Index`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_PROPERTY_TRIGGER` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_PROPERTY_TriggersSysId` bigint DEFAULT NULL,
  `SW_PROPERTY_TRIGGER_ARGFILTER` text,
  `SW_PROPERTY_TRIGGER_ARGMODEL` bigint DEFAULT NULL,
  `SW_PROPERTY_TRIGGER_FILTER` text,
  `SW_PROPERTY_TRIGGER_TYPE` int DEFAULT NULL,
  `SW_PROPERTY_TRIGGER_NAME` varchar(255) DEFAULT NULL,
  `SW_PROPERTY_TRIGGER_PROPERTY` bigint DEFAULT NULL,
  `SW_PROPERTY_TRIGGER_BASETYPE` int DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKEDLL` varchar(500) DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKECLASS` varchar(500) DEFAULT NULL,
  `SW_MODEL_TRIGGER_INVOKEMETHOD` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_property_trigger_owner` (`SW_SYS_PROPERTY_TriggersSysId`),
  KEY `ix_sw_sys_property_trigger_name` (`SW_PROPERTY_TRIGGER_NAME`),
  KEY `ix_sw_sys_property_trigger_property` (`SW_PROPERTY_TRIGGER_PROPERTY`),
  KEY `ix_sw_sys_property_trigger_arg_model` (`SW_PROPERTY_TRIGGER_ARGMODEL`),
  KEY `ix_sw_sys_property_trigger_type` (`SW_PROPERTY_TRIGGER_TYPE`),
  KEY `ix_sw_sys_property_trigger_base_type` (`SW_PROPERTY_TRIGGER_BASETYPE`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_PROPERTY_TRIGGER_COMMANDS` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_PROPERTY_TRIGGER_CommandsSysId` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_TYPE` int DEFAULT NULL,
  `SW_SYS_COMMAND_PROPERTY` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_EXP` text,
  `SW_SYS_COMMAND_ARGMODEL` bigint DEFAULT NULL,
  `SW_SYS_COMMAND_ARGEXP` text,
  `SW_SYS_COMMAND_ARGID` text,
  `SW_SYS_COMMAND_INDEX` int NOT NULL DEFAULT '0',
  `SW_SYS_COMMAND_PROPERTY_EXP` text,
  `SW_SYS_COMMAND_TEMPVALUE` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_property_trigger_commands_owner` (`SW_SYS_PROPERTY_TRIGGER_CommandsSysId`),
  KEY `ix_sw_sys_property_trigger_commands_type` (`SW_SYS_COMMAND_TYPE`),
  KEY `ix_sw_sys_property_trigger_commands_property` (`SW_SYS_COMMAND_PROPERTY`),
  KEY `ix_sw_sys_property_trigger_commands_arg_model` (`SW_SYS_COMMAND_ARGMODEL`),
  KEY `ix_sw_sys_property_trigger_commands_index` (`SW_SYS_COMMAND_INDEX`)
);

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_MODEL_TRIGGER` ADD COLUMN `SW_SYS_MODEL_TriggersMODEL_ID` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_model_trigger_owner` (`SW_SYS_MODEL_TriggersMODEL_ID`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_MODEL_TRIGGER'
    AND COLUMN_NAME = 'SW_SYS_MODEL_TriggersMODEL_ID'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_MODEL_TRIGGER_COMMANDS` ADD COLUMN `SW_SYS_MODEL_TRIGGER_CommandsSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_model_trigger_commands_owner` (`SW_SYS_MODEL_TRIGGER_CommandsSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_MODEL_TRIGGER_COMMANDS'
    AND COLUMN_NAME = 'SW_SYS_MODEL_TRIGGER_CommandsSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_PROPERTY_TRIGGER` ADD COLUMN `SW_SYS_PROPERTY_TriggersSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_property_trigger_owner` (`SW_SYS_PROPERTY_TriggersSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_PROPERTY_TRIGGER'
    AND COLUMN_NAME = 'SW_SYS_PROPERTY_TriggersSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_PROPERTY_TRIGGER_COMMANDS` ADD COLUMN `SW_SYS_PROPERTY_TRIGGER_CommandsSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_property_trigger_commands_owner` (`SW_SYS_PROPERTY_TRIGGER_CommandsSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_PROPERTY_TRIGGER_COMMANDS'
    AND COLUMN_NAME = 'SW_SYS_PROPERTY_TRIGGER_CommandsSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
  `source` text,
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
    'ALTER TABLE `fool_sys_model_property` ADD COLUMN `source` text AFTER `filter`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_model_property'
    AND COLUMN_NAME = 'source'
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
