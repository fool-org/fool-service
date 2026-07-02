package org.fool.framework.common.data.graphic;

import java.util.ArrayList;
import java.util.List;

public class GraphicNode<T> {
    private final T data;
    private final List<T> pointIn = new ArrayList<>();
    private final List<T> pointOut = new ArrayList<>();

    public GraphicNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    List<T> getPointIn() {
        return pointIn;
    }

    List<T> getPointOut() {
        return pointOut;
    }

    public boolean addIn(T item) {
        if (pointIn.contains(item)) {
            return false;
        }
        return pointIn.add(item);
    }

    public boolean addOut(T item) {
        if (pointOut.contains(item)) {
            return false;
        }
        return pointOut.add(item);
    }
}
