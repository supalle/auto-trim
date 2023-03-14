package com.supalle.autotrim.processor;

import com.sun.source.tree.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;
import com.supalle.autotrim.FieldHandle;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java19TreeProcessor extends Java17TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_19;
    }

    public Java19TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================


    @Override
    public JCTree visitConstantCaseLabel(ConstantCaseLabelTree node, AutoTrimContext context) {
        JCTree.JCConstantCaseLabel t = (JCTree.JCConstantCaseLabel) node;
        t.expr = process(t.expr, context);
        return t;
    }

    @Override
    public JCTree visitDeconstructionPattern(DeconstructionPatternTree node, AutoTrimContext context) {
        JCTree.JCRecordPattern t = (JCTree.JCRecordPattern) node;
        context.newVarStack();
        t.deconstructor = process(t.deconstructor, context);
        t.nested = process(t.nested, context);
        context.popVarStack();
        return t;
    }

    @Override
    public JCTree visitPatternCaseLabel(PatternCaseLabelTree node, AutoTrimContext context) {
        JCTree.JCPatternCaseLabel t = (JCTree.JCPatternCaseLabel) node;
        t.pat = process(t.pat, context);
        t.guard = process(t.guard, context);
        return t;
    }

    // ==================== visit ======================

}
