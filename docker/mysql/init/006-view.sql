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
  `view_model_class` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fool_sys_view_name` (`view_name`),
  KEY `ix_fool_sys_view_model` (`view_model`)
);

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
  `view_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_fool_sys_view_item_view_id` (`view_id`),
  KEY `ix_fool_sys_view_item_property` (`model_property`)
);

INSERT INTO `fool_sys_model` (`id`, `name`, `text`, `remark`, `model_type`, `class_name`, `table_name`, `auto_sys_id`, `id_property`)
SELECT 100, 'Order', 'Order', 'Market order smoke model', 3, 'org.fool.framework.market.Order', 'market_order', 0, NULL
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model` WHERE `name` = 'Order');

UPDATE `fool_sys_model`
SET `auto_sys_id` = 0
WHERE `name` = 'Order';

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
SELECT 1003, 'state', 'State', NULL, 0, 100, NULL, NULL, 'order_state', 11, 1, 0, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_model_property` WHERE `owner` = 100 AND `name` = 'state');

UPDATE `fool_sys_model_property`
SET `property_type` = 11,
    `allow_db_null` = 1,
    `is_check` = 0,
    `ix_group` = NULL,
    `multi_map` = 0
WHERE `owner` = 100 AND `name` = 'state';

INSERT INTO `fool_sys_view` (`id`, `view_name`, `view_text`, `view_remark`, `view_title`, `view_type`, `view_model`, `filter`, `view_model_class`)
SELECT 100, 'OrderList', 'OrderList', 'Seeded Docker smoke view', 'Order List', 0, 'Order', '', 'org.fool.framework.market.Order'
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view` WHERE `view_name` = 'OrderList');

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `view_id`)
SELECT 1001, 'Order ID', 'Order ID', 'Order ID', 'orderId', 0, 0, NULL, NULL, NULL, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'orderId');

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `view_id`)
SELECT 1002, 'Symbol', 'Symbol', 'Symbol', 'symbol', 0, 0, NULL, NULL, NULL, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'symbol');

INSERT INTO `fool_sys_view_item` (`id`, `item_name`, `item_label`, `item_legend`, `model_property`, `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`, `view_id`)
SELECT 1003, 'State', 'State', 'State', 'state', 0, 0, NULL, NULL, NULL, 100
WHERE NOT EXISTS (SELECT 1 FROM `fool_sys_view_item` WHERE `view_id` = 100 AND `model_property` = 'state');

INSERT INTO `market_order` (
  `order_id`, `order_symbol`, `order_amount`, `order_price`, `order_type`, `order_state`, `order_user_id`, `order_auth_id`, `status`
)
SELECT 1001, 'BTC-USDT', 0.2500000000, 62500.0000000000, 'LIMIT', 'OPEN', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `market_order` WHERE `order_id` = 1001);

INSERT INTO `market_order` (
  `order_id`, `order_symbol`, `order_amount`, `order_price`, `order_type`, `order_state`, `order_user_id`, `order_auth_id`, `status`
)
SELECT 1002, 'ETH-USDT', 1.5000000000, 3450.0000000000, 'LIMIT', 'FILLED', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `market_order` WHERE `order_id` = 1002);
