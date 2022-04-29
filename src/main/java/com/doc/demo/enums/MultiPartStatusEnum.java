package com.doc.demo.enums;

public enum MultiPartStatusEnum {
    INITIALIZE(1), UPLOADING(2), UPLOAD_FINISH(3);

    private final int status;

    MultiPartStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}