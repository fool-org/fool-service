package org.fool.framework.common.data;

public interface IController {
    Object get(Object id);

    Object[] getList(int page, int count);

    void create(Object object);

    void update(Object object);

    void delete(Object object);
}
