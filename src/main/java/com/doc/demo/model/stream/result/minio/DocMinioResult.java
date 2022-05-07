package com.doc.demo.model.stream.result.minio;

import com.doc.demo.model.stream.param.DocMinioParam;
import com.doc.demo.model.stream.DocResultAbstract;
import org.springframework.util.StringUtils;

import java.util.Date;

public class DocMinioResult extends DocResultAbstract {
    private String bucket;
    private String bucketSavePath;
    private String versionId;//版本ID
    private String etag;//桶中标签

    public DocMinioResult() {
        super(new Date());
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getBucketSavePath() {
        return bucketSavePath;
    }

    public void setBucketSavePath(String bucketSavePath) {
        this.bucketSavePath = bucketSavePath;
    }

    public void setBucketSavePath(DocMinioParam param) {
        this.bucketSavePath = param.getObjectName();
    }

    @Override
    public boolean isSuccess() {
        return !StringUtils.hasText(bucketSavePath) &&
                !StringUtils.hasText(etag) &&
                !StringUtils.hasText(bucket);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DocMinioResult{");
        sb.append("bucket='").append(bucket).append('\'');
        sb.append(", bucketSavePath='").append(bucketSavePath).append('\'');
        sb.append(", versionId='").append(versionId).append('\'');
        sb.append(", etag='").append(etag).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
