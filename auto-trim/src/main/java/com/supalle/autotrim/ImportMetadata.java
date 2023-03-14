package com.supalle.autotrim;

import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Name;

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
        // 强转 ImportTree 是因为JCImport的JCFieldAccess getQualifiedIdentifier() 方法
        // 在某些版本是JCTree getQualifiedIdentifier()，返回值不一致，方法全限定名也不一致
        Tree qualifiedIdentifier = ((ImportTree) jcImport).getQualifiedIdentifier();
        Name fullName = TreeInfo.fullName((JCTree) qualifiedIdentifier);
        this.importMap.put(fullName.toString(), jcImport);
    }
}
