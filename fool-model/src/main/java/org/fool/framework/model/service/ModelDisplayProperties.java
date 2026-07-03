package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;

import java.util.Objects;

public final class ModelDisplayProperties {
    private ModelDisplayProperties() {
    }

    public static Property displayProperty(Model model) {
        if (model == null) {
            return null;
        }
        if (model.getShowProperty() != null && hasName(model.getShowProperty())) {
            return model.getShowProperty();
        }
        if (model.getProperties() == null) {
            return model.getIdProperty();
        }
        return model.getProperties().stream()
                .filter(property -> property != null && !sameProperty(property, model.getIdProperty()))
                .filter(property -> PropertyType.String.equals(property.getPropertyType()))
                .findFirst()
                .orElse(model.getIdProperty());
    }

    public static boolean sameProperty(Property left, Property right) {
        if (left == null || right == null) {
            return false;
        }
        return (left.getName() != null && Objects.equals(left.getName(), right.getName()))
                || (left.getId() != null && Objects.equals(left.getId(), right.getId()));
    }

    private static boolean hasName(Property property) {
        return property.getName() != null && !property.getName().isBlank();
    }
}
