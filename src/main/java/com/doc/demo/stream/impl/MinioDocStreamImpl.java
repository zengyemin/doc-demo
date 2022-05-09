package com.doc.demo.stream.impl;

import com.doc.demo.enums.DocStreamEnum;
import com.doc.demo.factory.DocStreamFactory;
import com.doc.demo.schedules.FileSchedule;
import com.doc.demo.utils.FileUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.doc.demo.config.DocStreamConfig;
import com.doc.demo.enums.MultiPartStatusEnum;
import com.doc.demo.exception.DocStreamException;
import com.doc.demo.model.stream.param.DocMinioParam;
import com.doc.demo.model.stream.result.minio.DocMinioResult;
import com.doc.demo.model.stream.result.minio.MinioMultiPartResult;
import com.doc.demo.stream.DocStream;
import com.doc.demo.utils.ClassInstanceUtil;
import com.doc.demo.utils.IOUtil;
import com.doc.demo.utils.SpringUtil;
import com.doc.demo.utils.StreamClose;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.util.StringUtils;

/**
 * 图片相关的操作流
 *
 * @author : zengYeMin
 * @date : 2022/4/1 15:17
 **/

public class MinioDocStreamImpl implements DocStream<DocMinioParam, DocMinioResult, MinioMultiPartResult> {

    private static MinioDocStreamImpl minioDocStream;

    private DocStreamConfig.CustomMinioClient client;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public DocMinioResult uploadDoc(@NotNull DocMinioParam param, @NotNull InputStream is) {
        clientInit();//初始化minio客户端
        boolean exist = bucketExist(param.getBucket().getName());
        DocMinioResult result = new DocMinioResult();
        result.setBucket(param.getBucket().getName());
        if (!exist) {
            result.setMessage("无效的桶参数");
            return result;//如果桶不存在则直接返回false
        }
        PutObjectArgs args = PutObjectArgs.builder().bucket(param.getBucket().getName()).object(param.getObjectName())
            .stream(is, param.getObjectSize(), param.getPartSize()).build();
        try {
            ObjectWriteResponse response = client.putObject(args);
            logger.info("minio 上传文件完毕 {}", response.etag());
            result.setVersionId(response.versionId());
            result.setEtag(response.etag());
            result.setBucketSavePath(param);
            result.setSecretKey(param.getSecretKey());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("uploadDoc Exception e:{]", e);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public byte[] downloadDoc(@NotNull DocMinioParam param) {
        clientInit();//初始化minio客户端
        GetObjectArgs args =
            GetObjectArgs.builder().versionId(param.getVersionsId()).bucket(param.getBucket().getName())
                .object(param.getObjectName()).build();
        GetObjectResponse response = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            response = client.getObject(args);
            //将response响应的字节复制到OS输出流
            IOUtil.copy(response, os);
            return os.toByteArray();//转byte数组返回
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("downloadDoc Exception e:{]", e);
        } finally {
            StreamClose.close(response);
        }

        return null;
    }

    @Override
    public MinioMultiPartResult multiPartUpload(@NotNull DocMinioParam param, @Nullable InputStream is) {
        clientInit();//初始化minio客户端
        MinioMultiPartResult result = new MinioMultiPartResult();
        String uploadId = param.getUploadId();
        //uploadId为空则表示需要创建一个新的分段上传
        if (uploadId == null) {
            //minio分段上传的初始化方法,创建时使用
            multiPartUploadInitHandler(param, result);
        } else {
            //分段上传中或者是上传完毕的处理方法
            multiPartNotInitHandler(param, result);
        }

        return result;
    }

    @Override
    public boolean removeDoc(@NotNull DocMinioParam param) {
        clientInit();//初始化minio客户端
        RemoveObjectArgs args =
            RemoveObjectArgs.builder().bucket(param.getBucket().getName()).object(param.getObjectName())
                .versionId(param.getVersionsId()).build();
        try {
            client.removeObject(args);
            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("removeDoc Exception e:{]", e);
        }

        return false;
    }

    @Override
    public String docPreview(@NotNull DocMinioParam param, @NotNull Long expiredTime) {
        // 初始化minio客户端
        clientInit();
        //预览文件目录，当前目录下的文件会根据expiredTime定期删除
        File previewFileDir = getPreviewFileDir(param);
        //获取对应原文件格式的MD5文件名
        String md5FileNameFormat = param.getMD5FileNameFormat();
        String existsPreviewFile = getExistsPreviewFile(previewFileDir, md5FileNameFormat);
        //如果当前预览文件已经存在则直接返回路径
        if (existsPreviewFile != null) {
            return existsPreviewFile;
        }

        //判断是否传入解密参数
        boolean isHasText = StringUtils.hasText(param.getSecretKey());
        //获取实例化Doc操作对象
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, isHasText);
        //下载需要预览的文件
        byte[] bytes = docStream.downloadDoc(param);
        //创建预览文件
        File file = new File(previewFileDir, md5FileNameFormat);
        String filePath = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            //将流写入到文件中
            filePath = FileUtil.inputStreamWriteFile(inputStream, file);
        } catch (IOException e) {
            logger.error("docPreview IOException:{}", e);
        }
        if (filePath != null) {
            FileSchedule.instance().putFileExpiredInfo(filePath, expiredTime);
        }
        return filePath;
    }

    /**
     * 获取已存在预览文件
     *
     * @param previewFileDir 预览文件目录
     * @param md5FileNameFormat md5文件名格式与原文件一致
     * @return 存在则返回具体地址，不存在则返回null
     */
    private String getExistsPreviewFile(File previewFileDir, String md5FileNameFormat) {
        File[] childFiles = previewFileDir.listFiles();
        if (childFiles == null) {
            return null;
        }
        //匹配文件是否已存在预览目录中,匹配成功返回路径，失败返回null
        return Arrays.stream(childFiles).filter(f -> f.getName().contains(md5FileNameFormat)).map(File::getPath)
            .findAny().orElse(null);
    }

    /**
     * 判断当前桶是否存在
     * 如果桶不存在则创建
     *
     * @param bucket 桶的名字
     * @return 存在返回true
     */
    private boolean bucketExist(String bucket) {
        boolean exists = false;
        try {
            exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("bucketExist Exception e:{}", e.getMessage());
        }
        if (exists) {//桶存在直接返回true
            return true;
        }
        //创建新的桶，成功返回true，失败返回false
        try {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            logger.info("当前bucket不存在，为其创建新的桶 bucket:{}", bucket);
            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("创建新的桶失败 bucket:{} ,e:{}", bucket, e);
            return false;
        }
    }

    /**
     * 初始化minio客户端
     */
    private void clientInit() {
        if (this.client != null) {
            return;
        }
        this.client = SpringUtil.getBean(DocStreamConfig.CustomMinioClient.class);
    }

    /**
     * 分段上传中或者是上传完毕的处理方法
     * <p>
     * 1、获取分段上传的临时文件
     * 2、上传完毕则会进行合并minio中的文件{@code chunkCount == listParts.size()}
     * 3、上传中则只会进行设置已使用使用，配置上传状态
     *
     * @param param 上传用的配置信息
     * @param result 处理完毕返回的结果对象
     */
    private void multiPartNotInitHandler(DocMinioParam param, MinioMultiPartResult result) {
        //获取分段上传的临时文件信息
        List<Part> listParts = getListParts(param);
        //设置当前已使用的数量
        result.setUseUploadNum(listParts.size());
        result.setUploadId(param.getUploadId());
        //当前文件被分段的块数
        Integer chunkCount = param.getChunkCount();
        //如果总数量和已使用上传数量一致则表示需要合并了
        if (chunkCount == listParts.size()) {
            //进行合并分段上传，如果合并失败则会抛出异常中断
            mergeMultipartUpload(param, listParts);
            result.setStatusEnum(MultiPartStatusEnum.UPLOAD_FINISH);
        } else {
            result.setStatusEnum(MultiPartStatusEnum.UPLOADING);
        }
    }

    /**
     * minio分段上传的初始化方法，上传ID为空才会使用此方法
     * <p>
     * 1、先获取一个当前文件的上传ID {@code multiPartUploadId}
     * 2、获取根据ID和配置获取minio的分段Url集合 {@code uploadUrlList}
     *
     * @param param 获取URL的配置参数
     * @param result 外部已经实例化的结果对象，最终会返回到最外层
     */
    private void multiPartUploadInitHandler(DocMinioParam param, MinioMultiPartResult result) {
        //分段上传的ID
        String multiPartUploadId = client.getMultiPartUploadId(param);
        result.setUploadId(multiPartUploadId);

        //根据上传的Id和和分段块的数量获取上传的URL
        LinkedList<String> uploadUrlList = getUploadUrlList(param, multiPartUploadId);
        result.setStatusEnum(MultiPartStatusEnum.INITIALIZE);//设置上传状态
        result.setUploadUrlList(uploadUrlList);//上传链接
        result.setUseUploadNum(0);//已上传数量
    }

    /**
     * 根据上传的Id{@code multiPartUploadId} 和分段块的数量获取上传的URL
     * 获取多个分段链接如果有一个出现失败，则会抛出异常中断当前方法
     * 当前做法说为了保证分段的块可以完整的上传到minio
     *
     * @param param 获取URL的配置参数
     * @param multiPartUploadId 通过{@link DocStreamConfig.CustomMinioClient#getMultiPartUploadId(DocMinioParam)}
     * 获取分段上传的ID
     * @return {@link LinkedList<String>}
     */
    private LinkedList<String> getUploadUrlList(DocMinioParam param, String multiPartUploadId) {
        //设置获取分段URL的通用参数
        GetPresignedObjectUrlArgs.Builder argsBuilder =
            GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(param.getBucket().getName())//桶的名字
                .object(param.getObjectName())//对象名字
                .expiry(1, TimeUnit.DAYS);//临时文件过期时间

        //保存获取成功的分段URL
        LinkedList<String> urlList = new LinkedList<>();
        //必须从1开始，minio的分段list是从1开始的，如果起点不是1会造成丢失分段文件
        for (int i = 1; i <= param.getChunkCount(); i++) {
            Multimap<String, String> newMultiMap = HashMultimap.create();
            newMultiMap.put("partNumber", i + "");
            newMultiMap.put("uploadId", multiPartUploadId);
            //绑定参数，当前SDK中build每次引用都会调用constructor.newInstance()创建新对象，所以不会出现参数覆盖的情况
            GetPresignedObjectUrlArgs urlArgs = argsBuilder.extraQueryParams(newMultiMap).build();
            String url;
            try {
                url = client.getPresignedObjectUrl(urlArgs);
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
                throw new DocStreamException("获取分段上传的Url失败 e:%s", e.getMessage());
            }
            urlList.add(url);
        }
        return urlList;
    }

    /**
     * 进行合并分段上传，如果合并失败则会抛出异常中断
     *
     * @param param 合并分段文件的配置信息
     */
    private void mergeMultipartUpload(DocMinioParam param, List<Part> listParts) {
        try {
            //将分段数据合并，并且存放到指定位置
            ObjectWriteResponse response = client.completeMultipartUpload(param, listParts.toArray(new Part[] {}));
            if (response != null) {
                return;
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("mergeMultipartUpload Exception e:{}", e.getMessage());
        }
        String msg = "分段上传数据合并失败，userNick:%s fileName:%s uploadId:%s";
        throw new DocStreamException(msg, param.getUserNick(), param.getFileName(), param.getUploadId());
    }

    /**
     * 获取分段上传的临时文件信息
     * 使用上传ID、桶名、对象名字进行查询
     *
     * @param param 配置参数
     * @return {@link  List<Part>}
     */
    private List<Part> getListParts(DocMinioParam param) {
        try {
            //根据配置信息获取分断上传的临时文件
            ListPartsResponse listPartsResponse = client.listParts(param);
            return listPartsResponse.result().partList();
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            logger.error("bucketExist Exception e:{}", e.getMessage());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 单例创建对象
     *
     * @return {@link MinioDocStreamImpl}
     */
    public static MinioDocStreamImpl instance() {
        if (minioDocStream != null) {
            return minioDocStream;
        }
        synchronized (MinioDocStreamImpl.class) {
            if (minioDocStream != null) {
                return minioDocStream;
            }
            //调用检测方法，判断当前是否可以被创建
            ClassInstanceUtil.docStreamInstanceCheck();//Minio
            minioDocStream = new MinioDocStreamImpl();
        }
        return minioDocStream;
    }

    protected MinioDocStreamImpl() {
    }
}
