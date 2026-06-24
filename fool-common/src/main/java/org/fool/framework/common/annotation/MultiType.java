package org.fool.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiType {
    String tableName() default "";

    String parentProperty() default "";

    String parentColumn() default "";

    String childrenProperty() default "";

    String childrenColumn() default "";
}
