package com.github.yfge.fool.common.data.tree;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * @param <T>
 */
@Builder
@AllArgsConstructor
@Getter
@ToString
public class TreeNode<T> {
    public T data;

    @JsonIgnore
    @ToString.Exclude
    public TreeNode<T> parent;
    /// <summary>
    /// 子结点
    /// </summary>
    public List<TreeNode<T>> children;

    @ToString.Exclude
    @JsonIgnore
    Tree<T> tree;

    @JsonIgnore
    int level;


    public TreeNode() {
        children = new LinkedList<>();

    }

    @JsonIgnore
    public TreeNode<T> getNext() {
        if (this.parent != null) {
            int i = this.parent.children.indexOf(this);
            if (i < this.parent.children.size() - 1)
                return this.parent.children.get(i + 1);

        }
        return null;
    }

    @JsonIgnore
    public int getWidth() {
        if (this.children.size() == 0)
            return 1;
        else {
            int sum = 0;
            for (TreeNode<T> node : this.children)
                sum += node.getWidth();
            return sum;
        }
    }




}
