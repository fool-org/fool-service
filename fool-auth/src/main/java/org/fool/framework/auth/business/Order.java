package org.fool.framework.auth.business;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.fool.framework.common.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Table("market_order")
@Data
public class Order {
    private String id;
    private BigDecimal orderPrice;
    private List<OrderItems> orderItemsList;
}
