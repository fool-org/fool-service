USE car_wash;

CREATE TABLE IF NOT EXISTS `market_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL DEFAULT '0',
  `order_symbol` varchar(200) NOT NULL DEFAULT '',
  `order_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_created` timestamp NULL DEFAULT NULL,
  `order_canceled` timestamp NULL DEFAULT NULL,
  `order_finished` timestamp NULL DEFAULT NULL,
  `order_stop_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_type` varchar(200) NOT NULL DEFAULT '',
  `order_filled_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_filled_cash_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_filled_fees` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  `order_source` varchar(200) NOT NULL DEFAULT '',
  `order_state` varchar(200) NOT NULL DEFAULT '',
  `order_user_id` bigint NOT NULL DEFAULT '0',
  `order_customer_id` bigint NOT NULL DEFAULT '0',
  `order_auth_id` bigint NOT NULL DEFAULT '0',
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int NOT NULL DEFAULT '0',
  `order_plan_id` bigint NOT NULL DEFAULT '0',
  `order_plan_circle_id` bigint NOT NULL DEFAULT '0',
  `order_filled_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `order_user_id` (`order_user_id`),
  KEY `order_customer_id` (`order_customer_id`),
  KEY `order_auth_id` (`order_auth_id`),
  KEY `order_state` (`order_state`),
  KEY `order_plan_id` (`order_plan_id`),
  KEY `order_plan_circle_id` (`order_plan_circle_id`),
  KEY `ix_order_id` (`order_id`)
);

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `market_order` ADD COLUMN `order_customer_id` bigint NOT NULL DEFAULT ''0'' AFTER `order_user_id`, ADD KEY `order_customer_id` (`order_customer_id`)',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'market_order'
    AND COLUMN_NAME = 'order_customer_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `market_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL DEFAULT '0',
  `order_id` bigint NOT NULL DEFAULT '0',
  `item_name` varchar(200) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_market_order_item_item_id` (`item_id`),
  KEY `ix_market_order_item_order_id` (`order_id`)
);

CREATE TABLE IF NOT EXISTS `market_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL DEFAULT '0',
  `display_name` varchar(200) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_market_customer_customer_id` (`customer_id`),
  KEY `ix_market_customer_display_name` (`display_name`)
);
