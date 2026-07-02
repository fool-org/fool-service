package org.fool.framework.common.data.tree;

import java.util.List;

public class TreeDataFactory<T extends ITreeData> extends ITreeFactory<T> {
    public List<TreeNode<T>> createTreeByLevel(List<T> items) {
        return super.createTreeByLevel(items, (child, parent) -> child.TreeDataComPare(parent));
    }
}
