package org.fool.framework.common.data.tree;

public interface ITreeData {
    TreeNodeCompareResult TreeDataComPare(ITreeData ob);
    void SetParent(ITreeData ob);
}
