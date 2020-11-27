package com.imooc.enums;

/**
 * 
 */

public enum Sex {
    
    WOMEN(0,"女"),
    MAN(1,"男"),
    SECRET(2,"密秘");

    public final Integer type;
    public final String value;

    Sex(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
