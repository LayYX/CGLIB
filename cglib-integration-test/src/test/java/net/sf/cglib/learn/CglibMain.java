package net.sf.cglib.learn;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.KeyFactory;
import net.sf.cglib.proxy.Enhancer;
import org.junit.Test;

import java.util.UUID;

public class CglibMain {

    public static void main(String[] args) {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "G:/cglib");
        MyMethodInterceptor interceptor = new MyMethodInterceptor();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MyProxy.class);
        enhancer.setCallback(interceptor);

        MyProxy proxy = (MyProxy) enhancer.create();
        proxy.print();
    }

    @Test
    public void keyFactory() {
        CglibKey key = (CglibKey) KeyFactory.create(CglibKey.class);
        System.out.println(key.getClass());
        Object o = key.newInstance("cglib", "value", UUID.randomUUID().toString(), new Enhancer());
        System.out.println(o.toString());
        System.out.println(o.getClass());
    }
}
