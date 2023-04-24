package com.supalle.autotrimtest.model;

import com.supalle.autotrim.AutoTrim;
//import lombok.Data;

//@Data
@AutoTrim
public class JavaBean {
    private java.lang.String username;
    private java.lang.String password;

    public JavaBean(final java.lang.String username, java.lang.String password) {
        this.username = username;
        this.password = password + username;

    }

    class SubClass {
        @AutoTrim
        public void test(java.lang.String s) {
            final java.lang.String a = "" + System.currentTimeMillis();
            System.out.println(a + s + s);
            // System.out.println((java.lang.String) null);
            // System.out.println((s == null ? null : s.trim()));
        }
    }

    class String {

    }
}
