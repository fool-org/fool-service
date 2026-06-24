package org.fool.framework.app.reference.shared;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("REF_CUSTOMER")
public class ReferenceCustomer {
    @Id
    @Column("CUSTOMER_ID")
    Long customerId;
}
