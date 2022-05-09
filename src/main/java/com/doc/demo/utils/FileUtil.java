package com.doc.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class FileUtil {

    protected static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 分段文件的删除工具方法，此方法有两个处理方式，处理方式根据运行系统判断
     * <p>
     * 1、Windows系统则循环文件集合删除文件，文件删除成功后删除上级文件夹
     * 2、Linux系统则直接获取上级文件夹目录，使用{@code rm -rf 文件路径}进行删除
     *
     * @param partFiles 分段文件集合
     */
    public static void deletePartFiles(List<File> partFiles) {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("win")) {
            File file = partFiles.get(0);
            //循环执行删除操作
            boolean allMatch = partFiles.stream().allMatch(File::delete);
            if (allMatch) {
                logger.info("分段文件全部删除完毕，即将删除对应的md5文件夹");
                boolean delete = file.getParentFile().delete();
                logger.info("md5文件夹删除完毕，删除状态:{}", (delete ? "成功" : "失败"));
            }
            return;
        }
        Process p;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        String co = "rm -rf " + partFiles.get(0).getParentFile();
        try {
            p = Runtime.getRuntime().exec(co);
            inputStreamReader = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(inputStreamReader);
            int returnCode = p.waitFor();
            logger.info("执行删除任务完毕 删除命令:{} 时间:{}", co, returnCode);
        } catch (IOException e) {
            logger.error("deletePartFiles IOException:{}", e);
        } catch (InterruptedException e) {
            logger.error("deletePartFiles InterruptedException:{}", e);
        } finally {
            StreamClose.close(inputStreamReader, br);//关闭流
        }

    }

    /**
     * 传入输入流和一个文件，然后将流写入文件中
     *
     * @param is 传入的输入流 {@link InputStream}
     * @param file 被写入的文件 {@link File}
     * @return 写入文件的绝对路径 {@link String}
     */
    public static String inputStreamWriteFile(InputStream is, File file) {
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    return null;
                }
            } catch (IOException e) {
                logger.error("创建写入文件失败 msg:{}", e.getMessage());
                return null;
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            long copy = IOUtil.copy(is, fileOutputStream);
            long size = copy / 1024;
            logger.info("流写入文件成功 文件名:{} 文件大小:{}KB", file.getName(), size);
        } catch (IOException e) {
            logger.error("流写入文件失败 msg:{}", e.getMessage());
            return null;
        }
        return file.getPath();
    }
}
