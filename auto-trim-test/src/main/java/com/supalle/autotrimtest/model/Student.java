package com.supalle.autotrimtest.model;

import com.supalle.autotrim.AutoTrim;
import lombok.Data;

@Data
@AutoTrim
public class Student {
    private final String name;
    private int age;

    public String make(String var1, @AutoTrim.Ignored String var2) {
        return var1 + var2;
    }
}
