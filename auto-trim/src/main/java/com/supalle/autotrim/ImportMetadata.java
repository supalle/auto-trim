package com.supalle.autotrim;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import java.util.HashMap;
import java.util.Map;

public class ImportMetadata {

    private String packageName;
    private Map<String, JCTree.JCImport> importMap = new HashMap<>();

    public Map<String, JCTree.JCImport> getImportMap() {
        return importMap;
    }

    public void setImportMap(Map<String, JCTree.JCImport> importMap) {
        this.importMap = importMap;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void addImport(JCTree.JCImport jcImport) {
        if (jcImport == null) {
            return;
        }
        this.importMap.put(TreeInfo.fullName(jcImport.getQualifiedIdentifier()).toString(), jcImport);
    }
}
