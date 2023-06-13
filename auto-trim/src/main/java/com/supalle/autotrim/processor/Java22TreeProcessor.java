package com.supalle.autotrim.processor;

import com.sun.source.tree.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.DefinedBy;
import com.supalle.autotrim.AutoTrimContext;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java22TreeProcessor extends Java19TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public Java22TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================

    @DefinedBy(DefinedBy.Api.COMPILER_TREE)
    public JCTree visitCase(CaseTree node, AutoTrimContext context) {
        JCTree.JCCase t = (JCTree.JCCase) node;
        context.newVarStack();
        t.labels = process(t.labels, context);
        t.guard = process(t.guard, context);
        t.stats = process(t.stats, context);
        t.body = process(t.body, context);
        // if (node.getCaseKind() == CaseTree.CaseKind.RULE) {
        //     t.body = t.body instanceof JCTree.JCExpression && t.stats.head.hasTag(JCTree.Tag.YIELD)
        //             ? ((JCTree.JCYield) t.stats.head).value : t.stats.head;
        // } else {
        //     t.body = null;
        // }
        context.popVarStack();
        return t;
    }

    @DefinedBy(DefinedBy.Api.COMPILER_TREE)
    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, AutoTrimContext context) {
        JCTree.JCEnhancedForLoop t = (JCTree.JCEnhancedForLoop) node;
        context.newVarStack();
        t.var = process(t.var, context);
        t.expr = process(t.expr, context);
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        context.popVarStack();
        return t;
    }

    @DefinedBy(DefinedBy.Api.COMPILER_TREE)
    public JCTree visitStringTemplate(StringTemplateTree node, AutoTrimContext context) {
        JCTree.JCStringTemplate t = (JCTree.JCStringTemplate) node;
        t.processor = process(t.processor, context);
        // t.fragments = t.fragments;
        t.expressions = process(t.expressions, context);
        return t;
    }

    // @DefinedBy(DefinedBy.Api.COMPILER_TREE)
    // public JCTree visitAnyPattern(AnyPatternTree node, AutoTrimContext context) {
    //     JCTree.JCAnyPattern t = (JCTree.JCAnyPattern) node;
    //     return t;
    // }

    @Override
    public JCTree visitPatternCaseLabel(PatternCaseLabelTree node, AutoTrimContext context) {
        JCTree.JCPatternCaseLabel t = (JCTree.JCPatternCaseLabel) node;
        t.pat = process(t.pat, context);
        t.syntheticGuard = process(t.syntheticGuard, context);
        return t;
    }

    // 去掉 visitParenthesizedPattern

    // ==================== visit ======================

}
