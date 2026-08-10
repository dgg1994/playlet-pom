package com.playlet.oversea.api.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JpushReqEntity {
	
	/** 标题 */
    private String title;
    
    /** 消息体 */
    private String msg;
    
    /** 是否广播 true-是 false-否 */
    private boolean broadcasting;
    
    /** 别名（用户ID），一次最多1000个 */
    private List<String> aliasList;
    
    /** 注册ID（设备ID），一次最多1000个，优先于别名推送 */
    private List<String> registrationIdList;
    
    /** 附加参数 */
    private Map<String, Object> extrasMap;

}
