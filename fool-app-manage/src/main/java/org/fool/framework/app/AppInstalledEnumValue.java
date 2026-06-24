package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.EnumValue;

@Table("SW_SYS_EMUNVALUE")
@Data
public class AppInstalledEnumValue {
    @Column("EMUN_STR")
    private String name;
    @Column("EMUN_VALUE")
    private Integer value;
    @Column("SW_SYS_MODEL_EnumValuesMODEL_ID")
    private Long ownerModelId;

    public static AppInstalledEnumValue fromEnumValue(EnumValue source, Long ownerModelId) {
        AppInstalledEnumValue value = new AppInstalledEnumValue();
        value.setName(source.getName());
        value.setValue(parseValue(source.getValue()));
        value.setOwnerModelId(ownerModelId);
        return value;
    }

    private static Integer parseValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
