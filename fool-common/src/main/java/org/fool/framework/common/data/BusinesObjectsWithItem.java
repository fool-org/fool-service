package org.fool.framework.common.data;

public class BusinesObjectsWithItem<T> {
    private SubItemList<T> list;

    public SubItemList<T> getList() {
        return list;
    }

    public void setList(SubItemList<T> list) {
        this.list = list;
    }
}
