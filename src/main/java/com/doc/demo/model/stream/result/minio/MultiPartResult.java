package com.doc.demo.model.stream.result.minio;

import com.doc.demo.enums.MultiPartStatusEnum;
import com.doc.demo.model.stream.DocResultAbstract;

import java.util.Date;

/**
 * @author : zengYeMin
 * @date : 2022/4/11 12:40
 **/
public class MultiPartResult extends DocResultAbstract {
    //状态为1=初始化分片完成，状态为2=表示上传中，状态=3表示上传完毕
    private MultiPartStatusEnum statusEnum;
    //当前参数表示已上传的数量
    private int useUploadNum;

    public MultiPartResult(Date operateTime) {
        super(operateTime);
    }

    public MultiPartStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(MultiPartStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public int getUseUploadNum() {
        return useUploadNum;
    }

    public void setUseUploadNum(int useUploadNum) {
        this.useUploadNum = useUploadNum;
    }

    @Override
    protected boolean isSuccess() {
        return false;
    }
}
