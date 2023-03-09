package com.supalle.autotrim;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE
        , ElementType.CONSTRUCTOR
        , ElementType.FIELD
        , ElementType.METHOD
        , ElementType.PARAMETER})
@Inherited
@Documented
public @interface AutoTrim {
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE
            , ElementType.CONSTRUCTOR
            , ElementType.FIELD
            , ElementType.METHOD
            , ElementType.PARAMETER})
    @Inherited
    @Documented
    @interface Ignored {
    }
}

