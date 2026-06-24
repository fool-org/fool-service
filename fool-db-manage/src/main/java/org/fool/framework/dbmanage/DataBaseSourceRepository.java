package org.fool.framework.dbmanage;

@FunctionalInterface
public interface DataBaseSourceRepository {
    DataBaseSource findByKey(String key);
}
