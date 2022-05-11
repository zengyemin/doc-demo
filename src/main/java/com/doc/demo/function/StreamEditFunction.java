package com.doc.demo.function;

import com.doc.demo.model.stream.DocParamAbstract;

import com.doc.demo.stream.DocStream;
import java.io.InputStream;
import java.util.List;

/**
 * IO流的编辑函数式接口
 * 1、接口用于配合流的上传 {@link DocStream#uploadDoc(DocParamAbstract, InputStream)}
 * 2、接口用于配合流的下载 {@link DocStream#downloadDoc(DocParamAbstract)}
 *
 * @author : zengYeMin
 * @date : 2022/4/2 8:55
 **/
@FunctionalInterface
public interface StreamEditFunction<S extends InputStream> {

    S edit();
}
