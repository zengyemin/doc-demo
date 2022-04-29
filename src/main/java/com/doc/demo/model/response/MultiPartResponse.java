package com.doc.demo.model.response;

/**
 * @author : zengYeMin
 * @date : 2022/4/8 15:29
 **/
public class MultiPartResponse {
    private String msg;
    private String url;
    private int status;
    private int code;

    public MultiPartResponse(int status, String msg, int code) {
        this.msg = msg;
        this.status = status;
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
