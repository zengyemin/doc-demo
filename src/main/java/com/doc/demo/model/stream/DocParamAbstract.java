package com.doc.demo.model.stream;

import com.doc.demo.utils.MD5;
import com.doc.demo.exception.DocParamException;
import com.doc.demo.model.stream.param.DocLocalParam;
import com.doc.demo.model.stream.param.DocMinioParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;


import java.util.UUID;


/**
 * @author : zengYeMin
 * @date : 2022/3/31 18:20
 **/
public abstract class DocParamAbstract {
    //保存文件的操作用户名,或者是下载文件操作的用户名
    private String userNick;
    //保存文件的操作用户ID,或者是下载文件操作的用户ID
    private String userId;
    //当前操作的文件ID，建议使用数据库表中主键ID，用于上传和查询使用
    private String docId;
    //文件的MD5值
    private String fileMd5;
    //用于当前文件加密使用，为null则表示不加密
    private String secretKey;
    //需要被保存的文件名字
    private String fileName;
    //分片的数量
    private Integer chunkCount;

    protected DocParamAbstract() {
    }

    protected DocParamAbstract(DocParamAbstract.ParentBuilder builder) {
        this.userNick = builder.userNick;
        this.userId = builder.userId;
        this.docId = builder.docId;
        this.secretKey = builder.secretKey;
        this.fileName = builder.fileName;
        this.fileMd5 = builder.fileMd5;
        this.chunkCount = builder.chunkCount;
    }

    public String getUserNick() {
        return userNick;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDocId() {
        return docId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    /**
     * 当前上传文件多个参数拼接，然后进行MD5加密的文件名
     * <p>
     * 1、拼接用户名，不能为空 {@link #getUserNick()}
     * 2、拼接用户ID，不能为空 {@link #getUserId()}
     * 3、拼接文件ID，不能为空  {@link #getDocId()}
     *
     * @return 格式例如：04178787C1680E079A00B1A8C202E221.ES
     */
    public final String getMD5FileName() {
        String sb = new StringBuilder(this.getUserId())
                .append(this.getFileName())
                .append(this.getDocId()).toString();
        final String ENCRYPT_SUFFIX = ".ES";
        return MD5.getInstance().getMD5ofStr32(sb.getBytes()) + ENCRYPT_SUFFIX;
    }
    
    /**
     * 其它类只有继承了 {@link DocParamAbstract.ParentBuilder} 才可使用
     * 加上{@link SuppressWarnings}的原因是泛型会进行转型警告
     * 当前类只会对子类开放，转型不会出现问题，所以加上{@link SuppressWarnings}
     */
    @SuppressWarnings("unchecked")
    protected static abstract class ParentBuilder<P extends ParentBuilder> {
        private String userNick;//操作用户名
        private String userId;//用户ID
        private String docId;//当前操作的ID，用于上传和查询使用
        private String secretKey;//加密参数
        private String fileName;//文件名字
        private String fileMd5;
        private Integer chunkCount;

        /**
         * 下级子类实现这个抽象方法，用于做检查验证的自定义扩展
         * 例如{@link DocMinioParam#getBucket()}存储桶是不能为空的
         * 例如{@link DocLocalParam#getRootPath()} ()}存储位置是不能为空的
         */
        protected abstract void checkVerify();

        /**
         * 为当前类的必要参数检验，当前方法加上 {@code final} 修饰禁止重写
         */
        protected final void parentCheckVerify() {
            if (!StringUtils.hasText(this.fileName)) throw new DocParamException("文件名字不能为空");
            if (!StringUtils.hasText(this.docId)) throw new DocParamException("文件ID不能为空");
            if (!StringUtils.hasText(this.userId)) throw new DocParamException("用户id不能为空");
        }

        public P setUserNick(String userNick) {
            this.userNick = userNick;
            return (P) this;
        }


        public P setUserId(String userId) {
            this.userId = userId;
            return (P) this;
        }


        public P setDocId(String docId) {
            this.docId = docId;
            return (P) this;
        }

        /**
         * 设置文件加密的key,如果参数为null则表示不进行加密
         *
         * @return 返回 {@link ParentBuilder}的子类
         */
        public P secretKey() {
            this.secretKey = UUID.randomUUID().toString();
            return (P) this;
        }

        /**
         * 设置文件的加密key，主动设置的情况下不能为null
         *
         * @param secretKey 自定义加密参数
         * @return 返回 {@link ParentBuilder}的子类
         */
        public P secretKey(@NotNull String secretKey) {
            this.secretKey = secretKey;
            return (P) this;
        }


        public P setFileName(String fileName) {
            this.fileName = fileName;
            return (P) this;
        }

        public P setFileMd5(@NotNull String fileMd5) {
            this.fileMd5 = fileMd5;
            return (P) this;
        }

        public P setChunkCount(Integer chunkCount) {
            this.chunkCount = chunkCount;
            return (P) this;
        }
    }

}



