package com.supalle.autotrim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassMetadata {
    private ClassMetadata parent;

    private String name;

    private boolean innerClass;

    private int anonymousInnerClassCounter;

    private boolean autoTrim;

    private boolean autoTrimIgnored;

    private Set<String> subClasses;
    private Map<String, FieldMetadata> fieldMapping;
    private boolean anonymousInnerClass;

    private Map<String, Boolean> typeMatchResultCache;

    public ClassMetadata(ClassMetadata parent, String name) {
        this(parent, name, false, false, new HashMap<>());
    }

    public ClassMetadata(ClassMetadata parent, String name, boolean autoTrim, boolean autoTrimIgnored, Map<String, FieldMetadata> fieldMapping) {
        this.parent = parent;
        this.name = name;
        this.anonymousInnerClassCounter = 0;
        this.autoTrim = autoTrim;
        this.autoTrimIgnored = autoTrimIgnored;
        this.fieldMapping = fieldMapping;
    }

    public ClassMetadata getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public void setParent(ClassMetadata parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInnerClass() {
        return innerClass;
    }

    public void setInnerClass(boolean innerClass) {
        this.innerClass = innerClass;
    }

    public int nextAnonymousInnerClassCounter() {
        return ++anonymousInnerClassCounter;
    }

    public boolean isAutoTrim() {
        return autoTrim;
    }

    public boolean isAutoTrimIgnored() {
        return autoTrimIgnored;
    }

    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    public void setAutoTrimIgnored(boolean autoTrimIgnored) {
        this.autoTrimIgnored = autoTrimIgnored;
    }

    public Map<String, FieldMetadata> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(Map<String, FieldMetadata> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public FieldMetadata getFieldMetadata(String fieldName) {
        if (fieldMapping == null) {
            return null;
        }
        return fieldMapping.get(fieldName);
    }

    public FieldMetadata addFieldMetadata(FieldMetadata fieldMetadata) {
        return fieldMapping.put(fieldMetadata.getName(), fieldMetadata);
    }

    public Set<String> getSubClasses() {
        return subClasses;
    }

    public void setSubClasses(Set<String> subClasses) {
        this.subClasses = subClasses;
    }

    public boolean hasSubClass(String subClassName, boolean includeParent) {
        Set<String> subClasses = this.subClasses;
        if (subClasses == null || !subClasses.contains(subClassName)) {
            return includeParent && getParent() != null && getParent().hasSubClass(subClassName, true);
        }
        return true;
    }

    public void addSubClasses(String subClassName) {
        if (this.subClasses == null) {
            this.subClasses = new HashSet<>();
        }
        this.subClasses.add(subClassName);
    }

    public void setAnonymousInnerClass(boolean anonymousInnerClass) {
        this.anonymousInnerClass = anonymousInnerClass;
    }

    public boolean isAnonymousInnerClass() {
        return anonymousInnerClass;
    }


    public Boolean getTypeMatchResult(String typeName, String keyword) {
        if (typeMatchResultCache == null) {
            return null;
        }
        return typeMatchResultCache.get(typeName + "::" + keyword);
    }

    public Boolean cacheTypeMatchResult(String typeName, String keyword, boolean result) {
        if (typeMatchResultCache == null) {
            typeMatchResultCache = new HashMap<>();
        }
        typeMatchResultCache.put(typeName + "::" + keyword, result);
        return result;
    }
}
