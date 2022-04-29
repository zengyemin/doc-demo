package com.doc.demo;

import com.doc.demo.utils.StreamClose;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

class FileTest {
    private Key key;

    public FileTest(String str) {
        getKey(str);//生成密匙
    }

    /**
     * 根据参数生成KEY
     */
    public void getKey(String strKey) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            generator.init(new SecureRandom(strKey.getBytes()));
            this.key = generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param file     要加密的文件 如c:/test/srcFile.txt
     * @param destFile 加密后存放的文件名 如c:/加密后文件.txt
     */
    public void encrypt(String file, String destFile) {
        InputStream is = null;
        OutputStream out = null;
        CipherInputStream cis = null;
        try {
            is = new FileInputStream(file);
            out = new FileOutputStream(destFile);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, this.key);
            cis = new CipherInputStream(is, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } finally {
            StreamClose.close(cis, out, is);
        }


    }

    /**
     * 文件采用DES算法解密文件
     *
     * @param file 已加密的文件 如c:/加密后文件.txt
     *             * @param destFile
     *             解密后存放的文件名 如c:/ test/解密后文件.txt
     */
    public void decrypt(String file, String dest) {
        InputStream is = null;
        OutputStream out = null;
        CipherOutputStream cos = null;
        try {
            is = new FileInputStream(file);
            out = new FileOutputStream(dest);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, this.key);
            cos = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamClose.close(cos, out, is);
        }


    }

    public static void main(String[] args) {

        for (int j = 0; j < 1000; j++) {
            int i = new Random().nextInt(6);
            System.out.println("i = " + i);
        }
        System.out.println("(0%2==0) = " + (0%2==0));
        // File file = new File("E:\\temp\\730b5954a2cd50661048f4d943ba959a");
        // List<String> partFiles = Arrays.stream(file.listFiles()).sorted((o1, o2) -> {
        //     String o1Name = o1.getName();
        //     Integer o1FileNum = Integer.valueOf(o1Name.substring(0, o1Name.indexOf(".")));
        //     String o2Name = o2.getName();
        //     Integer o2FileNum = Integer.valueOf(o2Name.substring(0, o2Name.indexOf(".")));
        //     return o1FileNum.compareTo(o2FileNum);
        // }).map(File::getName).collect(Collectors.toList());
        // System.out.println("partFiles = " + partFiles);

    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;

        int n;
        while (-1 != (n = input.read(buffer))) {
            count += n;
            output.write(buffer, 0, n);
        }
        return count;
    }

}
