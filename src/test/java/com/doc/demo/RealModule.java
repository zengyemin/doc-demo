package com.doc.demo;

public class RealModule implements HttpApi {
    @Override
    public String get(String url) {
        System.out.println("url = " + url);
        return "result";
    }
}
