package org.fool.framework.common.data.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TreeTest {
    @Test
    public void createTreeReturnsIterableLevelOrderTree() {
        TreeNode<String> root = TreeNode.<String>builder()
                .data("root")
                .children(new ArrayList<>())
                .build();
        root.children.add(TreeNode.<String>builder()
                .data("child")
                .children(new ArrayList<>())
                .parent(root)
                .build());

        Tree<String> tree = new ITreeFactory<String>().CreateTree(List.of(root));

        List<String> seen = new ArrayList<>();
        for (TreeNode<String> node : tree) {
            seen.add(node.data);
        }

        assertEquals(List.of("root", "child"), seen);
        assertSame(tree, root.tree);
    }

    @Test
    public void addArrayToLeafCreatesUsableChildNodes() {
        Tree<String> tree = new Tree<>();
        ITreeFactory<String> factory = new ITreeFactory<>();

        factory.addArrayToLeaf(tree, new String[]{"root", "child"});

        assertEquals(1, factory.CalTreeWidth(tree));
        assertEquals("child", factory.getBottomNodes(tree).get(0).data);
    }

    @Test
    public void treeDataFactoryUsesTreeDataComparison() {
        List<SampleTreeData> items = new ArrayList<>(List.of(
                new SampleTreeData("child", 1),
                new SampleTreeData("root", 0)));

        List<TreeNode<SampleTreeData>> roots = new TreeDataFactory<SampleTreeData>().createTreeByLevel(items);

        assertEquals("root", roots.get(0).data.name);
        assertEquals("child", roots.get(0).children.get(0).data.name);
    }

    private static class SampleTreeData implements ITreeData {
        private final String name;
        private final int level;

        private SampleTreeData(String name, int level) {
            this.name = name;
            this.level = level;
        }

        @Override
        public TreeNodeCompareResult TreeDataComPare(ITreeData ob) {
            SampleTreeData other = (SampleTreeData) ob;
            if (level > other.level) {
                return TreeNodeCompareResult.Child;
            }
            if (level < other.level) {
                return TreeNodeCompareResult.Parent;
            }
            return name.equals(other.name) ? TreeNodeCompareResult.Equal : TreeNodeCompareResult.NextNode;
        }

        @Override
        public void SetParent(ITreeData ob) {
        }
    }
}
