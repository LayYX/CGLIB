package net.sf.cglib.learn;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MyMethodInterceptor implements MethodInterceptor {
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("before: " + method.getName());
        methodProxy.invokeSuper(o, objects);
        System.out.println("after: " + method.getName());
        return null;
    }
}
