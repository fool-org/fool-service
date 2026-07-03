USE car_wash;

CREATE TABLE IF NOT EXISTS `fool_sys_view` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `view_name` varchar(255) NOT NULL,
  `view_text` varchar(255) DEFAULT NULL,
  `view_remark` text,
  `view_title` varchar(255) DEFAULT NULL,
  `view_type` int DEFAULT NULL,
  `view_model` varchar(255) DEFAULT NULL,
  `filter` text,
  `auto_fresh_interval` int NOT NULL DEFAULT 0,
  `view_model_class` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fool_sys_view_name` (`view_name`),
  KEY `ix_fool_sys_view_model` (`view_model`)
);

SET @add_view_auto_fresh_interval = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_view` ADD COLUMN `auto_fresh_interval` int NOT NULL DEFAULT 0 AFTER `filter`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_view'
    AND COLUMN_NAME = 'auto_fresh_interval'
);
PREPARE add_view_auto_fresh_interval_stmt FROM @add_view_auto_fresh_interval;
EXECUTE add_view_auto_fresh_interval_stmt;
DEALLOCATE PREPARE add_view_auto_fresh_interval_stmt;

CREATE TABLE IF NOT EXISTS `fool_sys_view_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_name` varchar(255) DEFAULT NULL,
  `item_label` varchar(255) DEFAULT NULL,
  `item_legend` varchar(255) DEFAULT NULL,
  `model_property` varchar(255) DEFAULT NULL,
  `input_type` int DEFAULT NULL,
  `can_edit` tinyint(1) NOT NULL DEFAULT '0',
  `select_view_name` varchar(255) DEFAULT NULL,
  `input_regx` text,
  `format_regx` text,
  `edit_type` int DEFAULT 0,
  `show_index` int NOT NULL DEFAULT 0,
  `width` int NOT NULL DEFAULT 0,
  `source_expression` text,
  `view_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_fool_sys_view_item_view_id` (`view_id`),
  KEY `ix_fool_sys_view_item_property` (`model_property`)
);

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `fool_sys_view_item` ADD COLUMN `width` int NOT NULL DEFAULT 0 AFTER `show_index`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_view_item'
    AND COLUMN_NAME = 'width'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_view_item_source_expression = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `fool_sys_view_item` ADD COLUMN `source_expression` text AFTER `width`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_view_item'
    AND COLUMN_NAME = 'source_expression'
);
PREPARE add_view_item_source_expression_stmt FROM @add_view_item_source_expression;
EXECUTE add_view_item_source_expression_stmt;
DEALLOCATE PREPARE add_view_item_source_expression_stmt;

CREATE TABLE IF NOT EXISTS `SW_SYS_VIEW` (
  `VIEW_ID` bigint NOT NULL AUTO_INCREMENT,
  `VIEW_MODEL` bigint DEFAULT NULL,
  `VIEW_NAME` varchar(255) DEFAULT NULL,
  `VIEW_FILTER` text,
  `VIEW_DEFAULT` bigint DEFAULT NULL,
  `VIEW_TYPE` int DEFAULT NULL,
  `VIEW_CONTYPE` int NOT NULL DEFAULT '2',
  `VIEW_FILE` bigint DEFAULT NULL,
  `VIEW_CHECKAUTH` tinyint(1) NOT NULL DEFAULT '0',
  `VIEW_AUTOFRESHINTERVAL` int NOT NULL DEFAULT '0',
  `VIEW_CANEDIT` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`VIEW_ID`),
  KEY `ix_sw_sys_view_name` (`VIEW_NAME`),
  KEY `ix_sw_sys_view_model` (`VIEW_MODEL`),
  KEY `ix_sw_sys_view_default` (`VIEW_DEFAULT`),
  KEY `ix_sw_sys_view_file` (`VIEW_FILE`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_VIEW_FILE` (
  `VIEW_FILE_ID` bigint NOT NULL AUTO_INCREMENT,
  `VIEW_FILE_NAME` varchar(255) DEFAULT NULL,
  `VIEW_FILE_VIEWTYPE` int DEFAULT NULL,
  `VIEW_FILE_FILENAME` varchar(500) DEFAULT NULL,
  `VIEW_FILE_FILECONTENT` longtext,
  PRIMARY KEY (`VIEW_FILE_ID`),
  UNIQUE KEY `uk_sw_sys_view_file_name` (`VIEW_FILE_NAME`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_VIEW_ITEM` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_VIEW_ItemsVIEW_ID` bigint DEFAULT NULL,
  `VIEW_ITEM_NAME` varchar(255) DEFAULT NULL,
  `VIEW_ITEM_NOTE` text,
  `VIEW_ITEM_FORMAT` varchar(255) DEFAULT NULL,
  `VIEW_ITEM_PROPERTY` bigint DEFAULT NULL,
  `VIEW_ITEM_PROPERTY_SHOW` bigint DEFAULT NULL,
  `VIEW_ITEM_PROPERTY_VALUE` bigint DEFAULT NULL,
  `VIEW_ITEM_READONLY` tinyint(1) NOT NULL DEFAULT '0',
  `VIEW_ITEM_INDEX` int NOT NULL DEFAULT '0',
  `VIEW_ITEM_SUBVIEW` bigint DEFAULT NULL,
  `VIEW_ITEM_EDITVIEW` bigint DEFAULT NULL,
  `VIEW_ITEM_SELECTVIEW` bigint DEFAULT NULL,
  `VIEW_ITEM_WIDTH` int NOT NULL DEFAULT '0',
  `VIEW_ITEM_ISSHOW` tinyint(1) NOT NULL DEFAULT '0',
  `VIEW_ITEM_FILE` bigint DEFAULT NULL,
  `VIEW_ITEM_EDITTYPE` int DEFAULT NULL,
  `VIEW_ITEM_SOURCEEXP` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_view_item_owner` (`SW_SYS_VIEW_ItemsVIEW_ID`),
  KEY `ix_sw_sys_view_item_property` (`VIEW_ITEM_PROPERTY`),
  KEY `ix_sw_sys_view_item_index` (`VIEW_ITEM_INDEX`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_VIEW_OPERATION` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_VIEW_OperationsVIEW_ID` bigint DEFAULT NULL,
  `SW_VIEW_OPERATION_NAME` varchar(255) DEFAULT NULL,
  `SW_VIEW_OPERATION_MODELOPERATION` bigint DEFAULT NULL,
  `SW_VIEW_OPERATION_RESULTVIEW` bigint DEFAULT NULL,
  `SW_VIEW_OPERATION_SHOWPROCESS` tinyint(1) NOT NULL DEFAULT '0',
  `SW_VIEW_OPERATION_INDEX` int NOT NULL DEFAULT '0',
  `SW_VIEW_OPERATION_REQUIRESELECTB` tinyint(1) NOT NULL DEFAULT '0',
  `SW_VIEW_OPERATION_IMAGE` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_view_operation_owner` (`SW_SYS_VIEW_OperationsVIEW_ID`),
  KEY `ix_sw_sys_view_operation_model_operation` (`SW_VIEW_OPERATION_MODELOPERATION`),
  KEY `ix_sw_sys_view_operation_result_view` (`SW_VIEW_OPERATION_RESULTVIEW`),
  KEY `ix_sw_sys_view_operation_index` (`SW_VIEW_OPERATION_INDEX`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_OPERATIONVIEW` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_OPVIEW_NAME` varchar(255) DEFAULT NULL,
  `SW_SYS_OPVIEW_RESULT` bigint DEFAULT NULL,
  `SW_SYS_OPVIEW_OPREATION` bigint DEFAULT NULL,
  `SW_SYS_OPVIEW_SUCCESMSG` text,
  `SW_SYS_OPVIEW_ERRORMSG` text,
  `SW_SYS_OPVIEW_MSG` text,
  `SW_SYS_OPVIEW_SHOW` tinyint(1) NOT NULL DEFAULT '0',
  `SW_SYS_OPVIEW_ConfirmMSG` text,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_operationview_result` (`SW_SYS_OPVIEW_RESULT`),
  KEY `ix_sw_sys_operationview_operation` (`SW_SYS_OPVIEW_OPREATION`)
);

CREATE TABLE IF NOT EXISTS `SW_SYS_OPERATIONVIEW_ITEM` (
  `SysId` bigint NOT NULL AUTO_INCREMENT,
  `SW_SYS_OPERATIONVIEW_ParamsSysId` bigint DEFAULT NULL,
  `SW_SYS_OPVIEWITEM_NAME` varchar(255) DEFAULT NULL,
  `SW_SYS_OPVIEWITEM_INDEX` int NOT NULL DEFAULT '0',
  `SW_SYS_OPVIEWITEM_PARAM` bigint DEFAULT NULL,
  PRIMARY KEY (`SysId`),
  KEY `ix_sw_sys_operationview_item_owner` (`SW_SYS_OPERATIONVIEW_ParamsSysId`),
  KEY `ix_sw_sys_operationview_item_index` (`SW_SYS_OPVIEWITEM_INDEX`),
  KEY `ix_sw_sys_operationview_item_param` (`SW_SYS_OPVIEWITEM_PARAM`)
);

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_MODEL` bigint DEFAULT NULL AFTER `VIEW_ID`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_MODEL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_FILTER` text AFTER `VIEW_NAME`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_FILTER'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_DEFAULT` bigint DEFAULT NULL AFTER `VIEW_FILTER`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_DEFAULT'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_TYPE` int DEFAULT NULL AFTER `VIEW_DEFAULT`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_TYPE'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_FILE` bigint DEFAULT NULL AFTER `VIEW_CONTYPE`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_FILE'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_CHECKAUTH` tinyint(1) NOT NULL DEFAULT ''0'' AFTER `VIEW_FILE`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_CHECKAUTH'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_AUTOFRESHINTERVAL` int NOT NULL DEFAULT ''0'' AFTER `VIEW_CHECKAUTH`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_AUTOFRESHINTERVAL'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE `SW_SYS_VIEW` ADD COLUMN `VIEW_CANEDIT` tinyint(1) NOT NULL DEFAULT ''0'' AFTER `VIEW_AUTOFRESHINTERVAL`', 'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SW_SYS_VIEW' AND COLUMN_NAME = 'VIEW_CANEDIT'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_VIEW_ITEM` ADD COLUMN `SW_SYS_VIEW_ItemsVIEW_ID` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_view_item_owner` (`SW_SYS_VIEW_ItemsVIEW_ID`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_VIEW_ITEM'
    AND COLUMN_NAME = 'SW_SYS_VIEW_ItemsVIEW_ID'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_VIEW_OPERATION` ADD COLUMN `SW_SYS_VIEW_OperationsVIEW_ID` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_view_operation_owner` (`SW_SYS_VIEW_OperationsVIEW_ID`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_VIEW_OPERATION'
    AND COLUMN_NAME = 'SW_SYS_VIEW_OperationsVIEW_ID'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `SW_SYS_OPERATIONVIEW_ITEM` ADD COLUMN `SW_SYS_OPERATIONVIEW_ParamsSysId` bigint DEFAULT NULL AFTER `SysId`, ADD KEY `ix_sw_sys_operationview_item_owner` (`SW_SYS_OPERATIONVIEW_ParamsSysId`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'SW_SYS_OPERATIONVIEW_ITEM'
    AND COLUMN_NAME = 'SW_SYS_OPERATIONVIEW_ParamsSysId'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_view_item_edit_type = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_view_item` ADD COLUMN `edit_type` int DEFAULT 0 AFTER `format_regx`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_view_item'
    AND COLUMN_NAME = 'edit_type'
);
PREPARE add_view_item_edit_type_stmt FROM @add_view_item_edit_type;
EXECUTE add_view_item_edit_type_stmt;
DEALLOCATE PREPARE add_view_item_edit_type_stmt;

SET @add_view_item_show_index = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `fool_sys_view_item` ADD COLUMN `show_index` int NOT NULL DEFAULT 0 AFTER `edit_type`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fool_sys_view_item'
    AND COLUMN_NAME = 'show_index'
);
PREPARE add_view_item_show_index_stmt FROM @add_view_item_show_index;
EXECUTE add_view_item_show_index_stmt;
DEALLOCATE PREPARE add_view_item_show_index_stmt;

INSERT INTO `fool_sys_model` (`id`, `name`, `text`, `remark`, `model_type`, `class_name`, `table_name`, `auto_sys_id`, `id_property`)
SELECT 100, 'Order', 'Order', 'Market order smoke model', 0, 'org.fool.framework.market.Order', 'market_order', 0, 1001
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model` WHERE `name` = 'Order');

UPDATE `fool_sys_model`
SET `auto_sys_id` = 0,
    `id_property` = 1001
WHERE `name` = 'Order';

INSERT INTO `fool_sys_model` (`id`, `name`, `text`, `remark`, `model_type`, `class_name`, `table_name`, `auto_sys_id`, `id_property`)
SELECT 101, 'OrderItem', 'OrderItem', 'Market order item smoke model', 0, 'org.fool.framework.market.OrderItem', 'market_order_item', 0, 1011
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model` WHERE `name` = 'OrderItem');

UPDATE `fool_sys_model`
SET `auto_sys_id` = 0,
    `id_property` = 1011
WHERE `name` = 'OrderItem';

INSERT INTO `fool_sys_model` (`id`, `name`, `text`, `remark`, `model_type`, `class_name`, `table_name`, `auto_sys_id`, `id_property`)
SELECT 102, 'OrderState', 'Order State', 'Market order state enum', 2, 'org.fool.framework.market.OrderState', NULL, 0, NULL
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model` WHERE `name` = 'OrderState');

UPDATE `fool_sys_model`
SET `model_type` = 2,
    `class_name` = 'org.fool.framework.market.OrderState',
    `table_name` = NULL,
    `auto_sys_id` = 0,
    `id_property` = NULL
WHERE `name` = 'OrderState';

INSERT INTO `fool_sys_model_enum` (`name`, `value`, `remark`, `owner`)
SELECT 'Open', '0', 'Open order state', 102
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_enum` WHERE `owner` = 102 AND `value` = '0');

INSERT INTO `fool_sys_model_enum` (`name`, `value`, `remark`, `owner`)
SELECT 'Filled', '1', 'Filled order state', 102
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_enum` WHERE `owner` = 102 AND `value` = '1');

UPDATE `fool_sys_model_enum`
SET `value` = '0'
WHERE `owner` = 102 AND `value` = 'OPEN';

UPDATE `fool_sys_model_enum`
SET `value` = '1'
WHERE `owner` = 102 AND `value` = 'FILLED';

INSERT INTO `SW_SYS_MODEL` (`MODEL_ID`, `MODEL_NAME`, `MODEL_CLASS`, `MODEL_CONTYPE`, `MODEL_DATABASETABLE`, `MODEL_MODULE`, `MODEL_AUTOID`, `MODEL_CON`, `MODEL_DEFAULTOWNER`)
SELECT 102, 'OrderState', 'org.fool.framework.market.OrderState', NULL, NULL, NULL, 0, NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM `SW_SYS_MODEL` WHERE `MODEL_ID` = 102);

INSERT INTO `SW_SYS_EMUNVALUE` (`EMUN_STR`, `EMUN_VALUE`, `SW_SYS_MODEL_EnumValuesMODEL_ID`)
SELECT 'Open', 0, 102
WHERE NOT EXISTS (SELECT 1 FROM `SW_SYS_EMUNVALUE` WHERE `SW_SYS_MODEL_EnumValuesMODEL_ID` = 102 AND `EMUN_VALUE` = 0);

INSERT INTO `SW_SYS_EMUNVALUE` (`EMUN_STR`, `EMUN_VALUE`, `SW_SYS_MODEL_EnumValuesMODEL_ID`)
SELECT 'Filled', 1, 102
WHERE NOT EXISTS (SELECT 1 FROM `SW_SYS_EMUNVALUE` WHERE `SW_SYS_MODEL_EnumValuesMODEL_ID` = 102 AND `EMUN_VALUE` = 1);

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1001, 'orderId', 'Order ID', NULL, 0, 100, NULL, NULL, 'order_id', 3, 0, 1, '', 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 100 AND `name` = 'orderId');

UPDATE `fool_sys_model_property`
SET `property_type` = 3,
    `allow_db_null` = 0,
    `is_check` = 1,
    `ix_group` = '',
    `multi_map` = 0
WHERE `owner` = 100 AND `name` = 'orderId';

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1002, 'symbol', 'Symbol', NULL, 0, 100, NULL, NULL, 'order_symbol', 11, 0, 0, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 100 AND `name` = 'symbol');

UPDATE `fool_sys_model_property`
SET `property_type` = 11,
    `allow_db_null` = 0,
    `is_check` = 0,
    `ix_group` = NULL,
    `multi_map` = 0
WHERE `owner` = 100 AND `name` = 'symbol';

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1003, 'state', 'State', 102, 0, 100, NULL, NULL, 'order_state', 15, 1, 0, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 100 AND `name` = 'state');

UPDATE `fool_sys_model_property`
SET `property_model` = 102,
    `property_type` = 15,
    `allow_db_null` = 1,
    `is_check` = 0,
    `ix_group` = NULL,
    `multi_map` = 0
WHERE `owner` = 100 AND `name` = 'state';

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1011, 'itemId', 'Item ID', NULL, 0, 101, NULL, NULL, 'item_id', 3, 0, 1, '', 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 101 AND `name` = 'itemId');

UPDATE `fool_sys_model_property`
SET `property_type` = 3,
    `allow_db_null` = 0,
    `is_check` = 1,
    `ix_group` = '',
    `multi_map` = 0
WHERE `owner` = 101 AND `name` = 'itemId';

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1012, 'itemName', 'Item Name', NULL, 0, 101, NULL, NULL, 'item_name', 11, 0, 0, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 101 AND `name` = 'itemName');

UPDATE `fool_sys_model_property`
SET `property_type` = 11,
    `allow_db_null` = 0,
    `is_check` = 0,
    `ix_group` = NULL,
    `multi_map` = 0
WHERE `owner` = 101 AND `name` = 'itemName';

INSERT INTO `fool_sys_model_property` (`id`, `name`, `remark`, `property_model`, `is_collection`, `owner`, `filter`, `format`, `column`, `property_type`, `allow_db_null`, `is_check`, `ix_group`, `multi_map`)
SELECT 1004, 'items', 'Items', 101, 1, 100, NULL, NULL, NULL, 16, 1, 0, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 100 AND `name` = 'items');

UPDATE `fool_sys_model_property`
SET `property_model` = 101,
    `is_collection` = 1,
    `column` = NULL,
    `property_type` = 16,
    `allow_db_null` = 1,
    `is_check` = 0,
    `ix_group` = NULL,
    `multi_map` = 0
WHERE `owner` = 100 AND `name` = 'items';

INSERT INTO `SW_SYS_RELATION` (`SW_SYS_RELATION_TYPE`, `SW_SYS_RELATION_SOURCEPROPERTY`, `SW_SYS_RELATION_TARGETPROPERTY`, `SW_SYS_RELATION_TABLE`, `SW_SYS_RELATION_SOURCECOL`, `SW_SYS_RELATION_TARGETCOL`, `SW_SYS_RELATION_CANBENULL`)
SELECT 0, 1004, 1011, 'market_order_item', 'item_id', 'order_id', 0
WHERE NOT EXISTS (SELECT 1 FROM `SW_SYS_RELATION` WHERE `SW_SYS_RELATION_SOURCEPROPERTY` = 1004);

UPDATE `SW_SYS_RELATION`
SET `SW_SYS_RELATION_TYPE` = 0,
    `SW_SYS_RELATION_TARGETPROPERTY` = 1011,
    `SW_SYS_RELATION_TABLE` = 'market_order_item',
    `SW_SYS_RELATION_SOURCECOL` = 'item_id',
    `SW_SYS_RELATION_TARGETCOL` = 'order_id',
    `SW_SYS_RELATION_CANBENULL` = 0
WHERE `SW_SYS_RELATION_SOURCEPROPERTY` = 1004;

INSERT INTO `fool_sys_view` (`id`, `view_name`, `view_text`, `view_remark`, `view_title`, `view_type`, `view_model`, `filter`, `auto_fresh_interval`, `view_model_class`)
SELECT 100, 'OrderList', 'OrderList', 'Seeded Docker smoke view', 'Order List', 0, 'Order', '', 0, 'org.fool.framework.market.Order'
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view` WHERE `view_name` = 'OrderList');

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `edit_type`, `show_index`, `view_id`)
SELECT 1001, 'Order ID', 'Order ID', 'Order ID', 'orderId', 0, 0, NULL, NULL, NULL, 0, 1, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'orderId');

UPDATE `fool_sys_view_item`
SET `show_index` = 1
WHERE `view_id` = 100 AND `model_property` = 'orderId';

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `edit_type`, `show_index`, `view_id`)
SELECT 1002, 'Symbol', 'Symbol', 'Symbol', 'symbol', 0, 0, NULL, NULL, NULL, 0, 2, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'symbol');

UPDATE `fool_sys_view_item`
SET `show_index` = 2
WHERE `view_id` = 100 AND `model_property` = 'symbol';

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `edit_type`, `show_index`, `view_id`)
SELECT 1003, 'State', 'State', 'State', 'state', 0, 0, NULL, NULL, NULL, 0, 3, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'state');

UPDATE `fool_sys_view_item`
SET `show_index` = 3
WHERE `view_id` = 100 AND `model_property` = 'state';

INSERT INTO `market_order` (
  `order_id`, `order_symbol`, `order_amount`, `order_price`, `order_type`, `order_state`, `order_user_id`, `order_auth_id`, `status`
)
SELECT 1001, 'BTC-USDT', 0.2500000000, 62500.0000000000, 'LIMIT', '0', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `market_order` WHERE `order_id` = 1001);

INSERT INTO `market_order` (
  `order_id`, `order_symbol`, `order_amount`, `order_price`, `order_type`, `order_state`, `order_user_id`, `order_auth_id`, `status`
)
SELECT 1002, 'ETH-USDT', 1.5000000000, 3450.0000000000, 'LIMIT', '1', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `market_order` WHERE `order_id` = 1002);

UPDATE `market_order`
SET `order_state` = '0'
WHERE `order_id` = 1001;

UPDATE `market_order`
SET `order_state` = '1'
WHERE `order_id` = 1002;

INSERT INTO `market_order_item` (`item_id`, `order_id`, `item_name`)
VALUES (2001, 1001, 'Legacy item')
ON DUPLICATE KEY UPDATE `order_id` = VALUES(`order_id`), `item_name` = VALUES(`item_name`);

INSERT INTO `market_order_item` (`item_id`, `order_id`, `item_name`)
VALUES (2004, 1001, 'Delete me')
ON DUPLICATE KEY UPDATE `order_id` = VALUES(`order_id`), `item_name` = VALUES(`item_name`);

DELETE FROM `market_order_item` WHERE `item_id` = 2003;
