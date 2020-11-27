package com.imooc.enums;

/**
 * 评价枚举,1:表示好评价
 *        2：中评
 *        3：差评
 * @Desc 是不
 */

public enum CommentLevel {

    GOOD(1,"好评"),
    NORMAL(2,"中评"),
    BAD(3,"差评");

    public final Integer type;
    public final String value;

    /**
     * 枚举的构造函数
     * @param type
     * @param value
     */
    CommentLevel(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
