package com.supalle.autotrim.processor;

import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.supalle.autotrim.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.sun.tools.javac.code.Flags.FINAL;

public abstract class AbstractTreeProcessor extends SimpleTreeVisitor<JCTree, AutoTrimContext> implements TreeProcessor<JCTree, AutoTrimContext> {
    protected static final String AUTO_TRIM_TYPE_CANONICAL_NAME = AutoTrim.class.getCanonicalName();
    protected static final String AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME = AutoTrim.Ignored.class.getCanonicalName();
    public static final String SUPER_IDENT_NAME = "super";
    public static final String INIT_METHOD_NAME = "<init>";
    protected static final String SETTER_PREFIX = "set";
    public static final String TEMP_PREFIX = "_$AutoTrim$_";
    protected static final String STRING_SIMPLE_NAME = String.class.getSimpleName();
    protected static final String STRING_NAME = String.class.getCanonicalName();
    protected static final String STRING_TRIM_METHOD_NAME = "trim";
    protected final TreeMaker M;
    protected final JavacElements elementUtils;
    protected final ConcurrentMap<String, Boolean> processedSymbols;

    public AbstractTreeProcessor(TreeMaker m, JavacElements elementUtils, ConcurrentMap<String, Boolean> processedSymbols) {
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
        if (trees == null || trees.isEmpty())
            return trees;
        ListBuffer<T> lb = new ListBuffer<T>();
        for (T tree : trees)
            lb.append(process(tree, context));
        return lb.toList();
    }

    @Override
    protected JCTree defaultAction(Tree node, AutoTrimContext autoTrimContext) {
        return (JCTree) node;
    }

    // =================================================
    // ==================== code =======================
    // =================================================
    public JCTree visitClass(ClassTree node, AutoTrimContext context) {
        JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) node;
        context.newVarStack();
        final ClassMetadata classMetadata = context.getClassMetadata();
        String className;
        boolean innerClass;
        boolean anonymousInnerClass = false;
        if (jcClassDecl.sym != null) {
            innerClass = jcClassDecl.sym.owner instanceof Symbol.ClassSymbol;
            className = jcClassDecl.sym.fullname.toString();
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
        final ArrayList<JCTree.JCVariableDecl> fields = new ArrayList<>();
        final ArrayList<JCTree.JCMethodDecl> methods = new ArrayList<>();
        final ArrayList<JCTree.JCClassDecl> subClasses = new ArrayList<>();
        final ArrayList<JCTree> others = new ArrayList<>();
        for (JCTree member : members) {
            if (member instanceof JCTree.JCVariableDecl) {
                fields.add((JCTree.JCVariableDecl) member);
            } else if (member instanceof JCTree.JCMethodDecl) {
                methods.add((JCTree.JCMethodDecl) member);
            } else if (member instanceof JCTree.JCClassDecl) {
                subClasses.add((JCTree.JCClassDecl) member);
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

        for (JCTree.JCClassDecl subClass : subClasses) {
            Name simpleName = subClass.getSimpleName();
            if (simpleName != null) {
                String subClassName = simpleName.toString();
                if (subClassName.length() > 0) {
                    newClassMetadata.addSubClasses(subClassName);
                }
            }
        }

        for (JCTree.JCAnnotation annotation : jcClassDecl.getModifiers().getAnnotations()) {
            if (matchImportType(AUTO_TRIM_TYPE_CANONICAL_NAME, annotation, context)) {
                newClassMetadata.setAutoTrim(true);
                continue;
            }
            if (matchImportType(AUTO_TRIM_IGNORED_TYPE_CANONICAL_NAME, annotation, context)) {
                newClassMetadata.setAutoTrimIgnored(true);
            }
        }

        for (JCTree.JCVariableDecl variableDecl : fields) {
            boolean autoTrim = false;
            boolean autoTrimIgnored = false;
            for (JCTree.JCAnnotation annotation : variableDecl.getModifiers().getAnnotations()) {
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
        for (JCTree.JCMethodDecl method : methods) {
            process(method, context);
        }
        for (JCTree.JCClassDecl subClass : subClasses) {
            process(subClass, context);
        }

        context.popClassMetadata();
        context.popVarStack();
        return jcClassDecl;
    }

    public JCTree visitMethod(MethodTree node, AutoTrimContext context) {
        JCTree.JCMethodDecl t = (JCTree.JCMethodDecl) node;
        String methodName = t.getName().toString();
        String key = context.getClassMetadata().getName() + "#" + methodName;
        if (Boolean.TRUE.equals(this.processedSymbols.get(key))) {
            return t;
        }

        if (isEmptyMethod(t)) {
            return t;
        }
        context.newVarStack();

        List<JCTree.JCVariableDecl> parameters = t.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            boolean methodAutoTrim = false;
            boolean methodAutoTrimIgnored = false;
            for (JCTree.JCAnnotation annotation : t.getModifiers().getAnnotations()) {
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
            for (JCTree.JCVariableDecl variableDecl : parameters) {
                JCTree variableDeclType = variableDecl.getType();
                if (!matchImportType(STRING_NAME, variableDeclType, context)) {
                    continue;
                }
                boolean varAutoTrim = false;
                boolean varAutoTrimIgnored = false;
                JCTree.JCModifiers variableDeclModifiers = variableDecl.getModifiers();
                for (JCTree.JCAnnotation annotation : variableDeclModifiers.getAnnotations()) {
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

                VarStack.StackVar stackVar = new VarStack.StackVar(variableDecl.getName().toString(), varAutoTrim, variableDeclModifiers.getFlags().contains(Modifier.FINAL));
                if (stackVar.isAutoTrim()) {
                    autoTrimVars.add(stackVar);
                }
                context.addStackVar(stackVar);
            }

            ArrayList<JCTree.JCStatement> preStatements = new ArrayList<>();
            // 如果是构造器，且首行为super调用
            if (hasSuperFirst(t)) {
                ArrayList<JCTree.JCStatement> statements = new ArrayList<>(t.getBody().getStatements());
                // 如果是有参的super构造器
                if (hasArgsSuperFirst(t)) {
                    // 1. 给所有的String类型参数添加trim表达式包装
                    // 2. 替换所有String类型参数出现的地方
                    preStatements.add(process(statements.get(0), context));
                    for (VarStack.StackVar stackVar : autoTrimVars) {
                        java.util.List<JCTree.JCParens> references = stackVar.getReferences();
                        if (references == null) {
                            continue;
                        }

                        Name varName = elementUtils.getName(stackVar.getName());
                        JCTree.JCConditional trimConditional = buildTrimConditional(M, elementUtils, varName);
                        for (JCTree.JCParens reference : references) {
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
                java.util.List<JCTree.JCParens> references = stackVar.getReferences();
                if (references == null) {
                    continue;
                }

                Name varName = elementUtils.getName(stackVar.getName());
                if (references.size() == 1) {
                    JCTree.JCParens jcParens = references.get(0);
                    jcParens.expr = buildTrimConditional(M, elementUtils, varName);
                    continue;
                }

                if (stackVar.isNeedFinal()) {
                    final Name tempVarName = elementUtils.getName(TEMP_PREFIX).append(varName);
                    final JCTree.JCExpression tempVarIdent = M.Ident(tempVarName);

                    final JCTree.JCVariableDecl tempVar = M.VarDef(M.Modifiers(FINAL), tempVarName,
                            M.Ident(elementUtils.getName(STRING_SIMPLE_NAME)), buildTrimConditional(M, elementUtils, varName));
                    preStatements.add(tempVar);
                    for (JCTree.JCParens jcParens : references) {
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

    public JCTree visitVariable(VariableTree node, AutoTrimContext context) {
        JCTree.JCVariableDecl t = (JCTree.JCVariableDecl) node;
        JCTree.JCExpression initializer = t.getInitializer();
        if (initializer != null) {
            JCTree.JCExpression newInitializer = process(t.getInitializer(), context);
            if (newInitializer != initializer) {
                t.init = newInitializer;
            }
        }
        context.addStackVar(new VarStack.StackVar(t.getName().toString()));
        return t;
    }

    public JCTree visitBlock(BlockTree node, AutoTrimContext context) {
        JCTree.JCBlock t = (JCTree.JCBlock) node;
        context.newVarStack();
        t.stats = process(t.stats, context);
        context.popVarStack();
        return t;
    }

    public JCTree visitIdentifier(IdentifierTree node, AutoTrimContext context) {
        JCTree.JCIdent t = (JCTree.JCIdent) node;
        VarStack varStack = context.getVarStack();
        VarStack.StackVar stackVar = varStack.getStackVar(t.getName().toString());
        if (stackVar != null && stackVar.isAutoTrim()) {
            if (context.getClassMetadata().isInnerClass()) {
                stackVar.setNeedFinal(true);
            }
            JCTree.JCParens reference = M.Parens(t);
            stackVar.addReference(reference);
            return reference;
        }
        return t;
    }

    // =================================================
    // ==================== code =======================
    // =================================================


    protected static JCTree.JCConditional buildTrimConditional(TreeMaker treeMaker, JavacElements elementUtils, Name varName) {
        JCTree.JCIdent variableIdent = treeMaker.Ident(varName);
        return treeMaker.Conditional(
                treeMaker.Binary(JCTree.Tag.EQ, variableIdent, treeMaker.Literal(TypeTag.BOT, null))
                , treeMaker.Literal(TypeTag.BOT, null)
                , treeMaker.Apply(List.nil(),
                        treeMaker.Select(variableIdent, elementUtils.getName(STRING_TRIM_METHOD_NAME))
                        , List.nil())
        );
    }


    protected static boolean isEmptyMethod(JCTree.JCMethodDecl jcMethodDecl) {
        // 跳过全空的方法
        if (jcMethodDecl.getBody() == null) {
            return true;
        }
        final List<JCTree.JCStatement> statements = jcMethodDecl.getBody().getStatements();
        return statements == null || statements.isEmpty();
    }

    protected boolean hasSuperFirst(MethodTree methodTree) {
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

    protected boolean hasArgsSuperFirst(MethodTree methodTree) {
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
        if (jcTree instanceof JCTree.JCAnnotation) {
            JCTree annotationType = ((JCTree.JCAnnotation) jcTree).getAnnotationType();
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
