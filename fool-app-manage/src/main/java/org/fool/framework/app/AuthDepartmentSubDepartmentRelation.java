package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_DEPARTMENT_SubDepartments")
@Data
public class AuthDepartmentSubDepartmentRelation {
    @Column("SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID")
    private Long parentDepartmentId;
    @Column("SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM")
    private Long subDepartmentId;
}
