package com.doc.demo.model.stream.result.local;

import com.doc.demo.model.stream.result.minio.MultiPartResult;

import java.util.Date;

/**
 * @author : zengYeMin
 * @date : 2022/4/8 18:17
 **/
public class LocalMultiPartResult extends MultiPartResult {
    //合并文件存储地址
    private String mergeFilePath;
    //分段断文件临时存储地址
    private String md5FileTempPath;

    public LocalMultiPartResult() {
        super(new Date());
    }

    public String getMd5FileTempPath() {
        return md5FileTempPath;
    }

    public void setMd5FileTempPath(String md5FileTempPath) {
        this.md5FileTempPath = md5FileTempPath;
    }

    public String getMergeFilePath() {
        return mergeFilePath;
    }

    public void setMergeFilePath(String mergeFilePath) {
        this.mergeFilePath = mergeFilePath;
    }

    @Override
    protected boolean isSuccess() {
        return false;
    }
}
