package com.doc.demo.model.stream.result.local;

import com.doc.demo.model.stream.DocResultAbstract;
import org.springframework.util.StringUtils;

import java.util.Date;

public class DocLocalResult extends DocResultAbstract {
    private String rootPath;//保存根路径
    private String savePath;//具体保存路径,会带上文件名


    public DocLocalResult() {
        super(new Date());
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public boolean isSuccess() {
        return StringUtils.hasText(savePath);
    }
}
