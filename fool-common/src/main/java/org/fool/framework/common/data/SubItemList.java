package org.fool.framework.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubItemList<I> extends ArrayList<I> {
    private final List<I> addedList = new ArrayList<>();
    private final List<I> updatedList = new ArrayList<>();
    private final List<I> deleteList = new ArrayList<>();

    public List<I> getAddedList() {
        return addedList;
    }

    public List<I> getUpdatedList() {
        return updatedList;
    }

    public List<I> getDeleteList() {
        return deleteList;
    }

    @Override
    public boolean add(I item) {
        addedList.add(item);
        return super.add(item);
    }

    @Override
    public void add(int index, I element) {
        addedList.add(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends I> items) {
        addedList.addAll(items);
        return super.addAll(items);
    }

    @Override
    public boolean addAll(int index, Collection<? extends I> items) {
        addedList.addAll(items);
        return super.addAll(index, items);
    }

    @Override
    public I set(int index, I element) {
        updatedList.add(element);
        return super.set(index, element);
    }

    @Override
    public I remove(int index) {
        I item = get(index);
        deleteList.add(item);
        addedList.remove(item);
        return super.remove(index);
    }

    @Override
    public boolean remove(Object item) {
        int index = indexOf(item);
        if (index < 0) {
            return false;
        }
        remove(index);
        return true;
    }
}
