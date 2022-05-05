package com.doc.demo.model.stream.param;

import com.doc.demo.config.DocStreamConfig.CustomMinioClient;
import com.doc.demo.enums.MinioBucketEnum;
import com.doc.demo.model.stream.DocParamAbstract;
import com.doc.demo.stream.impl.MinioDocStreamImpl;
import com.doc.demo.exception.DocParamException;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * minio上传的参数配置类
 *
 * @author : zengYeMin
 * @date : 2022/4/1 15:19
 **/
public class DocMinioParam extends DocParamAbstract {

    /**
     * 存储的桶，可以理解为一个根目录
     */
    private MinioBucketEnum bucket;

    /**
     * minio中存储文件格式,例如:ethic/meeting/doc/
     */
    private String bucketPath;

    /**
     * 版本ID
     */
    private String versionsId;

    /**
     * 分片上传时minio使用的ID,如果当前参数为null则表示新建
     */
    private String uploadId;

    /**
     * 文件大小，-1表示不知道文件大小
     */
    private long objectSize = -1;

    /**
     * 文件上限
     */
    private long partSize = 10485760;

    protected DocMinioParam() {
    }

    /**
     * 获取保存在minio中的文件对象名
     * {@code StringUtils.hasText(bucketPath)}成立，格式例如：bucket/bucketPath/userNick_userId/MD5FileName
     * {@code StringUtils.hasText(bucketPath)}不成立，格式例如：bucket/userNick_userId/MD5FileName
     *
     * @return {@link String}
     */
    public String getObjectName() {
        StringBuilder sb = new StringBuilder();
        String bucketPath = getBucketPath();
        if (StringUtils.hasText(bucketPath)) {
            if (bucketPath.endsWith("/")) {
                sb.append(bucketPath);
            } else {
                sb.append(bucketPath).append("/");
            }
        }
        String prefix = sb.append(super.getUserNick()).append("_").append(super.getUserId()).append("/").toString();
        return prefix + super.getMD5FileName();
    }

    public MinioBucketEnum getBucket() {
        return bucket;
    }

    public long getObjectSize() {
        return objectSize;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getBucketPath() {
        return bucketPath;
    }

    private DocMinioParam(Builder builder) {
        super(builder);
        this.bucket = builder.bucket;
        this.bucketPath = builder.bucketPath;
        this.objectSize = builder.objectSize;
        this.partSize = builder.partSize;
        this.versionsId = builder.versionsId;
        this.uploadId = builder.uploadId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getVersionsId() {
        return versionsId;
    }

    public String getUploadId() {
        return uploadId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DocMinioParam{");
        sb.append("bucket='").append(bucket).append('\'');
        sb.append(", objectSize=").append(objectSize);
        sb.append(", partSize=").append(partSize);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder extends ParentBuilder<DocMinioParam.Builder> {

        private MinioBucketEnum bucket;

        private String versionsId;

        private String uploadId;

        private String bucketPath;

        private long objectSize = -1;

        private long partSize = 10485760;

        @Override
        protected void checkVerify() {
            super.parentCheckVerify();
            if (bucket == null) {
                throw new DocParamException("存储Minio不能为空");
            }
            if (!StringUtils.hasText(this.bucketPath)) {
                throw new DocParamException("桶的存储路径不能为空");
            }
        }

        /**
         * 设置要储存的桶
         *
         * @param bucket 对应minio创建的桶
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setBucket(MinioBucketEnum bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * 设置文件的版本信息
         *
         * @param versionsId minio中的版本信息
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setVersionsId(String versionsId) {
            this.versionsId = versionsId;
            return this;
        }

        /**
         * 设置分段上传ID，目前是分片上传才会使用
         * 如果使用分段上传方法{@link MinioDocStreamImpl#multiPartUpload(DocMinioParam, InputStream)}
         * 当前参数为null则会创建一个新的分片上传地址
         * 如果不为null则根据上传ID获取之前所以创建的地址
         *
         * @param uploadId 通过 {@link CustomMinioClient#getMultiPartUploadId(DocMinioParam)}获取
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setUploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        /**
         * 设置储存桶中的路径
         *
         * @param bucketPath 参数例如: ethic/meeting/doc/
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setBucketPath(String bucketPath) {
            this.bucketPath = bucketPath;
            return this;
        }

        /**
         * 设置本次上传对象的大小
         *
         * @param objectSize 默认是-1
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setObjectSize(long objectSize) {
            this.objectSize = objectSize;
            return this;
        }

        /**
         * 本次上传的最大限制设置
         *
         * @param partSize 默认是10485760
         * @return 当前对象 {@link DocMinioParam.Builder}
         */
        public Builder setPartSize(long partSize) {
            this.partSize = partSize;
            return this;
        }

        /**
         * 绑定设置对象的参数
         * 在绑定之前会对部分参数进行检测
         *
         * @return 返回赋值的 {@link DocMinioParam} 对象
         * @see #checkVerify()
         */
        public DocMinioParam build() {
            //进行参数的检查校验
            checkVerify();
            return new DocMinioParam(this);
        }

    }
}
