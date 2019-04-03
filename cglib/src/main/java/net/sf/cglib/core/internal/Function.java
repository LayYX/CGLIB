package net.sf.cglib.core.internal;

/**
 * Function 接口用于模拟函数式编程
 * 每一个接口实现类会模拟一个函数的行为，避免创建只有一个方法的类
 * @param <K>
 * @param <V>
 */
public interface Function<K, V> {
    V apply(K key);
}
