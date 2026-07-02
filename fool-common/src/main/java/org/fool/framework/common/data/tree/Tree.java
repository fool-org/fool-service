package org.fool.framework.common.data.tree;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@ToString
@Getter
public class Tree<T> implements Iterable<TreeNode<T>> {
    List<TreeNode<T>> nodeList = new LinkedList<>();

    @Override
    public Iterator<TreeNode<T>> iterator() {
        Queue<TreeNode<T>> queue = new ArrayDeque<>(nodeList);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public TreeNode<T> next() {
                TreeNode<T> node = queue.remove();
                if (node.children != null) {
                    queue.addAll(node.children);
                }
                return node;
            }
        };
    }
}
