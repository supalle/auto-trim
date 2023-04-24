package com.supalle.autotrim;

import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.sun.tools.javac.code.Flags.FINAL;

public class AutoTrimTreeProcessor extends SimpleTreeVisitor<JCTree, AutoTrimContext> {
    private static final String AUTO_TRIM_TYPE_CANONICAL_NAME = AutoTrim.class.getCanonicalName();
    private static final String AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME = AutoTrim.Ignored.class.getCanonicalName();
    public static final String SUPER_IDENT_NAME = "super";
    public static final String INIT_METHOD_NAME = "<init>";
    private static final String SETTER_PREFIX = "set";
    public static final String TEMP_PREFIX = "_$AutoTrim$_";
    private static final String STRING_SIMPLE_NAME = String.class.getSimpleName();
    private static final String STRING_NAME = String.class.getCanonicalName();
    private static final String STRING_TRIM_METHOD_NAME = "trim";
    private final TreeMaker M;
    private final JavacElements elementUtils;
    private final ConcurrentMap<String, Boolean> processedSymbols;


    public AutoTrimTreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
        M = m;
        this.elementUtils = elementUtils;
        this.processedSymbols = processedSymbols;
    }

    public <T extends JCTree> T process(T tree, AutoTrimContext context) {
        if (tree == null) {
            return null;
        }
        return (T) (tree.accept(this, context));
    }

    public <T extends JCTree> List<T> process(List<T> trees, AutoTrimContext context) {
        if (trees == null)
            return null;
        ListBuffer<T> lb = new ListBuffer<T>();
        for (T tree : trees)
            lb.append(process(tree, context));
        return lb.toList();
    }

    public JCTree visitAnnotatedType(AnnotatedTypeTree node, AutoTrimContext context) {
        JCAnnotatedType t = (JCAnnotatedType) node;
        return t;
    }

    public JCTree visitAnnotation(AnnotationTree node, AutoTrimContext context) {
        JCAnnotation t = (JCAnnotation) node;
        return t;
    }

    public JCTree visitAssert(AssertTree node, AutoTrimContext context) {
        JCAssert t = (JCAssert) node;
        t.cond = process(t.cond, context);
        t.detail = process(t.detail, context);
        return t;
    }

    public JCTree visitAssignment(AssignmentTree node, AutoTrimContext context) {
        JCAssign t = (JCAssign) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitCompoundAssignment(CompoundAssignmentTree node, AutoTrimContext context) {
        JCAssignOp t = (JCAssignOp) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitBinary(BinaryTree node, AutoTrimContext context) {
        JCBinary t = (JCBinary) node;
        t.lhs = process(t.lhs, context);
        t.rhs = process(t.rhs, context);
        return t;
    }

    public JCTree visitBlock(BlockTree node, AutoTrimContext context) {
        JCBlock t = (JCBlock) node;
        context.newVarStack();
        t.stats = process(t.stats, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitBreak(BreakTree node, AutoTrimContext context) {
        JCBreak t = (JCBreak) node;
        return t;
    }

    public JCTree visitCase(CaseTree node, AutoTrimContext context) {
        JCCase t = (JCCase) node;
        context.newVarStack();
        // t.pat = process(t.pat, context);
        t.stats = process(t.stats, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitCatch(CatchTree node, AutoTrimContext context) {
        JCCatch t = (JCCatch) node;
        context.newVarStack();
        t.param = process(t.param, context);
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitClass(ClassTree node, AutoTrimContext context) {
        JCClassDecl jcClassDecl = (JCClassDecl) node;
        context.newVarStack();
        final ClassMetadata classMetadata = context.getClassMetadata();
        String className;
        boolean innerClass;
        boolean anonymousInnerClass = false;
        if (jcClassDecl.sym != null) {
            innerClass = jcClassDecl.sym.owner instanceof Symbol.ClassSymbol;
            className = jcClassDecl.sym.name.toString();
        } else {
            innerClass = true;
            Name simpleName = jcClassDecl.getSimpleName();
            if (simpleName == null || simpleName.length() == 0) {
                int number = classMetadata.nextAnonymousInnerClassCounter();
                className = classMetadata.getName() + "$InnerClass" + number;
                anonymousInnerClass = true;
            } else {
                className = classMetadata.getName() + '.' + simpleName;
            }
        }
        final ClassMetadata newClassMetadata = context.newClassMetadata(classMetadata, className);
        newClassMetadata.setInnerClass(innerClass);
        newClassMetadata.setAnonymousInnerClass(anonymousInnerClass);
        final List<JCTree> members = Optional.ofNullable(jcClassDecl.getMembers()).orElseGet(List::nil);
        final ArrayList<JCVariableDecl> fields = new ArrayList<>();
        final ArrayList<JCMethodDecl> methods = new ArrayList<>();
        final ArrayList<JCClassDecl> subClasses = new ArrayList<>();
        final ArrayList<JCTree> others = new ArrayList<>();
        for (JCTree member : members) {
            if (member instanceof JCVariableDecl) {
                fields.add((JCVariableDecl) member);
            } else if (member instanceof JCMethodDecl) {
                methods.add((JCMethodDecl) member);
            } else if (member instanceof JCClassDecl) {
                subClasses.add((JCClassDecl) member);
            } else {
                others.add(member);
            }
        }
        // test
        // if (!others.isEmpty()) {
        //     for (JCTree other : others) {
        //         System.out.println(other);
        //     }
        //     // Assert.error("others is mot empty.");// throw
        // }

        for (JCClassDecl subClass : subClasses) {
            Name simpleName = subClass.getSimpleName();
            if (simpleName != null) {
                String subClassName = simpleName.toString();
                if (subClassName.length() > 0) {
                    newClassMetadata.addSubClasses(subClassName);
                }
            }
        }

        for (JCAnnotation annotation : jcClassDecl.getModifiers().getAnnotations()) {
            if (matchImportType(AUTO_TRIM_TYPE_CANONICAL_NAME, annotation, context)) {
                newClassMetadata.setAutoTrim(true);
                continue;
            }
            if (matchImportType(AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME, annotation, context)) {
                newClassMetadata.setAutoTrimIgnored(true);
            }
        }

        for (JCVariableDecl variableDecl : fields) {
            boolean autoTrim = false;
            boolean autoTrimIgnored = false;
            for (JCAnnotation annotation : variableDecl.getModifiers().getAnnotations()) {
                if (matchImportType(AUTO_TRIM_TYPE_CANONICAL_NAME, annotation, context)) {
                    autoTrim = true;
                    continue;
                }
                if (matchImportType(AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME, annotation, context)) {
                    autoTrimIgnored = true;
                }
            }
            FieldMetadata fieldMetadata = new FieldMetadata(variableDecl.getName().toString().toLowerCase(Locale.ENGLISH), autoTrim, autoTrimIgnored);
            newClassMetadata.addFieldMetadata(fieldMetadata);

            variableDecl.accept(this, context);
        }
        for (JCMethodDecl method : methods) {
            process(method, context);
        }
        for (JCClassDecl subClass : subClasses) {
            process(subClass, context);
        }

        context.popClassMetadata();
        context.popVarStack();
        return jcClassDecl;
    }

    public JCTree visitConditionalExpression(ConditionalExpressionTree node, AutoTrimContext context) {
        JCConditional t = (JCConditional) node;
        t.cond = process(t.cond, context);
        t.truepart = process(t.truepart, context);
        t.falsepart = process(t.falsepart, context);
        return t;
    }

    public JCTree visitContinue(ContinueTree node, AutoTrimContext context) {
        JCContinue t = (JCContinue) node;
        return t;
    }

    public JCTree visitDoWhileLoop(DoWhileLoopTree node, AutoTrimContext context) {
        JCDoWhileLoop t = (JCDoWhileLoop) node;
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        t.cond = process(t.cond, context);
        return t;
    }

    public JCTree visitErroneous(ErroneousTree node, AutoTrimContext context) {
        JCErroneous t = (JCErroneous) node;
        t.errs = process(t.errs, context);
        return t;
    }

    public JCTree visitExpressionStatement(ExpressionStatementTree node, AutoTrimContext context) {
        JCExpressionStatement t = (JCExpressionStatement) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, AutoTrimContext context) {
        JCEnhancedForLoop t = (JCEnhancedForLoop) node;
        context.newVarStack();
        // t.var = process(t.var, context);
        t.expr = process(t.expr, context);
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        context.popVarStack();
        return t;
    }

    public JCTree visitForLoop(ForLoopTree node, AutoTrimContext context) {
        JCForLoop t = (JCForLoop) node;
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

    public JCTree visitIdentifier(IdentifierTree node, AutoTrimContext context) {
        JCIdent t = (JCIdent) node;
        VarStack varStack = context.getVarStack();
        VarStack.StackVar stackVar = varStack.getStackVar(t.getName().toString());
        if (stackVar != null && stackVar.isAutoTrim()) {
            if (context.getClassMetadata().isInnerClass()) {
                stackVar.setNeedFinal(true);
            }
            JCParens reference = M.Parens(t);
            stackVar.addReference(reference);
            return reference;
        }
        return t;
    }

    public JCTree visitIf(IfTree node, AutoTrimContext context) {
        JCIf t = (JCIf) node;
        t.cond = process(t.cond, context);
        t.thenpart = process(t.thenpart, context);
        t.elsepart = process(t.elsepart, context);
        return t;
    }

    public JCTree visitImport(ImportTree node, AutoTrimContext context) {
        JCImport t = (JCImport) node;
        return t;
    }

    public JCTree visitArrayAccess(ArrayAccessTree node, AutoTrimContext context) {
        JCArrayAccess t = (JCArrayAccess) node;
        t.indexed = process(t.indexed, context);
        t.index = process(t.index, context);
        return t;
    }

    public JCTree visitLabeledStatement(LabeledStatementTree node, AutoTrimContext context) {
        JCLabeledStatement t = (JCLabeledStatement) node;
        context.newVarStack();
        t.body = process(t.body, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitLiteral(LiteralTree node, AutoTrimContext context) {
        JCLiteral t = (JCLiteral) node;
        return t;
    }

    public JCTree visitMethod(MethodTree node, AutoTrimContext context) {
        JCMethodDecl t = (JCMethodDecl) node;
        String methodName = t.getName().toString();
        String key = context.getClassMetadata().getName() + "#" + methodName;
        if (Boolean.TRUE.equals(this.processedSymbols.get(key))) {
            return t;
        }

        if (isEmptyMethod(t)) {
            return t;
        }
        context.newVarStack();

        List<JCVariableDecl> parameters = t.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            boolean methodAutoTrim = false;
            boolean methodAutoTrimIgnored = false;
            for (JCAnnotation annotation : t.getModifiers().getAnnotations()) {
                if (matchImportType(AUTO_TRIM_TYPE_CANONICAL_NAME, annotation, context)) {
                    methodAutoTrim = true;
                    continue;
                }
                if (matchImportType(AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME, annotation, context)) {
                    methodAutoTrimIgnored = true;
                }
            }
            ClassMetadata classMetadata = context.getClassMetadata();
            boolean fieldAutoTrimIgnored = false;
            boolean fieldAutoTrim = false;

            boolean isSetterMethod = methodName.length() > SETTER_PREFIX.length() && methodName.startsWith(SETTER_PREFIX)
                    && parameters.size() == 1;
            if (isSetterMethod) {
                String fieldName = methodName.substring(SETTER_PREFIX.length()).toLowerCase(Locale.ENGLISH);
                FieldMetadata fieldMetadata = classMetadata.getFieldMetadata(fieldName);
                if (fieldMetadata != null) {
                    fieldAutoTrimIgnored = fieldMetadata.isAutoTrimIgnored();
                    fieldAutoTrim = fieldMetadata.isAutoTrim();
                }
            }
            boolean classAutoTrimIgnored = classMetadata.isAutoTrimIgnored();
            boolean classAutoTrim = classMetadata.isAutoTrim();

            ArrayList<VarStack.StackVar> autoTrimVars = new ArrayList<>();
            for (JCVariableDecl variableDecl : parameters) {
                JCTree variableDeclType = variableDecl.getType();
                if (!matchImportType(STRING_NAME, variableDeclType, context)) {
                    continue;
                }
                boolean varAutoTrim = false;
                boolean varAutoTrimIgnored = false;
                JCModifiers variableDeclModifiers = variableDecl.getModifiers();
                for (JCAnnotation annotation : variableDeclModifiers.getAnnotations()) {
                    if (matchImportType(AUTO_TRIM_TYPE_CANONICAL_NAME, annotation, context)) {
                        varAutoTrim = true;
                        continue;
                    }
                    if (matchImportType(AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME, annotation, context)) {
                        varAutoTrimIgnored = true;
                    }
                }
                autoTrim:
                {
                    // varAutoTrimIgnored > varAutoTrim > methodAutoTrimIgnored > methodAutoTrim
                    // > fieldAutoTrimIgnored > fieldAutoTrim  (if method is setter)
                    // > classAutoTrimIgnored > classAutoTrim
                    if (varAutoTrimIgnored) {
                        varAutoTrim = false;
                        break autoTrim;
                    }
                    if (varAutoTrim) {
                        break autoTrim;
                    }
                    if (methodAutoTrimIgnored) {
                        break autoTrim;
                    }
                    if (methodAutoTrim) {
                        varAutoTrim = true;
                        break autoTrim;
                    }
                    if (isSetterMethod) {
                        if (fieldAutoTrimIgnored) {
                            break autoTrim;
                        }
                        if (fieldAutoTrim) {
                            varAutoTrim = true;
                            break autoTrim;
                        }
                    }
                    if (classAutoTrimIgnored) {
                        break autoTrim;
                    }
                    if (classAutoTrim) {
                        varAutoTrim = true;
                    }
                }

                VarStack.StackVar stackVar = new VarStack.StackVar(variableDecl.getName().toString(), variableDeclType, varAutoTrim, variableDeclModifiers.getFlags().contains(Modifier.FINAL));
                if (stackVar.isAutoTrim()) {
                    autoTrimVars.add(stackVar);
                }
                context.addStackVar(stackVar);
            }

            ArrayList<JCStatement> preStatements = new ArrayList<>();
            // 如果是构造器，且首行为super调用
            if (hasSuperFirst(t)) {
                ArrayList<JCStatement> statements = new ArrayList<>(t.getBody().getStatements());
                // 如果是有参的super构造器
                if (hasArgsSuperFirst(t)) {
                    // 1. 给所有的String类型参数添加trim表达式包装
                    // 2. 替换所有String类型参数出现的地方
                    preStatements.add(process(statements.get(0), context));
                    for (VarStack.StackVar stackVar : autoTrimVars) {
                        java.util.List<JCParens> references = stackVar.getReferences();
                        if (references == null) {
                            continue;
                        }

                        Name varName = elementUtils.getName(stackVar.getName());
                        JCConditional trimConditional = buildTrimConditional(M, elementUtils, varName);
                        for (JCParens reference : references) {
                            reference.expr = trimConditional;
                        }
                        references.clear();
                    }
                } else {
                    preStatements.add(statements.get(0));
                }
                statements.remove(0);
                t.getBody().stats = List.from(statements);
            }

            t.body = process(t.getBody(), context);

            for (VarStack.StackVar stackVar : autoTrimVars) {
                java.util.List<JCParens> references = stackVar.getReferences();
                if (references == null) {
                    continue;
                }

                Name varName = elementUtils.getName(stackVar.getName());
                if (references.size() == 1) {
                    JCParens jcParens = references.get(0);
                    jcParens.expr = buildTrimConditional(M, elementUtils, varName);
                    continue;
                }

                if (stackVar.isNeedFinal()) {
                    final Name tempVarName = elementUtils.getName(TEMP_PREFIX).append(varName);
                    final JCExpression tempVarIdent = M.Ident(tempVarName);

                    final JCVariableDecl tempVar = M.VarDef(M.Modifiers(FINAL), tempVarName,
                            M.Ident(elementUtils.getName(STRING_SIMPLE_NAME)), buildTrimConditional(M, elementUtils, varName));
                    preStatements.add(tempVar);
                    for (JCParens jcParens : references) {
                        jcParens.expr = tempVarIdent;
                    }
                } else {
                    preStatements.add(M.Exec(M.Assign(M.Ident(varName), buildTrimConditional(M, elementUtils, varName))));
                }
            }

            if (!preStatements.isEmpty()) {
                t.getBody().stats = List.from(preStatements).appendList(t.getBody().getStatements());
            }
        }

        context.popVarStack();
        this.processedSymbols.put(key, Boolean.TRUE);
        return t;
    }

    public JCTree visitMethodInvocation(MethodInvocationTree node, AutoTrimContext context) {
        JCMethodInvocation t = (JCMethodInvocation) node;
        t.meth = process(t.meth, context);
        t.args = process(t.args, context);
        return t;
    }

    public JCTree visitModifiers(ModifiersTree node, AutoTrimContext context) {
        JCModifiers t = (JCModifiers) node;
        // List<JCAnnotation> annotations = process(t.annotations, context);
        return t;
    }

    public JCTree visitNewArray(NewArrayTree node, AutoTrimContext context) {
        JCNewArray t = (JCNewArray) node;
        t.elemtype = process(t.elemtype, context);
        t.dims = process(t.dims, context);
        t.elems = process(t.elems, context);
        return t;
    }

    public JCTree visitNewClass(NewClassTree node, AutoTrimContext context) {
        JCNewClass t = (JCNewClass) node;
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
        JCLambda t = (JCLambda) node;
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
        JCParens t = (JCParens) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitReturn(ReturnTree node, AutoTrimContext context) {
        JCReturn t = (JCReturn) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitMemberSelect(MemberSelectTree node, AutoTrimContext context) {
        JCFieldAccess t = (JCFieldAccess) node;
        t.selected = process(t.selected, context);
        return t;
    }

    public JCTree visitMemberReference(MemberReferenceTree node, AutoTrimContext context) {
        JCMemberReference t = (JCMemberReference) node;
        t.expr = process(t.expr, context);
        t.typeargs = process(t.typeargs, context);
        return t;
    }

    public JCTree visitEmptyStatement(EmptyStatementTree node, AutoTrimContext context) {
        JCSkip t = (JCSkip) node;
        return t;
    }

    public JCTree visitSwitch(SwitchTree node, AutoTrimContext context) {
        JCSwitch t = (JCSwitch) node;
        context.newVarStack();
        t.selector = process(t.selector, context);
        t.cases = process(t.cases, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitSynchronized(SynchronizedTree node, AutoTrimContext context) {
        JCSynchronized t = (JCSynchronized) node;
        t.lock = process(t.lock, context);
        t.body = process(t.body, context);
        return t;
    }

    public JCTree visitThrow(ThrowTree node, AutoTrimContext context) {
        JCThrow t = (JCThrow) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitCompilationUnit(CompilationUnitTree node, AutoTrimContext context) {
        JCCompilationUnit t = (JCCompilationUnit) node;
        return t;
    }

    public JCTree visitTry(TryTree node, AutoTrimContext context) {
        JCTry t = (JCTry) node;
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

    public JCTree visitParameterizedType(ParameterizedTypeTree node, AutoTrimContext context) {
        JCTypeApply t = (JCTypeApply) node;
        return t;
    }

    public JCTree visitUnionType(UnionTypeTree node, AutoTrimContext context) {
        JCTypeUnion t = (JCTypeUnion) node;
        // List<JCExpression> components = process(t.alternatives, context);
        return t;
    }

    public JCTree visitIntersectionType(IntersectionTypeTree node, AutoTrimContext context) {
        JCTypeIntersection t = (JCTypeIntersection) node;
        t.bounds = process(t.bounds, context);
        return t;
    }

    public JCTree visitArrayType(ArrayTypeTree node, AutoTrimContext context) {
        JCArrayTypeTree t = (JCArrayTypeTree) node;
        t.elemtype = process(t.elemtype, context);
        return t;
    }

    public JCTree visitTypeCast(TypeCastTree node, AutoTrimContext context) {
        JCTypeCast t = (JCTypeCast) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitPrimitiveType(PrimitiveTypeTree node, AutoTrimContext context) {
        JCPrimitiveTypeTree t = (JCPrimitiveTypeTree) node;
        return t;
    }

    public JCTree visitTypeParameter(TypeParameterTree node, AutoTrimContext context) {
        JCTypeParameter t = (JCTypeParameter) node;
        // List<JCAnnotation> annos = process(t.annotations, context);
        // List<JCExpression> bounds = process(t.bounds, context);
        return t;
    }

    public JCTree visitInstanceOf(InstanceOfTree node, AutoTrimContext context) {
        JCInstanceOf t = (JCInstanceOf) node;
        t.expr = process(t.expr, context);
        return t;
    }

    public JCTree visitUnary(UnaryTree node, AutoTrimContext context) {
        JCUnary t = (JCUnary) node;
        t.arg = process(t.arg, context);
        return t;
    }

    public JCTree visitVariable(VariableTree node, AutoTrimContext context) {
        JCVariableDecl t = (JCVariableDecl) node;
        JCExpression initializer = t.getInitializer();
        if (initializer != null) {
            JCExpression newInitializer = process(t.getInitializer(), context);
            if (newInitializer != initializer) {
                t.init = newInitializer;
            }
        }
        context.addStackVar(new VarStack.StackVar(t.getName().toString()));
        return t;
    }

    public JCTree visitWhileLoop(WhileLoopTree node, AutoTrimContext context) {
        JCWhileLoop t = (JCWhileLoop) node;
        t.body = process(t.body, context);
        t.cond = process(t.cond, context);
        return t;
    }

    public JCTree visitWildcard(WildcardTree node, AutoTrimContext context) {
        JCWildcard t = (JCWildcard) node;
        // TypeBoundKind kind = M.at(t.kind.pos).TypeBoundKind(t.kind.kind);
        // JCTree inner = process(t.inner, context);
        return t;
    }


    public JCTree visitOther(Tree node, AutoTrimContext context) {
        JCTree tree = (JCTree) node;
        switch (tree.getTag()) {
            case LETEXPR: {
                LetExpr t = (LetExpr) node;
                t.defs = process(t.defs, context);
                t.expr = process(t.expr, context);
                return t;
            }
            default:
                throw new AssertionError("unknown tree tag: " + tree.getTag());
        }
    }

    @Override
    protected JCTree defaultAction(Tree node, AutoTrimContext autoTrimContext) {
        return (JCTree) node;
    }

    private static JCConditional buildTrimConditional(TreeMaker treeMaker, JavacElements elementUtils, Name varName) {
        JCIdent variableIdent = treeMaker.Ident(varName);
        return treeMaker.Conditional(
                treeMaker.Binary(Tag.EQ, variableIdent, treeMaker.Literal(TypeTag.BOT, null))
                , treeMaker.Literal(TypeTag.BOT, null)
                , treeMaker.Apply(List.nil(),
                        treeMaker.Select(variableIdent, elementUtils.getName(STRING_TRIM_METHOD_NAME))
                        , List.nil())
        );
    }


    private static boolean isEmptyMethod(JCMethodDecl jcMethodDecl) {
        // 跳过全空的方法
        if (jcMethodDecl.getBody() == null) {
            return true;
        }
        final List<JCStatement> statements = jcMethodDecl.getBody().getStatements();
        return statements == null || statements.isEmpty();
    }

    private boolean hasSuperFirst(MethodTree methodTree) {
        boolean isInitMethod = methodTree.getName().contentEquals(INIT_METHOD_NAME);
        if (!isInitMethod) {
            return false;
        }
        final StatementTree statementTree = methodTree.getBody().getStatements().get(0);
        if (!(statementTree instanceof ExpressionStatementTree)) {
            return false;
        }

        final ExpressionTree expressionTree = ((ExpressionStatementTree) statementTree).getExpression();
        if (!(expressionTree instanceof MethodInvocationTree)) {
            return false;
        }

        final ExpressionTree methodSelect = ((MethodInvocationTree) expressionTree).getMethodSelect();
        if (!(methodSelect instanceof IdentifierTree)) {
            return false;
        }
        return ((IdentifierTree) methodSelect).getName().contentEquals(SUPER_IDENT_NAME);
    }

    private boolean hasArgsSuperFirst(MethodTree methodTree) {
        boolean isInitMethod = methodTree.getName().contentEquals(INIT_METHOD_NAME);
        if (!isInitMethod) {
            return false;
        }
        final StatementTree statementTree = methodTree.getBody().getStatements().get(0);
        if (!(statementTree instanceof ExpressionStatementTree)) {
            return false;
        }

        final ExpressionTree expressionTree = ((ExpressionStatementTree) statementTree).getExpression();
        if (!(expressionTree instanceof MethodInvocationTree)) {
            return false;
        }

        final ExpressionTree methodSelect = ((MethodInvocationTree) expressionTree).getMethodSelect();
        if (!(methodSelect instanceof IdentifierTree)) {
            return false;
        }

        if (!((IdentifierTree) methodSelect).getName().contentEquals(SUPER_IDENT_NAME)) {
            return false;
        }

        final java.util.List<? extends ExpressionTree> arguments = ((MethodInvocationTree) expressionTree).getArguments();
        return arguments != null && !arguments.isEmpty();
    }


    public boolean matchImportType(String typeName, JCTree jcTree, AutoTrimContext context) {
        if (jcTree == null || typeName == null) {
            return false;
        }
        final Type type = jcTree.type;
        if (type != null && typeName.equals(type.toString())) {
            return true;
        }
        String annotationTypeName;
        if (jcTree instanceof JCAnnotation) {
            JCTree annotationType = ((JCAnnotation) jcTree).getAnnotationType();
            Name name = TreeInfo.fullName(annotationType);
            if (name == null) {
                return false;
            }
            annotationTypeName = name.toString();
        } else {
            Name name = TreeInfo.fullName(jcTree);
            if (name == null) {
                return false;
            }
            annotationTypeName = name.toString();
        }

        if (typeName.equals(annotationTypeName)) {
            return true;
        }
        if (typeName.length() > annotationTypeName.length() && typeName.endsWith(annotationTypeName)) {
            ClassMetadata classMetadata = context.getClassMetadata();
            Boolean typeMatchResult = classMetadata.getTypeMatchResult(typeName, annotationTypeName);
            if (typeMatchResult != null) {
                return typeMatchResult;
            }
            String[] names = annotationTypeName.split("\\.", 2);
            String firstSimpleName = names[0];
            if (classMetadata.hasSubClass(firstSimpleName, true)) {
                return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, false);
            }
            final ImportMetadata importMetadata = context.getImportMetadata();
            String packageName = importMetadata.getPackageName();
            if (elementUtils.getTypeElement(packageName == null ? firstSimpleName : packageName + "." + firstSimpleName) != null) {
                return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, false);
            }
            Set<String> packages = importMetadata.getImportMap().keySet();
            if (packages.contains(annotationTypeName)) {
                return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, false);
            }
            if (packages.contains(typeName)) {
                return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, true);
            }
            boolean hasSuffix = names.length > 1 && names[2].trim().length() > 0;
            String firstSimpleNameAndPrefix = "." + firstSimpleName;
            if (hasSuffix) {
                String suffix = "." + names[2].trim();
                for (String pkg : packages) {
                    if (pkg.equals(firstSimpleName) || pkg.endsWith(firstSimpleNameAndPrefix)) {
                        Symbol.ClassSymbol typeElement = elementUtils.getTypeElement(pkg + suffix);
                        if (typeElement != null && typeName.equals(typeElement.asType().toString())) {
                            return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, true);
                        }
                    }
                }
            }
            for (String pkg : packages) {
                if (pkg.endsWith("*") && typeName.startsWith(pkg)) {
                    Symbol.ClassSymbol typeElement = elementUtils.getTypeElement(pkg.substring(0, pkg.length() - 1) + annotationTypeName);
                    if (typeElement != null && typeName.equals(typeElement.asType().toString())) {
                        return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, true);
                    }
                }
            }
            {// java.lang.*
                Symbol.ClassSymbol typeElement = elementUtils.getTypeElement("java.lang." + annotationTypeName);
                if (typeElement != null && typeName.equals(typeElement.asType().toString())) {
                    return classMetadata.cacheTypeMatchResult(typeName, annotationTypeName, true);
                }
            }
        }

        return false;
    }

}