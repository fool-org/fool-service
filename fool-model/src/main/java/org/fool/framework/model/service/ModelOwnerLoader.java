package org.fool.framework.model.service;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Component
class ModelOwnerLoader {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    void attachOwner(Model model, IDynamicData data) {
        if (!(data instanceof DbMysqlDynamic dynamicData) || model == null || model.getOwner() == null) {
            return;
        }
        Property ownerCollection = ownerCollection(model.getOwner(), model);
        RelationColumns relation = relation(ownerCollection);
        String childId = dynamicId(data);
        String childColumn = idColumn(model);
        if (relation == null || !relation.isComplete()
                || !StringUtils.hasText(childId) || !StringUtils.hasText(childColumn)) {
            return;
        }
        List<String> ownerIds = jdbcTemplate.query(
                "SELECT `" + relation.targetColumn() + "` FROM `" + relation.table() + "` WHERE `"
                        + childColumn + "` = ? LIMIT 1",
                (resultSet, row) -> resultSet.getString(1),
                childId);
        if (ownerIds.isEmpty() || !StringUtils.hasText(ownerIds.get(0))) {
            return;
        }
        Model ownerModel = model.getOwner();
        dynamicData.setOwner(loadOwner(ownerModel, ownerIds.get(0)));
    }

    private Property ownerCollection(Model ownerModel, Model childModel) {
        if (ownerModel.getProperties() == null) {
            return null;
        }
        return ownerModel.getProperties().stream()
                .filter(property -> property != null && Boolean.TRUE.equals(property.getIsCollection()))
                .filter(property -> sameModel(property.getPropertyModel(), childModel))
                .findFirst()
                .orElse(null);
    }

    private RelationColumns relation(Property property) {
        if (property == null || property.getId() == null) {
            return null;
        }
        return jdbcTemplate.query(
                "SELECT `SW_SYS_RELATION_TABLE`,`SW_SYS_RELATION_TARGETCOL` "
                        + "FROM `SW_SYS_RELATION` WHERE `SW_SYS_RELATION_SOURCEPROPERTY` = ? LIMIT 1",
                resultSet -> resultSet.next()
                        ? new RelationColumns(resultSet.getString(1), resultSet.getString(2))
                        : null,
                property.getId());
    }

    private IDynamicData loadOwner(Model model, String ownerId) {
        if (model == null || !StringUtils.hasText(model.getTableName())) {
            return null;
        }
        List<IDynamicData> owners = jdbcTemplate.query(
                "SELECT * FROM `" + model.getTableName() + "` WHERE `" + idColumn(model) + "` = ? LIMIT 1",
                new Mapper(model),
                ownerId);
        return owners.isEmpty() ? null : owners.get(0);
    }

    private String idColumn(Model model) {
        Property idProperty = model.getIdProperty();
        return idProperty == null || !StringUtils.hasText(idProperty.getColumn())
                ? "SYSID"
                : idProperty.getColumn();
    }

    private boolean sameModel(Model left, Model right) {
        return left != null && right != null
                && ((left.getId() != null && Objects.equals(left.getId(), right.getId()))
                || (left.getName() != null && Objects.equals(left.getName(), right.getName())));
    }

    private String dynamicId(IDynamicData data) {
        String id = data.getId();
        Object value = id == null ? data.get("SYSID") : id;
        return value == null ? null : value.toString();
    }

    private record RelationColumns(String table, String targetColumn) {
        private boolean isComplete() {
            return StringUtils.hasText(table)
                    && StringUtils.hasText(targetColumn);
        }
    }
}
