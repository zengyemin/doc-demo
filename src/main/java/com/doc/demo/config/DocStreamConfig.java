package com.doc.demo.config;

import com.doc.demo.properties.DocStreamProperties;
import com.doc.demo.exception.DocStreamException;
import com.doc.demo.model.stream.param.DocMinioParam;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.errors.*;
import io.minio.messages.Part;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author : zengYeMin
 * @date : 2022/3/30 15:09
 **/
@Configuration
public class DocStreamConfig {
    @Resource
    private DocStreamProperties properties;

    @Bean
    public CustomMinioClient minioClient() {
        //如果为false则表示不需要进行bean初始化.
        if (!properties.isInitMinio()) return null;
        if (!StringUtils.hasText(properties.getUrl())) {
            throw new BeanInstantiationException(CustomMinioClient.class, "无效的URl");
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new BeanInstantiationException(CustomMinioClient.class, "无效的SecretKey");
        }
        if (!StringUtils.hasText(properties.getAccessKey())) {
            throw new BeanInstantiationException(CustomMinioClient.class, "无效的AccessKey");
        }
        if (properties.getPort() <= 0) {
            throw new BeanInstantiationException(CustomMinioClient.class, "无效的端口");
        }
        MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getUrl(), properties.getPort(), false)
                .credentials(properties.getAccessKey(), properties.getSecretKey()).build();

        return new CustomMinioClient(minioClient);
    }


    public static class CustomMinioClient extends MinioClient {

        protected CustomMinioClient(MinioClient client) {
            super(client);
        }

        /**
         * 获取分配上传的ID
         *
         * @param param 配置的参数
         * @return 分片上传的ID
         */
        public String getMultiPartUploadId(DocMinioParam param) {
            try {
                CreateMultipartUploadResponse multipartUpload = super.createMultipartUpload(param.getBucket().getName(),
                        null, param.getObjectName(), null, null);
                return multipartUpload.result().uploadId();
            } catch (ErrorResponseException | InsufficientDataException | InternalException |
                    InvalidKeyException | InvalidResponseException | IOException |
                    NoSuchAlgorithmException | ServerException | XmlParserException e) {
                throw new DocStreamException("获取分配上传的ID e:%s", e.getMessage());
            }

        }

        /**
         * 通过组装之前上传的部分来完成分段上传
         * 如果CompleteMultipartUpload失败，需要做好重试失败的处理
         *
         * @param param 配置的参数
         * @param parts 需要被合并的端
         * @return 分段上传响应对象
         */
        public ObjectWriteResponse completeMultipartUpload(DocMinioParam param, Part[] parts) throws ServerException,
                InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException,
                XmlParserException, InvalidResponseException, InternalException {
            return super.completeMultipartUpload(param.getBucket().getName(), null, param.getObjectName(),
                    param.getUploadId(), parts, null, null);
        }

        /**
         * 列出已为特定分段上传上传的部分。此操作必须包含上传 ID
         * 返回的默认零件数为 1,000个
         * 可以通过指定 {@code maxParts}请求参数来限制返回的部件数量
         *
         * @param param param 配置的参数
         */
        public ListPartsResponse listParts(DocMinioParam param) throws ServerException, InsufficientDataException,
                ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException,
                InvalidResponseException, InternalException {
            return super.listParts(param.getBucket().getName(), region, param.getObjectName(), null,
                    null, param.getUploadId(), null, null);
        }

    }

}
