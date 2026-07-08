USE car_wash;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `market_symbols` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_currency` varchar(200) NOT NULL DEFAULT '',
  `quote_currency` varchar(200) NOT NULL DEFAULT '',
  `price_precision` int NOT NULL DEFAULT '0',
  `amount_precision` int NOT NULL DEFAULT '0',
  `symbol_partition` varchar(200) NOT NULL DEFAULT '',
  `symbol` varchar(200) NOT NULL DEFAULT '',
  `value_precision` int NOT NULL DEFAULT '0',
  `exchange_type` int NOT NULL DEFAULT '1',
  `price_min` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `price_max` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `price_tick_size` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `price_multiplier_up` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `price_multiplier_down` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `price_multiplier_arg_min` int NOT NULL DEFAULT '0',
  `lot_size_qty_min` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `lot_size_qty_max` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `lot_size_qty_step_size` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `notional_min` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `notional_apply_to_market` int NOT NULL DEFAULT '0',
  `notional_arg_min` int NOT NULL DEFAULT '0',
  `iceberg_parts` int NOT NULL DEFAULT '0',
  `market_lot_size_qty_min` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `market_lot_size_qty_max` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `market_lot_size_qty_step_size` decimal(30,15) NOT NULL DEFAULT '0.000000000000000',
  `max_orders_num` int NOT NULL DEFAULT '0',
  `max_algo_orders_num` int NOT NULL DEFAULT '0',
  `max_iceberg_orders_num` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);
