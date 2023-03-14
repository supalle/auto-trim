package com.supalle.autotrim.processor;

import com.sun.source.tree.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java17TreeProcessor extends Java14TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_17;
    }

    public Java17TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================
    public JCTree visitCase(CaseTree node, AutoTrimContext context) {
        JCTree.JCCase t = (JCTree.JCCase) node;
        context.newVarStack();
        // t.pat = process(t.pat, context);
        t.labels = process(t.labels, context);
        t.stats = process(t.stats, context);
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }


    @Override
    public JCTree visitParenthesizedPattern(ParenthesizedPatternTree node, AutoTrimContext context) {
        JCTree.JCParenthesizedPattern t = (JCTree.JCParenthesizedPattern) node;
        t.pattern = process(t.pattern, context);
        return t;
    }


    // ==================== visit ======================

}
