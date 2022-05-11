package com.doc.demo.controller;

import com.doc.demo.enums.MinioBucketEnum;
import com.doc.demo.repository.jpa.StudentInfoRepository;
import com.doc.demo.enums.DocStreamEnum;
import com.doc.demo.enums.MultiPartFileDeleteEnum;
import com.doc.demo.factory.DocStreamFactory;
import com.doc.demo.model.stream.DocParamAbstract;
import com.doc.demo.model.stream.param.DocLocalParam;
import com.doc.demo.model.stream.param.DocMinioParam;
import com.doc.demo.model.stream.result.local.LocalMultiPartResult;
import com.doc.demo.model.stream.result.minio.MinioMultiPartResult;
import com.doc.demo.properties.DocStreamProperties;
import com.doc.demo.stream.DocStream;
import com.doc.demo.utils.StreamClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/multiPart")
@SuppressWarnings("all")
public class DocMultiPartController {

    private Logger logger = LoggerFactory.getLogger(DocMultiPartController.class);

    @Resource
    private DocStreamProperties properties;

    private DocStream minioDocStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, true);

    private DocStream localDocStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_LOCAL, true);

    public static String secretKey = null;//临时保存一下秘钥

    private String userNick = "zym", userId = "1234", docId = "123456789";

    @Resource
    StudentInfoRepository repository;

    /**
     * 因为没有持久层这边临时做一下缓存机制，保存一些简单上传数据
     */
    private static final Map<String, Map<String, MinioMultiPartResult>> userFileUoloadMinioCache =
        new ConcurrentHashMap<>();

    /**
     * minio的分段上传处理
     *
     * @param fileName 文件名字
     * @param fileMd5 文件的MD5值
     * @param chunkCount 拆分的数量
     * @return {@link ResponseEntity}
     */
    @RequestMapping(value = "minioUpload", method = RequestMethod.POST)
    public ResponseEntity minioUpload(@RequestParam("fileName") String fileName,
        @RequestParam("fileMd5") String fileMd5, @RequestParam("chunkCount") Integer chunkCount) {
        //todo 真实业务场景docMinioParam的创建也是可以被隐藏的，可以在service中进行组装
        //获取文件的上传信息，如果是第一次则不会有上传信息
        MinioMultiPartResult userUploadCache = (MinioMultiPartResult) getUserUploadCache(userId, fileMd5);
        DocMinioParam docMinioParam = DocMinioParam.builder().setBucket(MinioBucketEnum.ETHICS)//桶
            .setBucketPath("zym/doc/")//桶中存放地址
            .setFileMd5(fileMd5)//上传文件的MD5
            .setChunkCount(chunkCount)//分成多少断的参数
            .setFileName(fileName)//文件名
            .setDocId(docId)//文件ID
            .setUploadId(userUploadCache.getUploadId())//上传ID，为null则会去新建
            .setUserNick(userNick)//用户名
            .setUserId(userId).build();
        //开始进行分段上传处理
        MinioMultiPartResult result = (MinioMultiPartResult) minioDocStream.multiPartUpload(docMinioParam, null);
        if (result.getSecretKey() != null) {
            secretKey = result.getSecretKey();
        }
        //如果是断点续传则UploadUrlList会为null，
        //可以理解为同一个文件在没有上传完毕时第二次调用此接口，
        //需要将这个文件第一次进入此方法创建的URL信息赋值到result中
        if (result.getUploadUrlList() == null) {
            result.setUploadUrlList(userUploadCache.getUploadUrlList());
        }
        //用户进行分段上传时的缓存，这些是模拟持久层保存数据才这么做的
        putUserUploadCache(userId, fileMd5, result);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "localUpload", method = RequestMethod.POST)
    public ResponseEntity localUpload(@RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam("fileName") String fileName, @RequestParam("fileMd5") String fileMd5,
        @RequestParam("chunkCount") Integer chunkCount) throws InterruptedException {
        Thread.sleep(600);//todo 这个是因为本地测试速度太快了，进行限制一下
        //        LocalMultiPartResult userUploadCache = (LocalMultiPartResult) getUserUploadCache(userId, fileMd5);
        DocLocalParam localParam = DocLocalParam.builder().secretKey()//加密
            .setUserNick(userNick)//用户名
            .setUserId(userId)//用户ID
            .setFileName(fileName)//文件名
            .setDocId(docId)//文件ID
            .setFileMd5(fileMd5)//上传文件的MD5
            .setChunkCount(chunkCount)//文件拆分的数量
            .setMultiPartRootPath(properties.getMultiPartTempPath())//设置分段文件存储的根路径
            .setRootPath("E:\\fileUploadRoot")//上传文件的保存根路径
            .setFilePath("ethic\\doc")//文件的具体路径
            .setDeleteEnum(MultiPartFileDeleteEnum.FINISH_DELETE)//设置分片文件删除状态
            .build();
        InputStream is = null;
        try {
            if (file != null) {
                is = file.getInputStream();
            }
            LocalMultiPartResult result = (LocalMultiPartResult) localDocStream.multiPartUpload(localParam, is);
            if (result.getSecretKey() != null) {
                secretKey = result.getSecretKey();
            }
            //用户进行分段上传时的缓存，这些是模拟持久层保存数据才这么做的
            //            putUserUploadCache(userId, fileMd5, result);
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.OK);
        } finally {
            StreamClose.close(is);
        }

    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ResponseEntity download(@RequestParam("fileName") String fileName) throws UnsupportedEncodingException {
        String userNick = "zym", userId = "12345";
        DocLocalParam localParam = DocLocalParam.builder().secretKey(secretKey)//加密
            .setUserNick(userNick).setUserId(userId).setFileName(fileName)//文件名
            .setDocId(docId)//文件ID
            .setRootPath("E:\\fileUploadRoot")//上传文件的保存根路径
            .setFilePath("ethic\\doc")//文件的具体路径
            .build();
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_LOCAL, true);
        //执行下载，如果文件需要解密则必须在调用下载之前设置解密key
        byte[] bytes = docStream.downloadDoc(localParam);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);//响应为流
        //如果文件中有较多特殊字符可能会导致格式有问题
        fileName = URLEncoder.encode("下载_" + fileName, "UTF-8");
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "removeMinio", method = RequestMethod.GET)
    public ResponseEntity remove(@RequestParam("fileName") String fileName) {
        DocMinioParam docMinioParam = DocMinioParam.builder().setBucket(MinioBucketEnum.ETHICS)//桶
            .setBucketPath("zym/doc/")//桶中存放地址
            .setFileName(fileName)//文件名
            .setDocId(docId)//文件ID
            .setUserNick(userNick).setUserId(userId).build();
        boolean b = minioDocStream.removeDoc(docMinioParam);
        return new ResponseEntity(b ? "删除成功" : "删除失败", HttpStatus.OK);
    }

    /**
     * 用户进行分段上传时的上传ID缓存
     *
     * @param userId 用户ID
     * @param fileMd5 文件的MD5值
     * @param uploadId 上传ID
     */
    private void putUserUploadCache(String userId, String fileMd5, MinioMultiPartResult result) {
        Map<String, MinioMultiPartResult> map = new HashMap<>();
        map.put(fileMd5, result);
        userFileUoloadMinioCache.put(userId, map);
    }

    /**
     * 获取用户的上传ID
     *
     * @param userId 操作用户的ID
     * @param fileMd5 文件的MD5值
     * @return {@link DocParamAbstract}
     */
    private MinioMultiPartResult getUserUploadCache(String userId, String fileMd5) {
        Map<String, MinioMultiPartResult> stringStringMap = userFileUoloadMinioCache.get(userId);
        if (stringStringMap == null) {
            return new MinioMultiPartResult();
        }
        MinioMultiPartResult result = stringStringMap.get(fileMd5);
        return result == null ? new MinioMultiPartResult() : result;
    }
}
