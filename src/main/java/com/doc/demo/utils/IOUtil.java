package com.doc.demo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author : zengYeMin
 * @date : 2022/4/21 16:55
 **/
public class IOUtil {
    /**
     * 将传入的输入流{@code input} 复制到传入的输出流中{@code output}
     * <p>
     * 请确保输出流{@code output}是初始化状态
     *
     * @param input  传入输入流 {@link InputStream}
     * @param output 传入输出流 {@link OutputStream}
     * @return 流的字节大小 {@link long}
     * @throws IOException 不可控行为需要对异常进行处理
     */
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
