package org.fool.framework.dao.business;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * CREATE TABLE `market_order` (
 * `id` bigint NOT NULL AUTO_INCREMENT,
 * `order_id` bigint NOT NULL DEFAULT '0',
 * `order_symbol` varchar(200) NOT NULL DEFAULT '',
 * `order_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_created` timestamp NULL DEFAULT NULL,
 * `order_canceled` timestamp NULL DEFAULT NULL,
 * `order_finished` timestamp NULL DEFAULT NULL,
 * `order_stop_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_type` varchar(200) NOT NULL DEFAULT '',
 * `order_filled_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_filled_cash_amount` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_filled_fees` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * `order_source` varchar(200) NOT NULL DEFAULT '',
 * `order_state` varchar(200) NOT NULL DEFAULT '',
 * `order_user_id` bigint NOT NULL DEFAULT '0',
 * `order_auth_id` bigint NOT NULL,
 * `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
 * `status` int NOT NULL DEFAULT '0',
 * `order_plan_id` bigint NOT NULL DEFAULT '0',
 * `order_plan_circle_id` bigint NOT NULL DEFAULT '0',
 * `order_filled_price` decimal(20,10) NOT NULL DEFAULT '0.0000000000',
 * PRIMARY KEY (`id`),
 * KEY `order_id` (`order_id`),
 * KEY `order_user_id` (`order_user_id`),
 * KEY `order_auth_id` (`order_auth_id`),
 * KEY `order_state` (`order_state`),
 * KEY `order_plan_id` (`order_plan_id`),
 * KEY `order_plan_circle_id` (`order_plan_circle_id`),
 * KEY `ix_order_id` (`order_id`)
 * )
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Table("market_order")
@Data
public class Order {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Id
    private String id;
    private BigDecimal orderPrice;
    private BigDecimal orderStopPrice;
    @SqlGenerate(SqlGenerateConfig.INSERT)
    private LocalDateTime createAt;
    private String orderSymbol;
    private List<OrderItems> orderItemsList;
}
