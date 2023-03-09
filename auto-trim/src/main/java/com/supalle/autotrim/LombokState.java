package com.supalle.autotrim;

import java.lang.reflect.Field;

public class LombokState {
    private static Field lombokInvoked;

    public static boolean isLombokInvoked() {
        if (System.getProperty("lombok.disable") != null) {
            return true;
        }
        if (lombokInvoked != null) {
            try {
                return lombokInvoked.getBoolean(null);
            } catch (Exception ignored) {
            }
            return true;
        }

        try {
            Class<?> data = Class.forName("lombok.launch.AnnotationProcessorHider$AstModificationNotifierData");
            lombokInvoked = data.getField("lombokInvoked");
            return lombokInvoked.getBoolean(null);
        } catch (Exception ignored) {
        }
        return true;
    }
}
