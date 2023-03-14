package com.supalle.autotrim.processor;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;
import com.supalle.autotrim.FieldHandle;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java12TreeProcessor extends Java8TreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_12;
    }

    public Java12TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================

    private final FieldHandle<JCTree.JCExpression> casePats = FieldHandle.of(JCTree.JCCase.class, "pats");

    public JCTree visitCase(CaseTree node, AutoTrimContext context) {
        JCTree.JCCase t = (JCTree.JCCase) node;
        context.newVarStack();
        // t.pats = process(t.pats, context);
        casePats.set(t, process(casePats.get(t), context));
        t.stats = process(t.stats, context);
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    @Override
    public JCTree visitSwitchExpression(SwitchExpressionTree node, AutoTrimContext context) {
        JCTree.JCSwitchExpression t = (JCTree.JCSwitchExpression) node;
        t.selector = process(t.selector, context);
        t.cases = process(t.cases, context);
        return t;
    }


    // ==================== visit ======================

}
