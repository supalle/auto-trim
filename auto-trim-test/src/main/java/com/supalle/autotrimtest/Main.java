package com.supalle.autotrimtest;

import com.supalle.autotrim.AutoTrim;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }


    @AutoTrim
    public static String test(String string) {
        return string;
    }
}