package net.sf.cglib.learn;

import net.sf.cglib.proxy.Enhancer;

interface CglibKey {
    public Object newInstance(
        String name,
        String valve,
        String id,
        Enhancer enhancer
    );
}
