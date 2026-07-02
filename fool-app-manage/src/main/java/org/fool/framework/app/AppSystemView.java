package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.ConnectionType;

@Table("SW_SYS_VIEW")
@Data
public class AppSystemView {
    public static final int CONNECTION_TYPE_APP_SYS = ConnectionType.APP_SYS.code();

    @Id
    @SqlGenerate
    @Column("VIEW_ID")
    private Long viewId;
    @Column("VIEW_NAME")
    private String viewName;
    @Column("VIEW_CONTYPE")
    private Integer connectionType = CONNECTION_TYPE_APP_SYS;

    public static AppSystemView appSys(String viewName) {
        AppSystemView view = new AppSystemView();
        view.setViewName(viewName);
        view.setConnectionType(CONNECTION_TYPE_APP_SYS);
        return view;
    }
}
