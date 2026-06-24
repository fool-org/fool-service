package org.fool.framework.app.reference.order;

import org.fool.framework.app.reference.shared.ReferenceCustomer;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("REF_ORDER")
public class ReferenceOrder {
    @Id
    @Column("ORDER_ID")
    Long orderId;

    ReferenceCustomer customer;
}
