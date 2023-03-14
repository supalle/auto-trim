package com.supalle.autotrimtest.model;

import com.supalle.autotrim.AutoTrim;

import java.util.function.Function;

//@Data
@AutoTrim
public class Student {
    private final String name;
    private int age;

    public Student(String name) {
        this.name = name;
    }

    public String make(String var1, @AutoTrim.Ignored String var2) {
        return var1 + var2 + new Function<String, String>() {

            @Override
            @AutoTrim
            public String apply(String s) {
                return s + s;
            }

        }.apply(var1);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    //    @Data
    @AutoTrim
    class SubStudent {
        private String name;

        public SubStudent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;

        }
    }

}
