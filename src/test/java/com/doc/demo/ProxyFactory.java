package com.doc.demo;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
    public static HttpApi getProxy(HttpApi target) {
        return (HttpApi) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new LogHandler(target));
    }

    private static class LogHandler implements InvocationHandler {
        private HttpApi target;

        LogHandler(HttpApi target) {
            this.target = target;
        }

        // method底层的方法无参数时，args为空或者长度为0
        @Override
        public Object invoke(Object proxy, Method method, @Nullable Object[] args)
                throws Throwable {
            System.out.println("method = " + method.getName());
            System.out.println("proxy = " + 66666);
            Object invoke = method.invoke(target, args);
            System.out.println("proxy = " + 77777);
            // 扩展的功能
//            Log.i("http-statistic", (String) args[0]);
            // 访问基础对象
            return invoke;
        }
    }

    public static void main(String[] args) {
        long l = TimeUnit.HOURS.toMillis(1);
        System.out.println("l = " + l);
        // HttpApi api = new RealModule();
        // HttpApi proxy = getProxy(api);
        // proxy.get("asdasdas");
        // HttpApi realModule = new RealModule();
        // realModule.get("4as5d4asd5a");
    }
}
