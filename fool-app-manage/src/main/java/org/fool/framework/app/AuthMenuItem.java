package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_MENU", columnPrefix = "AUTH_MENU_")
@Data
public class AuthMenuItem {
    @Id
    @SqlGenerate
    @Column("AUTH_MENU_ID")
    private Long menuId;
    @Column("AUTH_MENU_TEXT")
    private String text;
    @Column("AUTH_MENU_SHORTCUTKEY")
    private String shortcutKey;
    @Column("AUTH_MENU_IMAGE")
    private String image;
    @Column("AUTH_MENU_VISIABLE")
    private Boolean defaultVisible = false;
    @Column("AUTH_MENU_ENABLE")
    private Boolean defaultEnable = false;
    @Column("AUTH_MENU_VIEWID")
    private Long viewId = 0L;
    @Column("AUTH_MENU_TEMPLATEFILE")
    private String templateFile;
    @Column("AUTH_MENU_INDEX")
    private Integer index = 0;

    public static AuthMenuItem fromBootstrap(BootstrapMenuItem bootstrap) {
        AuthMenuItem item = new AuthMenuItem();
        item.setText(bootstrap.getText());
        item.setViewId(bootstrap.getViewId() == null ? 0L : bootstrap.getViewId());
        return item;
    }
}
