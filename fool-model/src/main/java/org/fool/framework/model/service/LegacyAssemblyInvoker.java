package org.fool.framework.model.service;

import org.fool.framework.common.dynamic.IDynamicData;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public final class LegacyAssemblyInvoker {
    private LegacyAssemblyInvoker() {
    }

    public static void invoke(
            String invokeClass,
            String invokeMethod,
            IDynamicData data,
            List<Object> constructorValues,
            List<Object> params) {
        if (!StringUtils.hasText(invokeClass) || !StringUtils.hasText(invokeMethod)) {
            throw new IllegalStateException("Missing assembly invoke target");
        }
        try {
            Class<?> type = Class.forName(invokeClass.trim());
            // ponytail: invokeDll plugin loading waits until real migrated handlers need it.
            Object target = instantiate(type, constructorValues == null ? List.of() : constructorValues);
            Object[] args = new Object[(params == null ? 0 : params.size()) + 1];
            args[0] = data;
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    args[i + 1] = params.get(i);
                }
            }
            Method method = method(type, invokeMethod.trim(), args);
            method.invoke(Modifier.isStatic(method.getModifiers()) ? null : target, args);
        } catch (InvocationTargetException e) {
            rethrowInvocation(e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object instantiate(Class<?> type, List<Object> args) throws ReflectiveOperationException {
        Constructor<?> constructor = Arrays.stream(type.getConstructors())
                .filter(candidate -> accepts(candidate.getParameterTypes(), args.toArray()))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        return constructor.newInstance(args.toArray());
    }

    private static Method method(Class<?> type, String name, Object[] args) throws NoSuchMethodException {
        return Arrays.stream(type.getMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .filter(candidate -> accepts(candidate.getParameterTypes(), args))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
    }

    private static boolean accepts(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] != null && !boxed(parameterTypes[i]).isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        return Integer.class;
    }

    private static void rethrowInvocation(InvocationTargetException e) {
        if (e.getCause() instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(e.getCause());
    }
}
