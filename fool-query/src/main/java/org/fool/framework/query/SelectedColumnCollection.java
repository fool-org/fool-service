package org.fool.framework.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SelectedColumnCollection implements Iterable<SelectedColumn> {
    private final List<SelectedColumn> items = new ArrayList<>();

    public boolean add(SelectedColumn item) {
        boolean duplicateName = items.stream()
                .anyMatch(existing -> Objects.equals(existing.getSelectedName(), item.getSelectedName()));
        if (duplicateName) {
            throw new IllegalArgumentException("已经有相同的列名称存在");
        }
        items.add(item);
        reindex();
        return true;
    }

    public void clear() {
        items.clear();
    }

    public boolean contains(SelectedColumn item) {
        return items.contains(item);
    }

    public SelectedColumn get(int index) {
        return items.get(index);
    }

    public void set(int index, SelectedColumn item) {
        throw new UnsupportedOperationException("NotImplementedException");
    }

    public int indexOf(SelectedColumn item) {
        return items.indexOf(item);
    }

    public boolean isReadOnly() {
        return true;
    }

    public boolean remove(SelectedColumn item) {
        return items.remove(item);
    }

    public int size() {
        return items.size();
    }

    public List<SelectedColumn> asList() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public Iterator<SelectedColumn> iterator() {
        return asList().iterator();
    }

    private void reindex() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelectedIndex(i);
        }
    }
}
