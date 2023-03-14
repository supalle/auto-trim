package com.supalle.autotrim.processor;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.YieldTree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java14TreeProcessor extends Java12TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_14;
    }

    public Java14TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================

    @Override
    public JCTree visitBindingPattern(BindingPatternTree node, AutoTrimContext context) {
        JCTree.JCBindingPattern t = (JCTree.JCBindingPattern) node;
        // t.var = process(t.var, context);
        // 不做处理
        return defaultAction(node, context);
    }

    @Override
    public JCTree visitYield(YieldTree node, AutoTrimContext context) {
        JCTree.JCYield t = (JCTree.JCYield) node;
        t.value = process(t.value, context);
        return t;
    }

    // ==================== visit ======================

}
