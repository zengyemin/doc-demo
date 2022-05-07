package com.doc.demo.enums;

import com.doc.demo.factory.DocStreamFactory;

/**
 * 当前枚举用于工厂中创建实例化对象使用
 *
 * @see DocStreamFactory#getDocStreamInstance(DocStreamEnum, boolean)
 */
public enum DocStreamEnum {
    DOC_MINIO,
    DOC_LOCAL
}
