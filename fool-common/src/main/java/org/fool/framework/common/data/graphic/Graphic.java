package org.fool.framework.common.data.graphic;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Graphic<T> {
    private final List<GraphicNode<T>> nodes = new ArrayList<>();

    public List<GraphicNode<T>> getNodes() {
        return nodes;
    }

    public T getTopNode() {
        for (GraphicNode<T> node : nodes) {
            if (node.getPointIn().isEmpty()) {
                return node.getData();
            }
        }
        for (GraphicNode<T> node : nodes) {
            if (node.getPointIn().size() == 1 && node.getPointIn().contains(node.getData())) {
                return node.getData();
            }
        }
        return null;
    }

    public void remove(T data) {
        GraphicNode<T> node = get(data);
        if (node == null) {
            throw new NoSuchElementException();
        }
        for (GraphicNode<T> other : nodes) {
            other.getPointIn().remove(node.getData());
            other.getPointOut().remove(node.getData());
        }
        nodes.remove(node);
    }

    public void addEdge(T from, T to) {
        add(from);
        add(to);
        get(from).addOut(to);
        get(to).addIn(from);
    }

    public GraphicNode<T> get(T data) {
        for (GraphicNode<T> node : nodes) {
            if (Objects.equals(node.getData(), data)) {
                return node;
            }
        }
        return null;
    }

    public void add(T data) {
        if (get(data) == null) {
            nodes.add(new GraphicNode<>(data));
        }
    }
}
