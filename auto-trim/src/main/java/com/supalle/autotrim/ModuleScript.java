package com.supalle.autotrim;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ALL")
public class ModuleScript {


    public static void addOpensForAutoTrim(Class<?> clazz) {
        if (!hasModule()) {
            return;
        }

        Object targetModule = invokeSpec("java.lang.Class#getModule", clazz);
        Object moduleLayer = invokeSpec("java.lang.ModuleLayer#boot", null);
        Optional<Object> moduleOptional = invokeMethod(findMethod("java.lang.ModuleLayer#findModule", String.class), moduleLayer, "jdk.compiler");
        Object jdkModule = moduleOptional.get();

        String[] packages = {
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.code"
        };

        Method implAddOpens = findMethod("java.lang.Module#implAddOpens", false, String.class, classForName("java.lang.Module"));
        setAccessibleTrue(implAddOpens);
        for (String pkg : packages) {
            invokeMethod(implAddOpens, jdkModule, pkg, targetModule);
        }
    }


    private static boolean hasModule() {
        return classForName("java.lang.Module") != null;
    }


    private static Method findMethod(String spec, Class<?>... parameterTypes) {
        return findMethod(spec, true, parameterTypes);
    }

    private static Method findMethod(String spec, boolean setAccessibleTrue, Class<?>... parameterTypes) {
        String[] specs = spec.split("#", 2);
        try {
            String className = specs[0];
            String methodName = specs[1];
            Class<?> clazz = Objects.requireNonNull(classForName(className));
            Method method = Objects.requireNonNull(clazz.getDeclaredMethod(methodName, parameterTypes));
            if (setAccessibleTrue) {
                method.setAccessible(true);
            }
            return method;
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("all")
    private static <T> T invokeSpec(String spec, Object instance, Object... args) {
        try {
            Method method = findMethod(spec);
            return (T) method.invoke(instance, args);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("all")
    private static <T> T invokeMethod(Method method, Object instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }


    private static void setAccessibleTrue(Method implAddOpens) {
        try {
            Field theUnsafe = classForName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafeInstance = theUnsafe.get(null);

            Method objectFieldOffset = findMethod("sun.misc.Unsafe#objectFieldOffset", Field.class);
            long offset = invokeMethod(objectFieldOffset, unsafeInstance, FirstFieldOffset.class.getDeclaredField("first"));

            Method putBooleanVolatile = findMethod("sun.misc.Unsafe#putBooleanVolatile", Object.class, long.class, boolean.class);
            invokeMethod(putBooleanVolatile, unsafeInstance, implAddOpens, offset, true);

        } catch (Exception ignored) {

        }
    }

    static class FirstFieldOffset {
        boolean first;
    }
}
