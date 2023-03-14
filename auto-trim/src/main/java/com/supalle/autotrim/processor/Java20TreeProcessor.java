package com.supalle.autotrim.processor;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java20TreeProcessor extends Java19TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public Java20TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================


    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, AutoTrimContext context) {
        JCTree.JCEnhancedForLoop t = (JCTree.JCEnhancedForLoop) node;
        context.newVarStack();
        // t.var = process(t.var, context);
        t.varOrRecordPattern = process(t.varOrRecordPattern, context);
        t.expr = process(t.expr, context);
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        context.popVarStack();
        return t;
    }

    // ==================== visit ======================

}
