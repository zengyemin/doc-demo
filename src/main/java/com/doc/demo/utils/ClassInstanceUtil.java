package com.doc.demo.utils;


import org.aspectj.apache.bcel.generic.ClassGenException;

/**
 * @author : zengYeMin
 * @date : 2022/4/6 18:10
 **/
public class ClassInstanceUtil {
    /**
     * 实例化DocStream时检测是否符合实例化规则
     */
    public static void docStreamInstanceCheck() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (!"getDocStreamInstance".equals(stackTrace[4].getMethodName())) {
            throw new ClassGenException("请使用DocStreamFactory.getDocStreamInstance方法进行对象实例化");
        }
    }
}
