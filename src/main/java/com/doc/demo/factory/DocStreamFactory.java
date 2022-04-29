package com.doc.demo.factory;

import com.doc.demo.enums.DocStreamEnum;
import com.doc.demo.exception.DocStreamException;
import com.doc.demo.proxy.DocStreamProxy;
import com.doc.demo.stream.DocStream;
import com.doc.demo.stream.impl.LocalDocStreamImpl;
import com.doc.demo.stream.impl.MinioDocStreamImpl;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class DocStreamFactory {
    /**
     * instanceMap用于做对象实例化的缓存信息,key为对应实例化的枚举状态
     * Map<Boolean, DocStream> 为具体实例化的对象信息
     * {@code Boolean=true} 表示为代理缓存实例
     * {@code Boolean=false} 表示为非代理缓存实例
     */
    private static Map<DocStreamEnum, Map<Boolean, DocStream>> instanceMap = new HashMap<>();

    /**
     * 创建{@link DocStream}的实现类对象
     * 如果没有对应的实现类则创建失败，返回一个空
     *
     * @param streamEnum 要被创建的状态枚举
     * @param proxy      是否加上动态代理 {@code proxy==true} 则加上动态代理
     * @return 标准返回参数 {@link DocStream} 对象
     */
    public static DocStream getDocStreamInstance(DocStreamEnum streamEnum, boolean proxy) {
        //获取当前对象中已经实例化的对象
        Map<Boolean, DocStream> objMap = instanceMap.get(streamEnum);
        //如果为空则创建新的内部容器objMap
        objMap = CollectionUtils.isEmpty(objMap) ? new HashMap<>() : objMap;
        //如果当前对象已经存入缓存了，则直接返回缓存中的对象，不进行实例化调用
        if (objMap.containsKey(proxy)) return objMap.get(proxy);
        switch (streamEnum) {
            case DOC_MINIO: {
                //minio操作对象的实例化,并且设置缓存
                return minioDocStreamInstance(streamEnum, proxy, objMap);
            }
            case DOC_LOCAL: {
                //local本地流操作对象的实例化,并且设置缓存
                return localDocStreamInstance(streamEnum, proxy, objMap);
            }
            default: {
                throw new DocStreamException("当前DocStream暂无具体实现方案 streamEnum:{}" + streamEnum);
            }
        }
    }

    /**
     * 获取一个动态代理的实例化对象
     *
     * @param doc {@link DocStream}的实现类
     * @return 返回一个实例化的代理对象
     */
    @SuppressWarnings("all")
    private static DocStream getDocStreamProxy(DocStream doc) {
        DocStream newDoc = (DocStream) Proxy.newProxyInstance(
                doc.getClass().getClassLoader(),
                doc.getClass().getInterfaces(),
                new DocStreamProxy(doc));
        return newDoc;
    }

    /**
     * 进行实例化的缓存，与应用的生命周期一致
     *
     * @param proxy      是否代理
     * @param streamEnum 当前实例化对象对应的枚举
     * @param objMap     保存具体实例化对象的内部map
     * @param doc        当前实例化的对象
     * @param <T>        缓存实例化对象必须继承 {@link DocStream}
     */
    private static <T extends DocStream> void cacheInstance(boolean proxy, DocStreamEnum streamEnum,
                                                            Map<Boolean, DocStream> objMap, T doc) {
        objMap.put(proxy, doc);//设置具体实例化对象
        instanceMap.put(streamEnum, objMap);//保存当前枚举状态的实例化信息
    }

    /**
     * minio操作对象的实例化
     * 实例化完毕后会对其进行缓存，最终放入 {@link DocStreamFactory#instanceMap}中
     *
     * @param streamEnum 对应的枚举状态
     * @param proxy      是否创建动态代理
     * @param objMap     具体实例化对象保存的内容
     * @return 返回实例化完毕的 {@link DocStream}
     */
    private static DocStream minioDocStreamInstance(DocStreamEnum streamEnum, boolean proxy, Map<Boolean, DocStream> objMap) {
        MinioDocStreamImpl instance = MinioDocStreamImpl.instance();
        if (!proxy) {
            //进行实例化的缓存，与应用的生命周期一致
            cacheInstance(false, streamEnum, objMap, instance);//minio
            return instance;
        }
        //todo 后续如果实例化类型多了，可以将下方生成实例化代理对象进行抽离拆分
        DocStream instanceProxy = getDocStreamProxy(instance);//为对象配置动态代理
        //进行实例化的缓存，与应用的生命周期一致
        cacheInstance(true, streamEnum, objMap, instanceProxy);//minio
        return instanceProxy;
    }

    /**
     * local本地流操作对象的实例化
     * 实例化完毕后会对其进行缓存，最终放入 {@link DocStreamFactory#instanceMap}中
     *
     * @param streamEnum 对应的枚举状态
     * @param proxy      是否创建动态代理
     * @param objMap     具体实例化对象保存的内容
     * @return 返回实例化完毕的 {@link DocStream}
     */
    private static DocStream localDocStreamInstance(DocStreamEnum streamEnum, boolean proxy, Map<Boolean, DocStream> objMap) {
        LocalDocStreamImpl instance = LocalDocStreamImpl.instance();
        if (!proxy) {
            //进行实例化的缓存，与应用的生命周期一致
            cacheInstance(false, streamEnum, objMap, instance);//minio
            return instance;
        }
        //todo 后续如果实例化类型多了，可以将下方生成实例化代理对象进行抽离拆分
        DocStream instanceProxy = getDocStreamProxy(instance);//为对象配置动态代理
        //进行实例化的缓存，与应用的生命周期一致
        cacheInstance(true, streamEnum, objMap, instanceProxy);//minio
        return instanceProxy;
    }
}
