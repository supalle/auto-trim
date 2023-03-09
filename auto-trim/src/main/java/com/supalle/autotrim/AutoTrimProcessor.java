package com.supalle.autotrim;

import com.google.auto.service.AutoService;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Pair;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AutoService(Processor.class)
public class AutoTrimProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;
    private JavacElements elementUtils;
    private TreeMaker treeMaker;

    // 候选集合 ElementsAnnotated、JCTree
    private final ConcurrentMap<Element, Pair<JCTree, JCTree.JCCompilationUnit>> candidates = new ConcurrentHashMap<>();

    // 已处理集合
    private final ConcurrentMap<String, Boolean> processedSymbols = new ConcurrentHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(Objects.requireNonNull(getContext(processingEnv), "不支持的javac编译环境，无法获取编译上下文"));
    }

    private Context getContext(ProcessingEnvironment processingEnv) {
        Elements elements = processingEnv.getElementUtils();
        com.sun.tools.javac.main.JavaCompiler javaCompiler = getFieldValue("javaCompiler", elements);
        return getFieldValue("context", javaCompiler);
    }

    private <T> T getFieldValue(String fieldName, Object instance) {
        if (instance == null || fieldName == null) {
            return null;
        }
        try {
            Optional<Field> opt = Arrays.stream(instance.getClass().getDeclaredFields())
                    .filter(field -> fieldName.equals(field.getName()))
                    .findFirst();
            if (opt.isPresent()) {
                Field field = opt.get();
                field.setAccessible(true);
                return (T) field.get(instance);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoTrim.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> rootElements = roundEnv.getRootElements();
        for (Element rootElement : rootElements) {
            candidates.put(rootElement, elementUtils.getTreeAndTopLevel(rootElement, null, null));
        }
        if (roundEnv.processingOver() || roundEnv.errorRaised() || LombokState.isLombokInvoked()) {
            finishRemaining();
            return true;
        }
        return false;
    }


    private void finishRemaining() {
        AutoTrimTreeProcessor processor = new AutoTrimTreeProcessor(treeMaker, elementUtils, processedSymbols);

        for (Map.Entry<Element, Pair<JCTree, JCTree.JCCompilationUnit>> entry : this.candidates.entrySet()) {
            // final Element rootElement = entry.getKey();
            // String enclosingElementType = rootElement.getEnclosingElement().asType().toString();

            final Pair<JCTree, JCTree.JCCompilationUnit> value = entry.getValue();
            final JCTree jcTree = value.fst;
            AutoTrimContext context = new AutoTrimContext();
            ImportMetadata importMetadata = new ImportMetadata();
            final JCTree.JCCompilationUnit jcCompilationUnit = value.snd;
            List<JCTree.JCImport> imports = jcCompilationUnit.getImports();
            if (imports != null && !imports.isEmpty()) {
                for (JCTree.JCImport jcImport : imports) {
                    importMetadata.addImport(jcImport);
                }
            }
            ExpressionTree packageName = jcCompilationUnit.getPackageName();
            if (packageName != null) {
                importMetadata.setPackageName(packageName.toString());
            }
            context.setImportMetadata(importMetadata);

            processor.process(jcTree, context);
        }
    }

}