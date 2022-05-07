package com.doc.demo.controller;

import com.doc.demo.enums.DocStreamEnum;
import com.doc.demo.enums.MinioBucketEnum;
import com.doc.demo.factory.DocStreamFactory;
import com.doc.demo.model.stream.param.DocMinioParam;
import com.doc.demo.model.stream.result.minio.DocMinioResult;
import com.doc.demo.stream.DocStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URLEncoder;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author : zengYeMin
 * @date : 2022/3/30 16:28
 **/
@RestController
@RequestMapping("doc")
@SuppressWarnings("all")//压制一下类型转换的警告
public class DocOperateController {

    private Logger logger = LoggerFactory.getLogger(DocOperateController.class);

    private final String userNick = "zym", userId = "1234",secretKey = "d08763ec-1a2f-4836-b1ef-16f3a5d77858";
    //    @Resource
    //    private DocStreamProperties properties;

    @PostMapping("upload")
    public ResponseEntity uploadImage(@RequestParam(value = "file") MultipartFile file) {
        try (ByteArrayInputStream fis = new ByteArrayInputStream(file.getBytes())) {
            DocMinioParam docMinioParam = DocMinioParam.builder().secretKey(secretKey)//设置加密
                .setBucket(MinioBucketEnum.ETHICS)//存入的桶
                .setBucketPath("zym/doc/")//桶中的路径
                .setFileName(file.getOriginalFilename())//文件名字
                .setDocId("123456789")//文件唯一表示ID
                .setUserNick(userNick)//操作用户
                .setUserId(userId)//操作用户ID
                .build();
            //执行下载，如果文件需要加密则必须在调用上传之前设置解密key
            DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, true);
            //执行minio上传
            DocMinioResult result = (DocMinioResult) docStream.uploadDoc(docMinioParam, fis);
            //            Map<String, String> multiPartTempPathMap = properties.getMultiPartTempPathMap();
            System.out.println("getEncryptKey:" + result.getSecretKey());
            boolean success = result.isSuccess();
            System.out.println("success = " + success);
        } catch (FileNotFoundException e) {
            return new ResponseEntity("failed", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return new ResponseEntity("failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity("success", HttpStatus.OK);
    }

    @GetMapping("download")
    public ResponseEntity download(@RequestParam("fileName") String fileName) throws UnsupportedEncodingException {
        //        RandomAccessFile file = new RandomAccessFile();
        DocMinioParam docMinioParam = DocMinioParam.builder().secretKey(secretKey)//设置解密
            .setBucket(MinioBucketEnum.ETHICS)//桶的名字
            .setBucketPath("zym/doc/")//桶中存放的具体路径
            .setFileName(fileName)//文件名
            .setDocId("123456789")//文件唯一标识ID
            .setUserNick(userNick)//用户名
            .setUserId(userId)//用户ID
            .build();
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, true);
        //执行下载，如果文件需要解密则必须在调用下载之前设置解密key
        byte[] bytes = docStream.downloadDoc(docMinioParam);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);//响应为流
        fileName = URLEncoder.encode("下载_" + fileName, "UTF-8");
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("remove")
    public ResponseEntity remove(Long fileId) {
        String fileName = "加密测试1.mpp";
        DocMinioParam docMinioParam = DocMinioParam.builder().setBucket(MinioBucketEnum.ETHICS)//桶的名字
            .setBucketPath("zym/doc/")//桶中存放的具体路径
            .setFileName(fileName)//文件名
            .setDocId("123456789")//文件唯一标识ID
            .setUserNick(userNick)//用户名
            .setUserId(userId)//用户ID
            .build();
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, false);
        boolean remove = docStream.removeDoc(docMinioParam);
        return new ResponseEntity(remove, HttpStatus.OK);
    }

    @GetMapping("previewFile")
    public ResponseEntity previewFile(@RequestParam("fileName") String fileName) {
        DocMinioParam docMinioParam = DocMinioParam.builder().secretKey(secretKey)//设置加密
            .setBucket(MinioBucketEnum.ETHICS)//存入的桶
            .setBucketPath("zym/doc/")//桶中的路径
            .setFileName(fileName)//文件名字
            .setDocId("123456789")//文件唯一表示ID
            .setUserNick(userNick)//操作用户
            .setUserId(userId)//操作用户ID
            .build();
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_MINIO, true);
        String s = docStream.docPreview(docMinioParam, 1);
        logger.info("预览的文件的存放路径 path:{}", s);
        return new ResponseEntity(s, HttpStatus.OK);
    }
}
