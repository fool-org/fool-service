package org.fool.framework.view.model;

import lombok.Data;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

import java.util.List;

@Data
@Table("sys_mvc_view")
public class View {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private Long id;
    @Id
    private String viewName;
    private String viewText;
    private String viewRemark;
    private String viewTitle;
    private ViewType viewType;
    private List<ViewItem> items;
    private String viewModel;
    private List<ListViewItem> listItems;
}
