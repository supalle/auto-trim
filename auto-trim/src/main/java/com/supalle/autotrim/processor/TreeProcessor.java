package com.supalle.autotrim.processor;

import com.sun.source.tree.TreeVisitor;

import javax.lang.model.SourceVersion;

public interface TreeProcessor<R, P> extends TreeVisitor<R, P> {

    SourceVersion getSupportedSourceVersion();


}
