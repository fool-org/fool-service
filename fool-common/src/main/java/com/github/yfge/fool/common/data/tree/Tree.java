package com.github.yfge.fool.common.data.tree;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@ToString
@Getter
public class Tree<T> {
     List<TreeNode<T>> nodeList;
}
