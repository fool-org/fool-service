package org.fool.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Display {
    String displayName() default "";

    boolean display() default false;

    int displayIndex() default 0;

    boolean editable() default false;

    boolean generationDropdownList() default false;

    boolean showInList() default true;
}
