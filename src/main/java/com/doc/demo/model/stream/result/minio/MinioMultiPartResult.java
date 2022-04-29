package com.doc.demo.model.stream.result.minio;

import java.util.Date;
import java.util.LinkedList;

/**
 * @author : zengYeMin
 * @date : 2022/4/8 18:09
 **/
public class MinioMultiPartResult extends MultiPartResult {
    //本次分片上传的ID
    private String uploadId;
    //创建成功后提供分段的minio上传链接
    private LinkedList<String> uploadUrlList;


    public MinioMultiPartResult() {
        super(new Date());
    }


    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public LinkedList<String> getUploadUrlList() {
        return uploadUrlList;
    }

    public void setUploadUrlList(LinkedList<String> uploadUrlList) {
        this.uploadUrlList = uploadUrlList;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultiPartResult{");
        sb.append("statusEnum=").append(super.getStatusEnum());
        sb.append(", uploadId='").append(uploadId).append('\'');
        sb.append(", uploadUrlList=").append(uploadUrlList);
        sb.append(", useUploadNum=").append(super.getUseUploadNum());
        sb.append('}');
        return sb.toString();
    }
}
