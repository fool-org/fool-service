package org.fool.framework.view;

import org.fool.framework.common.dynamic.IDynamicData;

public final class LegacyDynamicIds {
    private LegacyDynamicIds() {
    }

    public static String id(IDynamicData data) {
        if (data == null) {
            return null;
        }
        String id = data.getId();
        Object sysid = id == null ? data.get("SYSID") : id;
        return sysid == null ? null : sysid.toString();
    }
}
