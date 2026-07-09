package org.fool.framework.app;

import org.fool.framework.app.reference.shared.ReferenceCustomer;
import org.fool.framework.model.model.Model;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ReflectiveAppModuleSourceDependencyTest {
    @Test
    public void includesDeclaredDependencyPackagesLikeLegacyAssemblyReferences() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "PKG_WITH_DEP",
                "example.PackageModule",
                "1.0.0",
                "org.fool.framework.app.reflective",
                List.of(ReferenceCustomer.class.getPackageName()),
                Thread.currentThread().getContextClassLoader());

        List<AppModuleDefinition> modules = source.getModules();

        assertEquals(
                Arrays.asList(ReferenceCustomer.class.getPackageName(), "PKG_WITH_DEP"),
                modules.stream().map(AppModuleDefinition::getName).toList());
        assertEquals(1, modules.get(1).getDependencies().size());
        assertSame(modules.get(0), modules.get(1).getDependencies().get(0));
        assertEquals(
                List.of("ReferenceCustomer"),
                source.getModels(modules.get(0)).stream().map(Model::getName).toList());
    }
}
