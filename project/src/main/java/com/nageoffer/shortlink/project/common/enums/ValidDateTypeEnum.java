package com.nageoffer.shortlink.project.common.enums;

public enum ValidDateTypeEnum {

    //永久
    PERMANENT(0),
    //自定义
    CUSTOM(1);

    private final int code;

    ValidDateTypeEnum(int code) {
        this.code = code;
    }

    public int getType() {
        return code;
    }
}
