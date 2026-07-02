package org.fool.framework.common.data;

import java.util.List;

public interface IInerfaceDBContext<I, T> {
    List<I> get();

    void save(I object);

    void delete(I object);

    void create(I object);

    I getDetail(Object key);
}
