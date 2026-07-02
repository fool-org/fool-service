package org.fool.framework.common.data;

public abstract class IItem<T> implements IItemInterface<T>, IBusinessObject {
    @Override
    @SuppressWarnings("unchecked")
    public void setParent(Object parent) {
        setTypedParent((T) parent);
    }

    protected abstract void setTypedParent(T parent);
}
