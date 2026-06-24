package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.time.LocalDateTime;
import java.util.List;

@Table("SW_APPLICATION")
@Data
public class ApplicationDefinition {
    @Id
    @Column("SW_APP_APPLICATIONID")
    private String appId;
    @Column("SW_APP_KEY")
    private String appKey;
    @Column("SW_APP_TYPE")
    private AppType appType;
    @Column("SW_APP_AVATAR")
    private String avatar;
    @Column("SW_APP_COMPANY")
    private String company;
    @Column("SW_APP_CREATEIME")
    private LocalDateTime createTime;
    @Column("SW_APP_CREATOR")
    private String creatorId;
    @Column("SW_APP_INITPIC")
    private String initImage;
    @Column("SW_APP_NAME")
    private String name;
    @Column("SW_APP_NOTE")
    private String note;
    @Column("SW_APP_OWNER")
    private String ownerId;
    @Column("SW_APP_RELEASETIME")
    private LocalDateTime releaseTime;
    @Column("SW_APP_UPDATETIME")
    private LocalDateTime updateTime;
    @Column("SW_APP_URL")
    private String url;
    @Column("SW_APP_VERSION")
    private String version;
    @Column("SW_APP_CON")
    private String sysCon;
    @Column("SW_APP_VIEW")
    private Long defaultView;
    private List<StoreDatabase> dataBase;
}
