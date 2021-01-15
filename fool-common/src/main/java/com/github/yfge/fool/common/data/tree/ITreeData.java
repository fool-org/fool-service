package com.github.yfge.fool.common.data.tree;

public interface ITreeData {
    TreeNodeCompareResult TreeDataComPare(ITreeData ob);
    void SetParent(ITreeData ob);
}
