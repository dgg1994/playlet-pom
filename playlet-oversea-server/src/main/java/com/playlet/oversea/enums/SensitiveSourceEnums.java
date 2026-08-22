package com.playlet.oversea.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 类描述：敏感词来源
 *
 * @author GeminiSun
 * @date 2026/08/12 10:54
 */
@Getter
@AllArgsConstructor
public enum SensitiveSourceEnums {
    /**
     * 短剧评论
     */
    DRAMA_COMMENT(1, "短剧评论"),
    /**
     * 视频评论
     */
    VIDEO_COMMENT(2, "视频评论"),
    /**
     * 弹幕
     */
    DANMU(3, "弹幕"),
    /**
     * 用户昵称
     */
    USER_NAME(4, "用户昵称"),
    /**
     * 举报
     */
    PRIVATE_MESSAGE(5, "举报"),
    /**
     * 其他
     */
    OTHER(99, "其他");
    /**
     * 类型编码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String lable;

    /**
     * 根据code获取枚举
     */
    public static SensitiveSourceEnums getByCode(Integer code){
        for(SensitiveSourceEnums item:
                SensitiveSourceEnums.values()){
            if(item.getCode().equals(code)){
                return item;
            }
        }
        return OTHER;
    }
}
