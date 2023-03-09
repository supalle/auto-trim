package com.supalle.autotrim;


public class FieldMetadata {
    private String name;
    private boolean autoTrim;
    private boolean autoTrimIgnored;

    public FieldMetadata(String name, boolean autoTrim, boolean autoTrimIgnored) {
        this.name = name;
        this.autoTrim = autoTrim;
        this.autoTrimIgnored = autoTrimIgnored;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoTrim() {
        return autoTrim;
    }

    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    public boolean isAutoTrimIgnored() {
        return autoTrimIgnored;
    }

    public void setAutoTrimIgnored(boolean autoTrimIgnored) {
        this.autoTrimIgnored = autoTrimIgnored;
    }
}
