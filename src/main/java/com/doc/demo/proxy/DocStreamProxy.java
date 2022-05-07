package com.doc.demo.proxy;

import com.doc.demo.exception.DocParamException;
import com.doc.demo.model.stream.DocParamAbstract;
import com.doc.demo.stream.DocStream;
import com.doc.demo.utils.DocStreamUtil;
import com.doc.demo.utils.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.CipherInputStream;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyException;

public class DocStreamProxy implements InvocationHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DocStream stream;
    private DocStreamUtil docStreamUtil = DocStreamUtil.instance();

    public DocStreamProxy(DocStream stream) {
        this.stream = stream;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();

        Object invoke = null;
        //对上传进行处理
        if ("uploadDoc".equals(name)) {
            logger.info("即将开始执行{}", name);
            //如果md5FileName为null则表示参数异常,DocParamException会中断操作
            String secretKey = getSecretKey(args);
            //上传文件时，对传入的流参数进行加密处理
            invoke = uploadEncrypt(secretKey, method, args);
            logger.info("{}执行结束");
        }
        //对下载进行处理
        if ("downloadDoc".equals(name)) {
            logger.info("即将开始执行{}", name);
            //如果md5FileName为null则表示参数异常,DocParamException会中断操作
            String secretKey = getSecretKey(args);
            //下载文件时对返回的IO进行解密处理方法
            invoke = downloadDecrypt(secretKey, method, args);
            logger.info("{}执行结束", name);
        }
        return invoke == null ? method.invoke(stream, args) : invoke;
    }

    /**
     * 获取文件加密的key
     *
     * @param args 代理方法的参数数组
     * @return {@link String}
     */
    private String getSecretKey(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof DocParamAbstract) {
                return ((DocParamAbstract) arg).getSecretKey();
            }
        }
        throw new DocParamException("无效的MD5文件名字");
    }


    /**
     * 下载文件时对返回的IO进行解密处理方法
     *
     * @param secretKey 文件解密KEY {@link DocParamAbstract#getSecretKey()}} 中获取
     * @param method    代理执行的方法
     * @param args      方法中的传递参数
     * @return 执行完毕后的对象
     */
    private Object downloadDecrypt(String secretKey, Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException, KeyException, IOException {
        //secretKey为null则表示当前文件没有设置解密需求
        if (secretKey == null) return method.invoke(stream, args);
        Object invoke = method.invoke(stream, args);

        try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) invoke);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            //调用解密方法
            CipherInputStream cipherInputStream = docStreamUtil.decryptStream(bis, secretKey);
            IOUtil.copy(cipherInputStream, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("download IOException:{}", e.getMessage());
            if (e.getSuppressed() != null && e.getMessage().contains("Such issues can arise if a bad key")) {
                throw new KeyException("解密失败，请检查你的公钥或私钥！！");
            }
            throw e;
        }
    }

    /**
     * 上传文件时，对传入的流参数进行加密处理
     *
     * @param secretKey 文件加密KEY {@link DocParamAbstract#getSecretKey()}} 中获取
     * @param method    代理执行的方法
     * @param args      方法中的传递参数
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object uploadEncrypt(String secretKey, Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException {
        //secretKey为null则表示当前文件没有设置加密需求
        if (secretKey == null) return method.invoke(stream, args);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof InputStream) {
                //对流进行加密
                args[i] = docStreamUtil.encryptStream((InputStream) args[i], secretKey);
            }
        }
        return method.invoke(stream, args);
    }

    private DocStreamProxy() {
    }

}
