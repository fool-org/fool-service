package org.fool.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Columns.class)
public @interface Column {
    String value() default "";

    String propertyName() default "";

    boolean noMap() default false;

    int preIndex() default -1;

    int preLen() default 0;

    boolean overrideParent() default false;

    boolean key() default false;

    String keyGroupName() default "";

    GenerationType generationType() default GenerationType.NEVER;

    String generationExpression() default "";

    String defaultValue() default "";

    String format() default "";

    EncryptType encryptType() default EncryptType.NONE;
}
