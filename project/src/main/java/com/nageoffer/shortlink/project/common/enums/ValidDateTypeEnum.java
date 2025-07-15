package com.nageoffer.shortlink.project.common.enums;

public enum ValidDateTypeEnum {

    PERMANENT(0),
    CUSTOM(1);

    private final int code;

    ValidDateTypeEnum(int code) {
        this.code = code;
    }

    public int getType() {
        return code;
    }
}
