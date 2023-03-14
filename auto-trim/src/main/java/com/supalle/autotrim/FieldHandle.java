package com.supalle.autotrim;

import java.lang.reflect.Field;
import java.util.Objects;

public class FieldHandle<T> {
    private final Class<?> clazz;
    private final String fieldName;
    private Field field;
    private boolean initialized;
    private boolean available;

    public FieldHandle(Field field) {
        Objects.requireNonNull(field, "field must be not null.");
        this.field = field;
        this.clazz = this.field.getDeclaringClass();
        this.fieldName = this.field.getName();
        this.initialized = true;
        this.available = true;
    }

    public FieldHandle(Class<?> clazz, String fieldName) {
        this.clazz = clazz;
        this.fieldName = fieldName;
    }

    public static <T> FieldHandle<T> of(Class<?> clazz, String fieldName) {
        return new FieldHandle<>(clazz, fieldName);
    }

    public static <T> FieldHandle<T> of(String clazzName, String fieldName) {
        return new FieldHandle<>(classForName(clazzName), fieldName);
    }

    private static Class<?> classForName(String clazzName) {
        try {
            return Class.forName(clazzName);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Field getField() {
        if (!available && this.field == null) {
            try {
                Field declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                this.field = declaredField;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return this.field;
    }

    private boolean init() {
        if (!initialized) {
            try {
                Field declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                this.field = declaredField;
                this.available = true;
            } catch (NoSuchFieldException ignored) {
                this.available = false;
            }
            initialized = true;
        }
        return this.available;
    }

    public T get(Object instance) {
        if (!available || !init()) {
            return null;
        }
        try {
            return (T) getField().get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Object instance, T newValue) {
        if (!available || !init()) {
            return;
        }
        try {
            getField().set(instance, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
