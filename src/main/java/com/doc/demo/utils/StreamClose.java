package com.doc.demo.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author : zengYeMin
 * @date : 2022/4/2 16:25
 **/
public class StreamClose {

    public static <T extends Closeable> void close(T... streams) {
        if (streams == null) return;
        for (T stream : streams) {
            if (stream == null) continue;
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
