package org.fool.framework.view.model;

/**
 * 视图类型
 */
public enum ViewType {
    /**
     * 列表视图
     */
    ListView(0),
    /**
     * 查询视图
     */
    QueryView(2),
    /**
     * 详情视图
     */
    DetailView(1),
    /**
     * 组件视图
     */
    GroupView(4),
    /**
     * 地图视图
     */
    MapView(3),
    /**
     * 报表
     */
    ReportView(5),
    ButtonListView(6),
    GridView(7);

    private final int code;

    ViewType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

}
