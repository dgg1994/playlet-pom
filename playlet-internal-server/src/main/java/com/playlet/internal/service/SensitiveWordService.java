package com.playlet.internal.service;

import com.playlet.internal.security.sensitive.SensitiveNode;
import com.playlet.internal.security.sensitive.SensitiveWordFilter;
import com.playlet.internal.security.sensitive.SensitiveWordLoader;
import com.playlet.internal.security.sensitive.SensitiveMatch;
import com.playlet.internal.base.SensitiveCheckResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * 类描述：敏感词业务
 *
 * @author GeminiSun
 * @date 2026/08/12 09:30
 */
@Service
public class SensitiveWordService {

    @Resource
    private SensitiveWordLoader loader;

    @Resource
    private SensitiveWordFilter filter;

    /**
     * 项目启动加载
     */
    @PostConstruct
    public void init(){
        SensitiveNode root =
                loader.load();
        filter.init(root);
    }

    /**
     * 评论审核
     */
    public SensitiveCheckResult check(
            String content){

        SensitiveCheckResult result =
                new SensitiveCheckResult();
        List<SensitiveMatch> matches =
                filter.findWords(content);
        result.setMatches(matches);
        if(matches.isEmpty()){
            result.setPass(true);
            result.setLevel(0);
            return result;
        }
        result.setPass(false);
        int maxLevel=0;
        for(SensitiveMatch match:matches){
            if(match.getLevel()>maxLevel){

                maxLevel=
                        match.getLevel();
            }
        }
        result.setLevel(maxLevel);
        return result;
    }
}