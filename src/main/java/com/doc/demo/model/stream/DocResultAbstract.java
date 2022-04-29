package com.doc.demo.model.stream;

import java.util.Date;

/**
 * @author : zengYeMin
 * @date : 2022/4/2 9:17
 **/
public abstract class DocResultAbstract {
    //本次操作文件加密生成的Key,如果没有选择文件加密则会为null
    private String secretKey;
    //操作时间
    private Date operateTime;
    private String message;

    public DocResultAbstract(Date operateTime) {
        this.operateTime = operateTime;
    }

    protected abstract boolean isSuccess();

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

}
