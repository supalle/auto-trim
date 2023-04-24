package com.supalle.autotrim;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VarStack {

    private final VarStack parent;

    private final Map<String, StackVar> vars = new HashMap<>();

    public VarStack() {
        this(null);
    }

    public VarStack(VarStack parent) {
        this.parent = parent;
    }

    public VarStack getParent() {
        return this.parent;
    }

    public void addStackVar(final StackVar stackVar) {
        this.vars.put(stackVar.getName(), stackVar);
    }

    public StackVar getStackVar(final String varName) {
        final StackVar stackVar = this.vars.get(varName);
        if (stackVar == null && parent != null) {
            return parent.getStackVar(varName);
        }
        return stackVar;
    }

    public static class StackVar {
        private JCTree variableDeclType;
        private final String name;
        private final ExpressionTree replaceExpression;
        private boolean autoTrim;
        private boolean needFinal;
        private List<JCTree.JCParens> references;

        public StackVar(String name) {
            this(name, null);
        }

        public StackVar(String name, ExpressionTree replaceExpression) {
            this.name = name;
            this.replaceExpression = replaceExpression;
        }

        public StackVar(String name, JCTree variableDeclType, boolean autoTrim, boolean needFinal) {
            this.name = name;
            this.variableDeclType = variableDeclType;
            this.replaceExpression = null;
            this.autoTrim = autoTrim;
            this.needFinal = needFinal;
        }

        public String getName() {
            return name;
        }

        public JCTree getVariableDeclType() {
            return variableDeclType;
        }

        public void setVariableDeclType(JCTree variableDeclType) {
            this.variableDeclType = variableDeclType;
        }

        public ExpressionTree getReplaceExpression() {
            return replaceExpression;
        }

        public boolean hasReplaceExpression() {
            return replaceExpression != null;
        }

        public boolean isAutoTrim() {
            return autoTrim;
        }

        public void setAutoTrim(boolean autoTrim) {
            this.autoTrim = autoTrim;
        }

        public boolean isNeedFinal() {
            return needFinal;
        }

        public void setNeedFinal(boolean needFinal) {
            this.needFinal = needFinal;
        }

        public List<JCTree.JCParens> getReferences() {
            return references;
        }

        public void setReferences(List<JCTree.JCParens> references) {
            this.references = references;
        }

        public void addReference(JCTree.JCParens reference) {
            if (this.references == null) {
                this.references = new ArrayList<>();
            }
            this.references.add(reference);
        }
    }

}
