package com.supalle.autotrim.processor;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;
import com.supalle.autotrim.FieldHandle;

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


    // 为了兼容JDK22注释掉了，使用下边的 defaultAction 实现
    // @Deprecated
    // @Override
    // public JCTree visitParenthesizedPattern(ParenthesizedPatternTree node, AutoTrimContext context) {
    //     JCTree.JCParenthesizedPattern t = (JCTree.JCParenthesizedPattern) node;
    //     t.pattern = process(t.pattern, context);
    //     return t;
    // }

    private final FieldHandle<JCTree.JCExpression> pat = FieldHandle.of("com.sun.tools.javac.tree.JCTree.JCParenthesizedPattern", "pattern");

    @Override
    protected JCTree defaultAction(Tree node, AutoTrimContext context) {
        if (node != null && "com.sun.tools.javac.tree.JCTree.JCParenthesizedPattern".equals(node.getClass().getName())) {
            pat.set(node, process(pat.get(node), context));
            return (JCTree) node;
        }

        return super.defaultAction(node, context);
    }

    // ==================== visit ======================

}
