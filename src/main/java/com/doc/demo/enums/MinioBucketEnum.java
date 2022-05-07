package com.doc.demo.enums;

/**
 * @author : zengYeMin
 * @date : 2022/5/5 8:24
 **/
public enum MinioBucketEnum {
    CTMS("yootrial-ctms"), ETHICS("yootrial-ethics"), EDC("yootrial-edc"), CAPTURE("yootrial-capture"), PHARMACY(
        "yootrial-pharmacy"), TRAIN_EXAM("yootrial-train_exam");

    private final String name;

    MinioBucketEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
