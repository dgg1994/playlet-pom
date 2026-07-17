package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：任务类型枚举
 *
 * @author GeminiSun
 * @date 2026/07/17 17:36
 */
public enum WelfareActionTypeEnums {

    WATCH("WATCH", "观看"),
    SHARE("SHARE", "分享"),
    FOLLOW("FOLLOW", "关注"),
    COMMENT("COMMENT", "评论"),
    INVITE("INVITE", "邀请"),
    RECHARGE("RECHARGE", "充值"),
    SIGN_IN("SIGN_IN", "签到"),
    LIKE("LIKE", "点赞"),
    MANUAL("MANUAL", "仅配置/手动，无自动推进");


    private String name;

    private String lable;

    WelfareActionTypeEnums(String name, String lable) {
        this.name = name;
        this.lable = lable;
    }

    public String getName() {
        return name;
    }

    public String getLable() {
        return lable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public static String getName(int i) {
        WelfareActionTypeEnums[] typeEnums = values();
        for (WelfareActionTypeEnums typeEnum : typeEnums) {
            if (typeEnum.getName().equals(i)) {
                return typeEnum.getLable();
            }
        }
        return null;
    }

    public static List<DicEntity> getList() {
        WelfareActionTypeEnums[] typeEnums = values();
        List<DicEntity> list = new ArrayList<>();
        for (WelfareActionTypeEnums typeEnum : typeEnums) {
            DicEntity dicEntity = new DicEntity();
            dicEntity.setName(typeEnum.getName());
            dicEntity.setLable(typeEnum.getLable());
            list.add(dicEntity);
        }
        return list;
    }
}
