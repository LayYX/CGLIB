package net.sf.cglib.core.internal;

import java.util.concurrent.*;

/**
 *
 * @param <K>   AbstractClassGenerator
 * @param <KK>  KeyFoctory 创建的 key factory
 * @param <V>   load 函数加载返回值
 */
public class LoadingCache<K, KK, V> {
    protected final ConcurrentMap<KK, Object> map;
    protected final Function<K, V> loader;
    protected final Function<K, KK> keyMapper;

    public static final Function IDENTITY = new Function() {
        public Object apply(Object key) {
            return key;
        }
    };

    public LoadingCache(Function<K, KK> keyMapper, Function<K, V> loader) {
        // 返回 generator 的 key 函数
        this.keyMapper = keyMapper;
        // 类加载函数
        this.loader = loader;
        // key:加载类 缓存
        this.map = new ConcurrentHashMap<KK, Object>();
    }

    @SuppressWarnings("unchecked")
    public static <K> Function<K, K> identity() {
        return IDENTITY;
    }

    /**
     *
     * @param key generator
     * @return
     */
    public V get(K key) {
        // 使用 generator 缓存的 key 获取之前加载的类
        final KK cacheKey = keyMapper.apply(key);
        Object v = map.get(cacheKey);
        if (v != null && !(v instanceof FutureTask)) {
            return (V) v;
        }

        // 创建新的类
        return createEntry(key, cacheKey, v);
    }

    /**
     * 加载类并缓存
     *
     * Loads entry to the cache.
     * If entry is missing, put {@link FutureTask} first so other competing thread might wait for the result.
     * @param key original key that would be used to load the instance
     * @param cacheKey key that would be used to store the entry in internal map
     * @param v null or {@link FutureTask<V>}
     * @return newly created instance
     */
    protected V createEntry(final K key, KK cacheKey, Object v) {
        FutureTask<V> task;
        boolean creator = false;
        if (v != null) {
            // Another thread is already loading an instance
            task = (FutureTask<V>) v;
        } else {
            // 创建异步任务：通过 load 函数使用 generator 生成代理类
            task = new FutureTask<V>(new Callable<V>() {
                public V call() throws Exception {
                    return loader.apply(key);
                }
            });

            // 使用 key 缓存异步任务
            Object prevTask = map.putIfAbsent(cacheKey, task);

            if (prevTask == null) {
                // 之前没有缓存异步任务
                // creator does the load
                creator = true;
                task.run();
            } else if (prevTask instanceof FutureTask) {
                // 使用之前的异步任务获取类
                task = (FutureTask<V>) prevTask;
            } else {
                return (V) prevTask;
            }
        }

        V result;
        try {
            // 阻塞调用 get 方法等待代理类创建完毕
            result = task.get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while loading cache item", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            throw new IllegalStateException("Unable to load cache item", cause);
        }
        if (creator) {
            map.put(cacheKey, result);
        }
        return result;
    }
}
