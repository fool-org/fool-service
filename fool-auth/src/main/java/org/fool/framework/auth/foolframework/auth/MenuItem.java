package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_MENU", columnPrefix = "AUTH_MENU_")
@Data
public class MenuItem {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "AUTH_MENU_ID", key = true, generationType = GenerationType.ON_INSERT)
    private Long id;
    @Column("AUTH_MENU_TEXT")
    private String text;
    @Column("AUTH_MENU_SHORTCUTKEY")
    private String shortcutKey;
    @Column("AUTH_MENU_IMAGE")
    private String image;
    @Column("AUTH_MENU_VISIABLE")
    private Boolean defaultVisible;
    @Column("AUTH_MENU_ENABLE")
    private Boolean defaultEnable;
    @Column("AUTH_MENU_VIEWID")
    private Long viewId;
    @Column("AUTH_MENU_TEMPLATEFILE")
    private String templateFile;
    @Column("AUTH_MENU_INDEX")
    private Integer index;
}
