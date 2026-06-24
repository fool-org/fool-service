package org.fool.framework.app;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootstrapRole {
    private final String roleName;
    private final String authorizedUserId;
    private final List<BootstrapMenuItem> items = new ArrayList<>();
}
