package com.supalle.autotrim.processor;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.supalle.autotrim.AutoTrimContext;

import javax.lang.model.SourceVersion;
import java.util.concurrent.ConcurrentMap;

public class Processors {

    public static TreeProcessor<JCTree, AutoTrimContext>
    getProcessor(SourceVersion sourceVersion
            , TreeMaker m
            , JavacElements elementUtils
            , ConcurrentMap<String, Boolean> processedSymbols) {

        switch (sourceVersion.name()) {
            case "RELEASE_8":
            case "RELEASE_9":
            case "RELEASE_10":
            case "RELEASE_11": {
                return new Java8TreeProcessor(m, elementUtils, processedSymbols);
            }
            case "RELEASE_12":
            case "RELEASE_13": {
                return new Java12TreeProcessor(m, elementUtils, processedSymbols);
            }
            case "RELEASE_14":
            case "RELEASE_15":
            case "RELEASE_16": {
                return new Java14TreeProcessor(m, elementUtils, processedSymbols);
            }
            case "RELEASE_17":
            case "RELEASE_18": {
                return new Java17TreeProcessor(m, elementUtils, processedSymbols);
            }
            case "RELEASE_19": {
                return new Java19TreeProcessor(m, elementUtils, processedSymbols);
            }
            case "RELEASE_20":
            case "RELEASE_21": {
                return new Java20TreeProcessor(m, elementUtils, processedSymbols);
            }
            default: {
                throw new IllegalArgumentException("不支持的Javac版本" + sourceVersion);
            }
        }
    }

}
