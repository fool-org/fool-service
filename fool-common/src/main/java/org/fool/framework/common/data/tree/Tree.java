package org.fool.framework.common.data.tree;

import lombok.Getter;
import lombok.ToString;

import java.util.List;


@ToString
@Getter
public class Tree<T> {
     List<TreeNode<T>> nodeList;
}
