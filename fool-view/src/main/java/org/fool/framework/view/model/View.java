package org.fool.framework.view.model;

import lombok.Data;
import org.fool.framework.common.annotation.*;

import java.util.List;

@Data
@Table("fool_sys_view")
public class View {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private Long id;
    @Id
    private String viewName;
    private String viewText;
    private String viewRemark;
    private String viewTitle;
    private ViewType viewType;
    private String viewModel;
    @Column("view_id")
    private List<ViewItem> listItems;
    private String viewModelClass;
}
