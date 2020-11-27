package com.imooc.enums;

/**
 * 支付方式的枚举类
 * 1：选择支付方式为微信
 * 2：支付宝
 */

public enum PayMethod {

    WEIXI(1,"微信"),
    ALIPAY(2,"支付宝");

    public final Integer type;
    public final String value;

    PayMethod(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
