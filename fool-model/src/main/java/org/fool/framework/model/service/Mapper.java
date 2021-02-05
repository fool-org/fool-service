package org.fool.framework.model.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.AbstratMapper;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;

import java.sql.ResultSet;
import java.util.stream.Collectors;


/**
 * 动态数据 的映射
 */
@Slf4j
public class Mapper extends AbstratMapper<IDynamicData> {

    private final Model model;

    /**
     * 公共类型
     *
     * @param model
     */
    public Mapper(Model model) {
        this.model = model;
    }

    Model getModel() {
        return this.model;
    }

    /**
     * 暂时只处理简单类型
     *
     * @param resultSet
     * @param row
     * @return
     */
    @Override
    public IDynamicData mapRow(ResultSet resultSet, int row) {
        try {

            DbMysqlDynamic mysqlDynamic = new DbMysqlDynamic(this.model);
            for (var property : this.model.getProperties().stream().filter(p -> p.getIsCollection() == false).collect(Collectors.toList())) {
                mysqlDynamic.set(property.getName(), resultSet.getObject(property.getColumn()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
