package org.fool.framework.view.service;

import org.fool.framework.dao.Mapper;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dao.SqlScriptGenerator;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModelDaoMappingTest {

    @Test
    public void modelRelationsAreNotLoadedByGenericCollectionMapping() {
        Mapper<Model> mapper = new Mapper<>(Model.class);

        assertFalse(mapper.getMapFields().stream()
                .anyMatch(field -> field.getField().getName().equals("relations")));
        assertFalse(mapper.getMapFields().stream()
                .anyMatch(field -> field.getField().getName().equals("baseModel")));
    }

    @Test
    public void modelEnumValuesAreLoadedByGenericDetailMapping() {
        Mapper<Model> modelMapper = new Mapper<>(Model.class);

        assertTrue(modelMapper.getMapFields().stream()
                .anyMatch(field -> field.getField().getName().equals("enumValues")
                        && field.isCollection()
                        && "owner".equals(field.getColumnName())));

        Mapper<EnumValue> enumValueMapper = new Mapper<>(EnumValue.class);
        assertTrue(enumValueMapper.getMapFields().stream()
                .anyMatch(field -> field.getField().getName().equals("owner")
                        && "owner".equals(field.getColumnName())));

        QueryAndArgs query = new SqlScriptGenerator().generateSelectItems(enumValueMapper, "owner", 42L);

        assertEquals(
                "SELECT `name`,`value`,`remark`,`owner` FROM `fool_sys_model_enum` WHERE 1=1  AND `owner`=?",
                query.getSql());
        assertEquals(42L, query.getArgs()[0]);
    }

    @Test
    public void viewItemMapsLegacySourceExpressionMetadata() {
        Mapper<ViewItem> mapper = new Mapper<>(ViewItem.class);

        assertTrue(mapper.getMapFields().stream()
                .anyMatch(field -> field.getField().getName().equals("sourceExpression")
                        && "source_expression".equals(field.getColumnName())));
    }
}
