package com.github.yfge.fool.common.data.tree;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ITreeFactory<T> {
//    private TreeNodeCompare<T> compare;

    /// <summary>
    /// 新建一个树的生成器，同时传入一个委托，在生成树型结构时，会调用 传入的委托比较两个点
    /// </summary>
    /// <param name="Compare"></param>
//    public ITreeFactory(TreeNodeCompare<T> Compare) {
//        this.compare = Compare;
//    }

//    private int CompareResult(T ob1, T ob2) {
//        return compare.apply(ob1, ob2).getValue();
//
//    }
//
//    TreeNodeCompareResult TreeCompare(ITreeData child, ITreeData parent) {
//        return child.TreeDataComPare(parent);
//
//    }

    /// <summary>
    /// 按层建立树
    /// </summary>
    /// <param name="Items">建立树的集合</param>
    /// <returns>建立好的树结构</returns>
    public List<TreeNode<T>> createTreeByLevel
    (List<T> items,TreeNodeCompare<T> compare) {

        items.sort((p1,p2)->{
            return compare.apply(p1,p2).getValue();
        });
        List<TreeNode<T>> result = new LinkedList<TreeNode<T>>();
        TreeNode<T> lastNode = null;
        Queue<TreeNode<T>> queue = new LinkedBlockingQueue<TreeNode<T>>();
        TreeNode<T> currentNode = null;
        var current = result;
        if (items.size() > 0) {


            for (int i = 0; i < items.size(); i++) {

                TreeNode<T> AddedNode = TreeNode.<T>builder()
                        .data(items.get(i))
                        .parent(null)
                        .children(new LinkedList<>()).build();

                queue.add(AddedNode);//入队
                //看是否到了下一层的结点
                if (lastNode != null &&
                        (compare.apply(AddedNode.data, lastNode.data) == TreeNodeCompareResult.Child
                                || compare.apply(AddedNode.data, lastNode.data) == TreeNodeCompareResult.NextLevel)//下一层：即结点是子结点或是下一层结点
                ) {
                    currentNode = queue.poll();
                }
                //找到对应的父结点
                while (currentNode != null
                        &&
                        compare.apply(AddedNode.data, currentNode.data) != TreeNodeCompareResult.Child
                        && queue.size() > 0
                ) {
                    currentNode = queue.poll();
                }
                if (currentNode != null && compare.apply(AddedNode.data, currentNode.data) != TreeNodeCompareResult.Equal) {
                    AddedNode.parent = currentNode;
                    current = currentNode.children;
                }
                current.add(AddedNode);
                lastNode = AddedNode;
            }
        }
        return result;


    }

    public Tree<T> CreateTree(List<TreeNode<T>> items) {
        Tree<T> result = new Tree<T>();
        result.nodeList.addAll(items);
        items.forEach(p -> {
            p.tree = result;
        });
        return result;
    }

//
//    public TreeNode<T> CreateNode(TreeNode<T> parent) {
//
//        return new TreeNode<T>().builder()
//        .data(T){
//            Data =new
//
//            T(),Children =new List<TreeNode<T>>(),Parent =parent
//        };
//
//    }


    /// <summary>
    /// 计算一个树的宽度？起个什么名字比较好?
    /// </summary>
    /// <param name="tree"></param>
    /// <returns></returns>
    public int CalTreeWidth(Tree<T> tree) {

        int sum = 0;
        for (var node : tree.nodeList) {
            sum += calNodeWidth(node);
        }
        return sum;

    }

    /// <summary>
    /// 计算一个结点的宽度
    /// </summary>
    /// <param name="Node"></param>
    /// <returns></returns>
    private int calNodeWidth(TreeNode<T> node) {
        if (node.children.size() == 0)
            return 1;
        else {
            int sum = 0;
            for (var childNode : node.children) {
                sum += calNodeWidth(childNode);
            }
            return sum;
        }
    }


    public int addArrayToLeaf(Tree<T> tree, T[] itemArray) {
        return this.addArrayToLeaf(tree.nodeList, itemArray, 0, null);
    }

    public int addArrayToLeaf(List<TreeNode<T>> nodes, T[] itemArray, int offIndex, TreeNode<T> parent) {
        TreeNode<T> addnode = null;
        int addCount = 0;
        //加入的数组为空
        if (itemArray == null || itemArray.length == 0)
            return addCount;


        //当前没有元素
        if (itemArray.length - offIndex >= 1) {

            ///寻找是否存在相同的值
            int i = 0;
            while (i < nodes.size()) {

                if (nodes.get(i).data.equals(itemArray[offIndex])) {
                    addnode = nodes.get(i);
                    break;
                } else
                    addCount += calNodeWidth(nodes.get(i));
                i++;
            }

            //没有找到对应的值,加入新值
            if (addnode == null) {
                addnode = TreeNode.<T>builder()
                        .data(itemArray[offIndex])
                        .parent(parent)
                        .level(parent == null ? 0 : parent.level + 1).build();
                nodes.add(addnode);

            }

            //接着增加
            return addCount + addArrayToLeaf(addnode.children, itemArray, offIndex + 1, addnode);
        } else
            addCount++;
        return addCount;
    }

    /**
     * 得到最低层结点
     * @param tree
     * @return
     */
    public List<TreeNode<T>> getBottomNodes(Tree<T> tree) {
        List<TreeNode<T>> result = new LinkedList<TreeNode<T>>();
        for (var node : tree.nodeList) {
            result.addAll(getBottomNodes(node));

        }
        return result;

    }

    /**
     * 得到最底层结点
     * @param node
     * @return
     */
    private List<TreeNode<T>> getBottomNodes(TreeNode<T> node) {
        List<TreeNode<T>> result = new LinkedList<TreeNode<T>>();
        if (node.children != null && node.children.size() > 0) {
            node.children.forEach(p ->
                    result.addAll(getBottomNodes(p))
                );
        } else {
            result.add(node);
        }
        return result;
    }
}
