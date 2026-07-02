package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.model.model.EnumValue;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetEnumResult {
    private List<Value> enumValues = new ArrayList<>();

    @Data
    public static class Value {
        private String name;
        private Integer value;

        public static Value from(EnumValue source) {
            Value result = new Value();
            result.setName(source.getName());
            result.setValue(parseValue(source.getValue()));
            return result;
        }

        private static Integer parseValue(String value) {
            try {
                return value == null ? null : Integer.valueOf(value);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
