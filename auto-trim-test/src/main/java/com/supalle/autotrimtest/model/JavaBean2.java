package com.supalle.autotrimtest.model;

import com.supalle.autotrim.AutoTrim;
//import lombok.Data;

//@Data
@AutoTrim
public class JavaBean2 {
    private String username;
    private String password;

    public JavaBean2(String username, String password) {
        this.username = username;
        this.password = password;

    }

    public JavaBean2(String username, int password) {
        this.username = username;
        this.password = "password" + password;

    }

    class SubClass {
        @AutoTrim
        public void test(String s) {
            System.out.println(s);
        }

        JavaBean2 of(String username, String password) {
            return new JavaBean2(username, password);
        }
    }

//    class String{
//
//    }
}
