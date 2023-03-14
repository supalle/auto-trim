package com.supalle.autotrim.processor;

import com.sun.source.tree.*;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;
import com.supalle.autotrim.ClassMetadata;
import com.supalle.autotrim.FieldHandle;
import com.supalle.autotrim.VarStack;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Java8TreeProcessor extends AbstractTreeProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    public Java8TreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        super(m, elementUtils, processedSymbols);
    }

    // ==================== visit ======================

    private final FieldHandle<JCTree.JCExpression> casePat = FieldHandle.of(JCTree.JCCase.class, "pat");

    public JCTree visitCase(CaseTree node, AutoTrimContext context) {
        JCTree.JCCase t = (JCTree.JCCase) node;
        context.newVarStack();
        // t.pat = process(t.pat, context);
        casePat.set(t, process(casePat.get(t), context));
        t.stats = process(t.stats, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitAssert(AssertTree node, AutoTrimContext context) {
        JCTree.JCAssert t = (JCTree.JCAssert) node;
        t.cond = process(t.cond, context);
        t.detail = process(t.detail, context);
        return t;
    }

    public JCTree visitAssignment(AssignmentTree node, AutoTrimContext context) {
        JCTree.JCAssign t = (JCTree.JCAssign) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitCompoundAssignment(CompoundAssignmentTree node, AutoTrimContext context) {
        JCTree.JCAssignOp t = (JCTree.JCAssignOp) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitBinary(BinaryTree node, AutoTrimContext context) {
        JCTree.JCBinary t = (JCTree.JCBinary) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitCatch(CatchTree node, AutoTrimContext context) {
        JCTree.JCCatch t = (JCTree.JCCatch) node;
        context.newVarStack();
        t.param = process(t.param, context);
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitConditionalExpression(ConditionalExpressionTree node, AutoTrimContext context) {
        JCTree.JCConditional t = (JCTree.JCConditional) node;
        t.cond = process(t.cond, context);
        t.truepart = process(t.truepart, context);
        t.falsepart = process(t.falsepart, context);
        return t;
    }

    public JCTree visitDoWhileLoop(DoWhileLoopTree node, AutoTrimContext context) {
        JCTree.JCDoWhileLoop t = (JCTree.JCDoWhileLoop) node;
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        t.cond = process(t.cond, context);
        return t;
    }

    public JCTree visitErroneous(ErroneousTree node, AutoTrimContext context) {
        JCTree.JCErroneous t = (JCTree.JCErroneous) node;
        t.errs = process(t.errs, context);
        return t;
    }

    public JCTree visitExpressionStatement(ExpressionStatementTree node, AutoTrimContext context) {
        JCTree.JCExpressionStatement t = (JCTree.JCExpressionStatement) node;
        t.expr = process(t.expr, context);
        return t;
    }

    private final FieldHandle<JCTree.JCVariableDecl> enhancedForLoopVar = FieldHandle.of(JCTree.JCEnhancedForLoop.class, "var");


    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, AutoTrimContext context) {
        JCTree.JCEnhancedForLoop t = (JCTree.JCEnhancedForLoop) node;
        context.newVarStack();
        // t.var = process(t.var, context);
        enhancedForLoopVar.set(t, process(enhancedForLoopVar.get(t), context));
        t.expr = process(t.expr, context);
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        context.popVarStack();
        return t;
    }

    public JCTree visitForLoop(ForLoopTree node, AutoTrimContext context) {
        JCTree.JCForLoop t = (JCTree.JCForLoop) node;
        context.newVarStack();
        t.init = process(t.init, context);
        t.cond = process(t.cond, context);
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        t.step = process(t.step, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitIf(IfTree node, AutoTrimContext context) {
        JCTree.JCIf t = (JCTree.JCIf) node;
        t.cond = process(t.cond, context);
        t.thenpart = process(t.thenpart, context);
        t.elsepart = process(t.elsepart, context);
        return t;
    }

    public JCTree visitArrayAccess(ArrayAccessTree node, AutoTrimContext context) {
        JCTree.JCArrayAccess t = (JCTree.JCArrayAccess) node;
        t.indexed = process(t.indexed, context);
        t.index = process(t.index, context);
        return t;
    }

    public JCTree visitLabeledStatement(LabeledStatementTree node, AutoTrimContext context) {
        JCTree.JCLabeledStatement t = (JCTree.JCLabeledStatement) node;
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitMethodInvocation(MethodInvocationTree node, AutoTrimContext context) {
        JCTree.JCMethodInvocation t = (JCTree.JCMethodInvocation) node;
        t.meth = process(t.meth, context);
        t.args = process(t.args, context);
        return t;
    }

    public JCTree visitNewArray(NewArrayTree node, AutoTrimContext context) {
        JCTree.JCNewArray t = (JCTree.JCNewArray) node;
        t.elemtype = process(t.elemtype, context);
        t.dims = process(t.dims, context);
        t.elems = process(t.elems, context);
        return t;
    }

    public JCTree visitNewClass(NewClassTree node, AutoTrimContext context) {
        JCTree.JCNewClass t = (JCTree.JCNewClass) node;
        context.newVarStack();
        t.encl = process(t.encl, context);
        // List<JCExpression> typeargs = process(t.typeargs, context);
        t.clazz = process(t.clazz, context);
        t.args = process(t.args, context);
        t.def = process(t.def, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitLambdaExpression(LambdaExpressionTree node, AutoTrimContext context) {
        JCTree.JCLambda t = (JCTree.JCLambda) node;
        final ClassMetadata classMetadata = context.getClassMetadata();
        int number = classMetadata.nextAnonymousInnerClassCounter();
        String className = classMetadata.getName() + "$InnerClass" + number;
        context.newClassMetadata(classMetadata, className).setInnerClass(true);
        context.newVarStack();
        t.params = process(t.params, context);
        t.body = process(t.body, context);
        context.popVarStack();
        context.popClassMetadata();
        return t;
    }

    public JCTree visitParenthesized(ParenthesizedTree node, AutoTrimContext context) {
        JCTree.JCParens t = (JCTree.JCParens) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitReturn(ReturnTree node, AutoTrimContext context) {
        JCTree.JCReturn t = (JCTree.JCReturn) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitMemberSelect(MemberSelectTree node, AutoTrimContext context) {
        JCTree.JCFieldAccess t = (JCTree.JCFieldAccess) node;
        t.selected = process(t.selected, context);
        return t;
    }

    public JCTree visitMemberReference(MemberReferenceTree node, AutoTrimContext context) {
        JCTree.JCMemberReference t = (JCTree.JCMemberReference) node;
        context.newVarStack();
        t.expr = process(t.expr, context);
        t.typeargs = process(t.typeargs, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitSwitch(SwitchTree node, AutoTrimContext context) {
        JCTree.JCSwitch t = (JCTree.JCSwitch) node;
        context.newVarStack();
        t.selector = process(t.selector, context);
        t.cases = process(t.cases, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitSynchronized(SynchronizedTree node, AutoTrimContext context) {
        JCTree.JCSynchronized t = (JCTree.JCSynchronized) node;
        context.newVarStack();
        t.lock = process(t.lock, context);
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitThrow(ThrowTree node, AutoTrimContext context) {
        JCTree.JCThrow t = (JCTree.JCThrow) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitTry(TryTree node, AutoTrimContext context) {
        JCTree.JCTry t = (JCTree.JCTry) node;
        context.newVarStack();
        t.resources = process(t.resources, context);
        t.body = process(t.body, context);
        context.popVarStack();

        context.newVarStack();
        t.catchers = process(t.catchers, context);
        context.popVarStack();

        context.newVarStack();
        t.finalizer = process(t.finalizer, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitUnionType(UnionTypeTree node, AutoTrimContext context) {
        JCTree.JCTypeUnion t = (JCTree.JCTypeUnion) node;
        t.alternatives = process(t.alternatives, context);
        return t;
    }

    public JCTree visitIntersectionType(IntersectionTypeTree node, AutoTrimContext context) {
        JCTree.JCTypeIntersection t = (JCTree.JCTypeIntersection) node;
        t.bounds = process(t.bounds, context);
        return t;
    }

    public JCTree visitArrayType(ArrayTypeTree node, AutoTrimContext context) {
        JCTree.JCArrayTypeTree t = (JCTree.JCArrayTypeTree) node;
        t.elemtype = process(t.elemtype, context);
        return t;
    }

    public JCTree visitTypeCast(TypeCastTree node, AutoTrimContext context) {
        JCTree.JCTypeCast t = (JCTree.JCTypeCast) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitInstanceOf(InstanceOfTree node, AutoTrimContext context) {
        JCTree.JCInstanceOf t = (JCTree.JCInstanceOf) node;
        t.expr = process(t.expr, context);
        // t.pattern = process(t.pattern, context);
        return t;
    }

    public JCTree visitUnary(UnaryTree node, AutoTrimContext context) {
        JCTree.JCUnary t = (JCTree.JCUnary) node;
        t.arg = process(t.arg, context);
        return t;
    }


    public JCTree visitWhileLoop(WhileLoopTree node, AutoTrimContext context) {
        JCTree.JCWhileLoop t = (JCTree.JCWhileLoop) node;
        t.body = process(t.body, context);
        t.cond = process(t.cond, context);
        return t;
    }

    public JCTree visitWildcard(WildcardTree node, AutoTrimContext context) {
        JCTree.JCWildcard t = (JCTree.JCWildcard) node;
        // TypeBoundKind kind = M.at(t.kind.pos).TypeBoundKind(t.kind.kind);
        t.inner = process(t.inner, context);
        return t;
    }


    public JCTree visitOther(Tree node, AutoTrimContext context) {
        JCTree tree = (JCTree) node;
        switch (tree.getTag()) {
            case LETEXPR: {
                JCTree.LetExpr t = (JCTree.LetExpr) node;
                t.defs = process(t.defs, context);
                t.expr = process(t.expr, context);
                return t;
            }
            default:
                throw new AssertionError("unknown tree tag: " + tree.getTag());
        }
    }


    @Override
    public JCTree visitLiteral(LiteralTree node, AutoTrimContext context) {
        JCTree.JCLiteral t = (JCTree.JCLiteral) node;
        if (t.value instanceof JCTree) {
            t.value = process((JCTree) t.value, context);
        }
        return t;
    }
    // ==================== visit ======================

}
