package org.fool.framework.common.data.tree;



public interface TreeNodeCompare<T> {
    public TreeNodeCompareResult apply(T child, T parent);
}
