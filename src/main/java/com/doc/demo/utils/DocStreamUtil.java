package com.doc.demo.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;

/**
 * 流的加工处理工具类,目前有的方法如下
 * <p>
 * 1、根据文件的路径加密文件，返回类型 {@link CipherInputStream}
 * 2、根据一个IO流加密文件，返回类型 {@link CipherInputStream}
 * 3、根据文件的路径解密文件，返回类型 {@link CipherInputStream}
 * 4、根据一个IO流解密文件，返回类型 {@link CipherInputStream}
 *
 * @author : zengYeMin
 * @date : 2022/4/2 17:09
 **/
public class DocStreamUtil {

    private final Logger logger = LoggerFactory.getLogger(DocStreamUtil.class);

    private static DocStreamUtil docStreamUtil;

    private final String publicSecretKey = "";

    /**
     * 传入一个文件路径和一个加密字符，进行加密处理
     *
     * @param fileAddress 要加密的文件地址例如 {@code fileAddress=D:\DELL\Desktop\1234}
     * @param secretKey   加密参数例如{@code secretKey=ASDASD565ads12aeq}
     * @return 返回一个加密的流 {@link CipherInputStream}
     */
    public CipherInputStream encryptStream(String fileAddress, String secretKey) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(fileAddress);
            return encryptStream(is, secretKey);
        } catch (FileNotFoundException e) {
            logger.error("encryptStream FileNotFoundException:{}", e.fillInStackTrace());
        } finally {
            StreamClose.close(is);
        }
        return null;
    }

    /**
     * 传入一个输入流和一个加密字符，使用DES算法，进行加密处理
     *
     * @param is        输入流，当前流内部不会进行关闭，外部传入时注意流的关闭 {@code is}
     * @param secretKey 加密参数例如{@code secretKey=ASDASD565ads12aeqasda}
     * @return 返回一个加密的流 {@link CipherInputStream}
     */
    public CipherInputStream encryptStream(InputStream is, String secretKey) {
        //根据strKey参数生成一个key
        Key key = getKey(secretKey);
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new CipherInputStream(is, cipher);
        } catch (NoSuchAlgorithmException e) {
            logger.error("encryptStream FileNotFoundException:{}", e.fillInStackTrace());
        } catch (GeneralSecurityException e) {
            logger.error("encryptStream GeneralSecurityException:{}", e.fillInStackTrace());
        }
        return null;
    }

    /**
     * 传入文件路径和加密字符，使用DES算法进行解密处理
     *
     * @param fileAddress 要解密的文件地址例如 {@code fileAddress=D:\DELL\Desktop\1234}
     * @param secretKey   加密参数例如{@code secretKey=ASDASD565ada72aeqasda}
     * @return 返回一个加密的流 {@link CipherInputStream}
     */
    public CipherInputStream decryptStream(String fileAddress, String secretKey) {
        //根据strKey参数生成一个key
        Key key = getKey(secretKey);
        InputStream is = null;
        try {
            is = new FileInputStream(fileAddress);
            return decryptStream(is, secretKey);
        } catch (FileNotFoundException e) {
            logger.error("decryptStream FileNotFoundException:{}", e.fillInStackTrace());
        } finally {
            StreamClose.close(is);
        }
        return null;
    }

    /**
     * 传入一个输入流和一个加密字符，使用DES算法进行解密处理
     *
     * @param is        输入流，当前流内部不会进行关闭，外部传入时注意流的关闭 {@code is}
     * @param secretKey 加密参数例如{@code secretKey=ASDASD565ads12aeqasda}
     * @return 返回一个加密的流 {@link CipherInputStream}
     */
    public CipherInputStream decryptStream(InputStream is, String secretKey) {
        //根据strKey参数生成一个key
        Key key = getKey(secretKey);
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new CipherInputStream(is, cipher);
        } catch (NoSuchAlgorithmException e) {
            logger.error("encryptStream FileNotFoundException:{}", e.fillInStackTrace());
        } catch (GeneralSecurityException e) {
            logger.error("encryptStream GeneralSecurityException:{}", e.fillInStackTrace());
        }
        return null;
    }

    /**
     * 传入一个Object对象，并且将其转换为一个byte数组
     *
     * @param obj 传入的对象，请保证obj是一个可以进行读写操作的
     * @return 处理成功返回 {@code byte[]}
     * @throws ClassCastException 处理失败
     */
    public byte[] objectToByteArray(@NotNull Object obj) {
        byte[] bytes = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("objectToByteArray IOException:{]", e.getMessage());
            throw new ClassCastException("【objectToByteArray】当前Object无法转换成byte数组，请检查传入的Object");
        }
    }

    /**
     * 根据strKey参数生成 {@link Key} 对象
     *
     * @param strKey 参数格式可以尽可能的随机例如 {@code strKey=sadasd4a65dasdcafASDA}
     * @return 返回一个根据strKey参数生成的 {@link Key} 对象
     */
    private Key getKey(String strKey) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            //安全算法SHA1PRNG
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(strKey.getBytes());
            generator.init(56, random);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成加密的Key对象失败 getKey NoSuchAlgorithmException:{} ", e);
        }
    }

    private Key initKeyForAES(String key) throws NoSuchAlgorithmException {
        if (null == key || key.length() == 0) {
            throw new NullPointerException("key not is null");
        }
        SecretKeySpec key2 = null;
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(key.getBytes());
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            key2 = new SecretKeySpec(enCodeFormat, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException();
        }
        return key2;
    }

    public static DocStreamUtil instance() {
        if (docStreamUtil != null) {
            return docStreamUtil;
        }
        synchronized (DocStreamUtil.class) {
            if (docStreamUtil != null) {
                return docStreamUtil;
            }
            docStreamUtil = new DocStreamUtil();
        }
        return docStreamUtil;
    }

    private DocStreamUtil() {
    }
}

