package org.fool.framework.app.reflective;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.math.BigDecimal;
import java.util.List;

final class PackageScanFixtures {
    private PackageScanFixtures() {
    }

    @Table("PKG_BASE_RECORD")
    static class PackageBaseRecord {
        @Id
        @Column("BASE_ID")
        private Long baseId;
    }

    @Table("PKG_ORDER_LINE")
    static class PackageOrderLine {
        @Id
        @Column("LINE_ID")
        private Long lineId;
    }

    @Table("PKG_ORDER")
    static class PackageOrder extends PackageBaseRecord {
        @Id
        @Column("ORDER_ID")
        private Long orderId;
        @Column("ORDER_AMOUNT")
        private BigDecimal amount;
        @Column("ORDER_STATE")
        private PackageOrderState state;
        private List<PackageOrderLine> lines;
    }

    enum PackageOrderState {
        OPEN,
        CLOSED
    }
}
