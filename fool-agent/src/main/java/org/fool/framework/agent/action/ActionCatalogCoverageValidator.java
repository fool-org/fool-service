package org.fool.framework.agent.action;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Fails startup when a catalog action and its runtime handler drift apart. */
@Component
public class ActionCatalogCoverageValidator {
    public ActionCatalogCoverageValidator(ActionCatalog catalog, ControlledActionRegistry registry) {
        Set<String> expected = catalog.definitions().entrySet().stream()
                .filter(entry -> entry.getValue().executable())
                .map(entry -> ControlledActionRegistry.key(entry.getKey(), entry.getValue().resourceType()))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> actual = new TreeSet<>(registry.registeredKeys());
        if (!expected.equals(actual)) {
            Set<String> missing = new TreeSet<>(expected);
            missing.removeAll(actual);
            Set<String> undeclared = new TreeSet<>(actual);
            undeclared.removeAll(expected);
            throw new IllegalStateException(
                    "ACTION_CATALOG_HANDLER_DRIFT missing=" + missing + " undeclared=" + undeclared);
        }
    }
}
