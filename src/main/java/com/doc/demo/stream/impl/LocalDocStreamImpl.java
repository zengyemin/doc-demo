package com.doc.demo.stream.impl;

import com.doc.demo.enums.DocStreamEnum;
import com.doc.demo.enums.MultiPartFileDeleteEnum;
import com.doc.demo.enums.MultiPartStatusEnum;
import com.doc.demo.exception.DocStreamException;
import com.doc.demo.factory.DocStreamFactory;
import com.doc.demo.model.stream.param.DocLocalParam;
import com.doc.demo.model.stream.result.local.DocLocalResult;
import com.doc.demo.model.stream.result.local.LocalMultiPartResult;
import com.doc.demo.stream.DocStream;
import com.doc.demo.utils.ClassInstanceUtil;
import com.doc.demo.utils.FileUtil;
import com.doc.demo.utils.IOUtil;
import com.doc.demo.utils.StreamClose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : zengYeMin
 * @date : 2022/4/2 14:23
 **/
public class LocalDocStreamImpl implements DocStream<DocLocalParam, DocLocalResult, LocalMultiPartResult> {
    private final Logger logger = LoggerFactory.getLogger(LocalDocStreamImpl.class);
    private static LocalDocStreamImpl localDocStream;
    /**
     * 用于防止同一个用户进行分段上传时文件冲突
     * key是用户ID+文件的MD5值{@code  param.getUserId() + param.getFileMd5()}
     */
    private final Map<String, Date> multiPartFileMarkMap = new HashMap<>();

    @Override
    public DocLocalResult uploadDoc(@NotNull DocLocalParam param, @NotNull InputStream is) {
        //获取一个保存文件的目录
        File dirFile = getSaveDirFile(param);//uploadDoc
        //要写入的文件
        final File file = new File(dirFile, param.getMD5FileName());
        //创建一个处理完毕的结果对象
        DocLocalResult docLocalResult = new DocLocalResult();
        docLocalResult.setRootPath(param.getRootPath());//根路径
        docLocalResult.setSecretKey(param.getSecretKey());//加密key
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtil.copy(is, fos);
            docLocalResult.setSavePath(file.getPath());//具体保存路径
            return docLocalResult;
        } catch (IOException e) {
            docLocalResult.setMessage(e.getMessage());
            logger.error("LocalDocStreamImpl uploadDoc:{}", e);
        }
        return docLocalResult;
    }

    @Override
    public byte[] downloadDoc(@NotNull DocLocalParam param) {
        //存储文件的根目录
        File rootFile = getFile(param.getRootPath());
        //获取要下载的文件
        File file = new File(new File(rootFile, param.getSaveDirFilePath()), param.getMD5FileName());
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            IOUtil.copy(fis, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("downloadDoc IOException:{}", e);
        }
        return null;
    }

    @Override
    public LocalMultiPartResult multiPartUpload(@NotNull DocLocalParam param, @Nullable InputStream is) {
        final String markKeyfileMd5 = param.getUserId() + param.getFileMd5();
        synchronized (multiPartFileMarkMap) {
            if (multiPartFileMarkMap.containsKey(markKeyfileMd5)) {
                throw new DocStreamException("当前文件分片已在处理中，请勿重复处理");
            }
            //标记一下当前文件的分片处理
            multiPartFileMarkMap.put(markKeyfileMd5, new Date());
        }
        try {
            //分断上传的处理方法
            return multiPartHandler(param, is);
        } finally {
            //方法结束移除掉当前文件的标记处理
            multiPartFileMarkMap.remove(markKeyfileMd5);
        }

    }

    @Override
    public boolean removeDoc(@NotNull DocLocalParam param) {
        return false;
    }

    @Override
    public String docPreview(@NotNull DocLocalParam param, @NotNull Integer expiredTime) {
        byte[] bytes = this.downloadDoc(param);
        File previewFile = getPreviewFileDir(param);
        //获取预览的根路径文件
//        File rootPreviewFile = getFile(getPreviewFilePath());
        return null;
    }


    /**
     * 分断上传的处理方法，方法中有文件合成处理，分段文件写入处理
     * <p>
     * {@code md5TempFile.list().length == param.getChunkCount()}则表示合并分段处理
     * {@code md5TempFile.list().length != param.getChunkCount()}则表示临时分断文件上传
     *
     * @param param 配置的参数对象
     * @param is    输入流
     */
    private LocalMultiPartResult multiPartHandler(DocLocalParam param, InputStream is) {
        String multiPartTempPath = param.getMultiPartRootPath(),
                fileMd5 = param.getFileMd5();
        //获取一个临时文件的根目录
        File tempRootFile = getFile(multiPartTempPath);
        //拼接一个临时文件具体路径，加上用户参数是为了预防，两个不用的用户，上传同一个文件，出现分段文件冲突
        String md5TempPath = param.getUserNick() + "_" + param.getUserId() + File.separator + fileMd5;
        //获取当前文件夹的临时md5文件夹
        File md5TempFile = getChildFile(tempRootFile, md5TempPath);

        if (md5TempFile.list().length == param.getChunkCount()) {
            //合并分段上传的文件
            return mergePartFile(param, md5TempFile);
        } else {
            //将传入的流写到分段文件中
            return writePartFile(md5TempFile, is);
        }
    }

    /**
     * 合并分段文件，此方法主要是做验证，排序文件等相关操作
     * 具体执行合并文件的方法是{@link LocalDocStreamImpl#executeMergePartFile(List, ByteArrayOutputStream)}方法
     * <p>
     * 1、获取排序好的分段文件{@link #getSortedPartFiles(File)}
     * 2、执行文件合并{@link #executeMergePartFile(List, ByteArrayOutputStream)}
     * 3、分段文件的删除处理
     *
     * @param param       传入的配置参数
     * @param md5TempFile 存储的分段文件的文件夹，用文件的md5值命名
     * @return {@link LocalMultiPartResult}
     */
    private LocalMultiPartResult mergePartFile(DocLocalParam param, File md5TempFile) {
        //获取上传的分段文件，此方法在获取时会对文件进行排序
        List<File> partFiles = getSortedPartFiles(md5TempFile);

        LocalMultiPartResult result = new LocalMultiPartResult();
        ByteArrayInputStream bis = null;
        try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
            //传入分段文件集合，和一个输入流执行分段文件的合并
            executeMergePartFile(partFiles, fos);
            bis = new ByteArrayInputStream(fos.toByteArray());
            //执行文件上传操作
            uploadMergePartFile(param, bis, result);
            //分段文件的删除处理方法，目前分为三种处理方式
            partFileDeleteHandler(param.getDeleteEnum(), partFiles);
        } catch (IOException e) {
            logger.error("mergePartFile IOException:{}", e);
            result.setMessage(e.getMessage());
        } finally {
            StreamClose.close(bis);
        }
        return result;
    }

    /**
     * 上传合并的分段文件
     * 根据{@code secretKey}来拍断是否启用代理
     *
     * @param param 上传参数
     * @param bis   合并后的输入流
     */
    @SuppressWarnings("all")
    private void uploadMergePartFile(DocLocalParam param, ByteArrayInputStream bis, LocalMultiPartResult result) {
        String secretKey = param.getSecretKey();
        DocStream docStream = DocStreamFactory.getDocStreamInstance(DocStreamEnum.DOC_LOCAL, StringUtils.hasText(secretKey));
        DocLocalResult docLocalResult = (DocLocalResult) docStream.uploadDoc(param, bis);
        if (!docLocalResult.isSuccess()) {
            result.setMessage("分段上传失败" + docLocalResult.getMessage());
        } else {
            result.setMessage("分段上传完成，合并后文件存放地址为:" + docLocalResult.getSavePath());
        }
        result.setUseUploadNum(param.getChunkCount());
        result.setStatusEnum(MultiPartStatusEnum.UPLOAD_FINISH);
        result.setSecretKey(param.getSecretKey());//加密
    }

    /**
     * 获取上传的分段文件，此方法在获取时会对文件进行排序
     * <p>
     * 顺序例如：1.part，2.part，3.part，4.part
     *
     * @param md5TempFile 当前上传文件md5值生成的临时文件夹
     * @return {@link List<File>}
     */
    private List<File> getSortedPartFiles(File md5TempFile) {
        //获取分段文件信息，为了保证顺序获取前对文件进行排序
        List<File> partFiles = Arrays.stream(md5TempFile.listFiles()).sorted((o1, o2) -> {
            String o1Name = o1.getName();
            Integer o1FileNum = Integer.valueOf(o1Name.substring(0, o1Name.indexOf(".")));
            String o2Name = o2.getName();
            Integer o2FileNum = Integer.valueOf(o2Name.substring(0, o2Name.indexOf(".")));
            return o1FileNum.compareTo(o2FileNum);
        }).collect(Collectors.toList());
        logger.info("获取分段文件完毕,md5:{} 文件数量:{}", md5TempFile.getName(), partFiles.size());
        return partFiles;
    }

    /**
     * 传入分段文件集合，和一个输入流进行分段文件的合并
     * 输入流是准确存储的文件，将所有的分段文件流写入{@code fos}中生成新的文件
     *
     * @param partFiles 分段文件的集合，已经排序
     * @param fos       输入流
     */
    private void executeMergePartFile(List<File> partFiles, ByteArrayOutputStream fos) {
        for (int i = 0; i < partFiles.size(); i++) {
            int index = i + 1;//用于日志打印
            logger.info("开始合并 第 {} 个文段文件", index);
            try (FileInputStream partFis = new FileInputStream(partFiles.get(i))) {
                IOUtil.copy(partFis, fos);
            } catch (IOException e) {
                logger.info("executeMergePartFile IOException:{}", e.getMessage());
            }
            logger.info("第 {} 个文段文件合并完成", index);
        }
    }

    /**
     * 分段文件的删除处理方法，目前分为三种处理方式
     * <p>
     * 1、不删除分段文件
     * 2、分段上传处理完成后上传删除
     * 3、定时任务监控删除
     *
     * @param deleteEnum 删除处理状态
     * @param partFiles  分段文件集合
     */
    private void partFileDeleteHandler(MultiPartFileDeleteEnum deleteEnum, List<File> partFiles) {
        if (partFiles == null || partFiles.isEmpty()) return;
        switch (deleteEnum) {
            case NOT_DELETE: {
                logger.info("当前分段文件不进行删除");
                break;
            }
            case FINISH_DELETE: {
                //分段文件的删除工具方法，此方法有两个处理方式，处理方式根据运行系统判断
                FileUtil.deletePartFiles(partFiles);
                break;
            }
            case TIMING_DELETE: {
                logger.info("定时删除功能暂未实现");
                break;
            }

        }
    }

    /**
     * 获取一个文件目录，如果文件不存在则会进行创建
     *
     * @param path 文件地址
     * @return {@link File}
     */
    private File getFile(String path) {
        //tempRootFile是临时文件的根目录文件
        File tempFile = new File(path);
        if (tempFile.exists()) return tempFile;
        boolean mkdir = tempFile.mkdir();
        logger.info("创建一个文件存储目录，目录地址:{} 创建状态:{}", path, (mkdir ? "成功" : "失败"));
        if (!mkdir) throw new DocStreamException("创建文件目录失败 文件路径:%s", tempFile);
        return tempFile;
    }

    /**
     * 将传入的流传写入{@code md5TempFile}目录下的分段文件中
     *
     * @param md5TempFile 配置的处理参数对象
     * @param is          传入的输入流
     * @return {@link LocalMultiPartResult}
     */
    private LocalMultiPartResult writePartFile(File md5TempFile, InputStream is) {
        LocalMultiPartResult result = new LocalMultiPartResult();

        if (is == null) {//流为空则不做处理，返回一下当前文件的分片信息即可
            result.setUseUploadNum(md5TempFile.list().length);
            result.setStatusEnum(MultiPartStatusEnum.UPLOADING);//状态设置为上传中
            return result;
        }

        int useNum = md5TempFile.list().length;
        //创建分片文件
        File partFile = getPartFile(md5TempFile);
        try (FileOutputStream fos = new FileOutputStream(partFile)) {
            IOUtil.copy(is, fos);
        } catch (IOException e) {
            logger.error("IOException e:{}");
        }
        result.setStatusEnum(MultiPartStatusEnum.UPLOADING);//状态设置为上传中
        result.setUseUploadNum(useNum + 1);//设置已经上传的数量
        return result;
    }

    /**
     * 获取当前文件的md5目录，当前md5目前在合并文件后会进行删除的
     *
     * @param parentFile 零食文件根目录，会一直存在
     * @param childPath  子文件路径，格式例如
     *                   Windows： \ethic\doc
     *                   Linux：/ethic/doc
     * @return 对应的文件目录
     */
    private File getChildFile(File parentFile, String childPath) {
        //如果传入的子连接没有文字则返回父文件
        if (!StringUtils.hasText(childPath)) return parentFile;
        File childFile = new File(parentFile, childPath);
        //如果目录已经存在则直接返回
        if (childFile.exists()) return childFile;
        boolean mkdir = false;
        try {
            mkdir = childPath.contains(".") ? childFile.createNewFile() : childFile.mkdirs();
        } catch (IOException e) {
            logger.error("getChildFile IOException:{}", e);
        }
        logger.info("创建一个子文件存储目录，目录地址:{} 创建状态:{}", childFile, (mkdir ? "成功" : "失败"));
        if (!mkdir) throw new DocStreamException("创建子文件目录失败 文件路径:%s", childFile.getPath());
        return childFile;
    }

    /**
     * 获取一个保存文件的目录
     * 如果当前文件已存在相同文件则会将旧文件进行删除
     *
     * @param param 上传配置的参数
     * @return {@link File}
     */
    private File getSaveDirFile(DocLocalParam param) {
        File rootFile = getFile(param.getRootPath());
        File dirFile = getChildFile(rootFile, param.getSaveDirFilePath());
        //匹配当前文件是否已经存在，如果不存在则为null
        File tempFile = Arrays.stream(dirFile.listFiles())
                .filter(x -> x.getName().equals(param.getMD5FileName()))
                .findAny().orElse(null);

        //如果文件存在则先删除旧文件
        if (tempFile != null) tempFile.delete();
        return dirFile;
    }

    /**
     * 在具体md5文件夹下创建，创建对应的分段文件
     *
     * @param md5TempFile
     * @return
     */
    private File getPartFile(File md5TempFile) {
        String[] fileList = md5TempFile.list();
        String partFileName = (fileList.length + 1) + ".part";
        return new File(md5TempFile, partFileName);
    }

    /**
     * 单例创建对象
     *
     * @return {@link LocalDocStreamImpl}
     */
    public static LocalDocStreamImpl instance() {
        if (localDocStream != null) return localDocStream;
        synchronized (LocalDocStreamImpl.class) {
            if (localDocStream != null) return localDocStream;
            //调用检测方法，判断当前是否可以被创建
            ClassInstanceUtil.docStreamInstanceCheck();//Local
            localDocStream = new LocalDocStreamImpl();
        }
        return localDocStream;
    }

    private LocalDocStreamImpl() {
    }
}
