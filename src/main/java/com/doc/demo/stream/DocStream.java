package com.doc.demo.stream;

import com.doc.demo.exception.DocStreamException;
import com.doc.demo.model.stream.DocParamAbstract;
import com.doc.demo.model.stream.DocResultAbstract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 * 流操作的相关可实现此规范接口
 * 当前业务场景下，相关操作必须实现
 * <p>
 * 1、流的上传 {@link #uploadDoc(DocParamAbstract, InputStream)}
 * 2、流的获取 {@link #downloadDoc(DocParamAbstract)}
 * 3、分段上传 {@link #multiPartUpload(DocParamAbstract, InputStream)}
 * 4、流的删除 {@link #removeDoc(DocParamAbstract)}
 * 5、文件预览 {@link #docPreview(DocParamAbstract, Long)}
 * <p>
 * 如果后续有更多需要扩展的，可在具体的实现类中进行扩展
 *
 * @author : zengYeMin
 * @date : 2022/3/31 17:34
 **/
public interface DocStream<P extends DocParamAbstract, R1 extends DocResultAbstract, R2 extends DocResultAbstract> {

    /**
     * 传入一个IO流，然后根据{@code param}的配置信息进行上传处理
     * <p>
     * 上传方法{@link #uploadDoc(DocParamAbstract, InputStream)}不会对传入的
     * 流{@code is}进行关闭，外部引用此方法时切记调用关闭方法
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @param is 需要被上传的IO流
     * @return 处理成功则返回true, 失败返回false
     */
    R1 uploadDoc(@NotNull P param, @NotNull InputStream is);

    /**
     * 根据{@code param}的配置信息进行文件下载处理
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @return 成功返回一个byte数组，失败返回null
     */
    byte[] downloadDoc(@NotNull P param);

    /**
     * 分段上传，目前分段分为两种
     * <p>
     * 1、minio分段实现，是利用minio服务作为中转，然后同一合并
     * minio当前的操作方式，暂时没有实现服务器生产秘钥进行加密操作
     * <p>
     * 2、local分段实现，是将文件分段上传到服务器的临时存储区，然后同一合并临时文件
     * <p>
     * 分段上传方法 {@link #multiPartUpload(DocParamAbstract, InputStream)} 不会对传入的
     * 流{@code is}进行关闭，外部引用此方法时切记调用关闭方法
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @param is 分段的流，如果是类似于minio直接使用第三方则传入null即可
     * @return 返回的数据对象为 {@link DocResultAbstract}的子类
     */
    R2 multiPartUpload(@NotNull P param, @Nullable InputStream is);

    /**
     * 根据{@code param}的配置信息进行对应文件的删除
     * 如果删除的流不存在，也应该返回true
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @return 处理成功则返回true, 失败返回false
     */
    boolean removeDoc(@NotNull P param);

    /**
     * 文件预览接口，根据不同系统环境返回不同预览文件存储地址
     * <p>
     * 如果预览文件是加密文件，则需要配置对应的解密参数
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @param expiredTime 预览路径过期时间，单位为小时
     * @return 返回一个绝对路径 {@link String}
     */
    String docPreview(@NotNull P param, @NotNull Integer expiredTime);

    /**
     * 默认接口方法，获取预览文件的文件夹
     * 如果默认接口方法不满足业务场景，可自行重写
     *
     * @param param 根据不同的实现类会有所变化 {@link DocParamAbstract}
     * @return 返回一个预览文件目录 {@link File}
     */
    default File getPreviewFileDir(@NotNull P param) {
        String osName = System.getProperty("os.name").toLowerCase();
        //预览文件根路径
        String rootPath = osName.contains("win") ? "C:\\temp_preview_dir" : "/usr/local/temp_preview_dir";
        //预览文件用户路径
        String userPath = param.getUserNick() + "_" + param.getUserId();
        //最终文件路径
        String finalPath = new StringBuilder(rootPath).append(File.separator).append(userPath).toString();
        File file = new File(finalPath);
        if (file.exists()) {
            return file;
        }
        boolean mkdirs = file.mkdirs();
        if (!mkdirs) {
            throw new DocStreamException("创建预览文件目录失败:" + finalPath);
        }
        return file;
    }
}
