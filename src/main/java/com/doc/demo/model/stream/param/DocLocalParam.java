package com.doc.demo.model.stream.param;

import com.doc.demo.enums.MultiPartFileDeleteEnum;
import com.doc.demo.model.stream.DocParamAbstract;
import com.doc.demo.exception.DocParamException;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * @author : zengYeMin
 * @date : 2022/3/31 18:24
 **/
public class DocLocalParam extends DocParamAbstract {
    //存放文件的根路径
    private String rootPath;
    //文件具体的存放位置，需要确保路径真实有效
    private String filePath;
    //存放分段文件的根节点
    private String multiPartRootPath;
    //临时文件删除状态，1为合并后直接删除，2为定时删除
    private MultiPartFileDeleteEnum deleteEnum;

    public String getRootPath() {
        return rootPath;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * 获取保存文件的路径
     * {@code StringUtils.hasText(filePath)} 成立，格式例如：filePath/userNic_userId
     * {@code StringUtils.hasText(filePath)} 不成立，格式例如：userNic_userId
     *
     * @return {@link String}
     */
    public final String getSaveDirFilePath() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(filePath)) {
            if (filePath.endsWith(File.separator)) {
                sb.append(filePath);
            } else {
                sb.append(filePath).append(File.separator);
            }
        }
        return sb.append(super.getUserNick()).append("_").append(super.getUserId()).toString();
    }

    public String getMultiPartRootPath() {
        return multiPartRootPath;
    }

    public MultiPartFileDeleteEnum getDeleteEnum() {
        return deleteEnum;
    }

    public static DocLocalParam.Builder builder() {
        return new DocLocalParam.Builder();
    }

    private DocLocalParam(DocLocalParam.Builder builder) {
        super(builder);
        this.rootPath = builder.rootPath;
        this.filePath = builder.filePath;
        this.multiPartRootPath = builder.multiPartRootPath;
        this.deleteEnum = builder.deleteEnum;
    }

    public static class Builder extends ParentBuilder<DocLocalParam.Builder> {
        private String rootPath;
        private String filePath;
        private String multiPartRootPath;
        private MultiPartFileDeleteEnum deleteEnum;

        /**
         * 设置文件存储的根节点，当前根路径请确保是真实存在的
         * 合并的文件都会存放在当前根节点中
         *
         * @param rootPath 文件夹路径，格式例如
         *                 Windows： E:\fileUploadRoot
         *                 Linux：/opt/fileUploadRoot
         * @return {@link Builder}
         */
        public Builder setRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        /**
         * 设置文件存储的具体路径
         * 当前路径属于{@code rootPath}的下级路径
         * 当前参数不进行设置则文件会直接存放于{@code rootPath}根路径
         *
         * @param filePath 文件夹路径，格式例如
         *                 Windows：ethic\doc
         *                 Linux：ethic/doc
         * @return {@link Builder}
         */
        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        /**
         * 设置分段文件存储的根路径
         * 当前文件的下级子节点在分段文件合并后，会一并删除
         *
         * @param multiPartRootPath 文件夹路径
         * @return {@link Builder}
         */
        public Builder setMultiPartRootPath(String multiPartRootPath) {
            this.multiPartRootPath = multiPartRootPath;
            return this;
        }

        /**
         * 设置分配上传临时文件删除状态，目前分为三种
         * 1、完毕后删除
         * 2、定时机制删除
         * 3、不进行删除
         *
         * @param deleteEnum 配置的枚举状态
         * @return {@link Builder}
         */
        public Builder setDeleteEnum(MultiPartFileDeleteEnum deleteEnum) {
            this.deleteEnum = deleteEnum;
            return this;
        }

        @Override
        protected void checkVerify() {
            super.parentCheckVerify();
            if (!StringUtils.hasText(this.rootPath)) throw new DocParamException("文件存储根路径不能为空");

        }

        /**
         * 绑定设置对象的参数
         * 在绑定之前会对部分参数进行检测
         *
         * @return 返回赋值的 {@link DocLocalParam} 对象
         * @see #checkVerify()
         */
        public DocLocalParam build() {
            //进行参数的检查校验
            checkVerify();
            return new DocLocalParam(this);
        }
    }
}
