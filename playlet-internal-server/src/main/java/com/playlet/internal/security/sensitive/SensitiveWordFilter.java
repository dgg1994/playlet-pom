package com.playlet.internal.security.sensitive;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 类描述：核心匹配
 *
 * @author GeminiSun
 * @date 2026/08/12 09:28
 */
@Component
public class SensitiveWordFilter {

    /**
     * DFA根节点
     */
    private SensitiveNode root;

    /**
     * 初始化
     */
    public void init(
            SensitiveNode root){
        this.root=root;
    }

    /**
     * 查找敏感词
     */
    public List<SensitiveMatch> findWords(
            String text){
        List<SensitiveMatch> result =
                new ArrayList<>();
        if(text==null ||
                text.isEmpty()
                ||
                root==null){
            return result;
        }
        Set<String> exist =
                new HashSet<>();

        char[] chars =
                text.toCharArray();

        for(int i=0;i<chars.length;i++){
            SensitiveNode node=root;
            for(int j=i;j<chars.length;j++){
                char c=chars[j];
                node =
                        node.getChildren()
                                .get(c);
                if(node==null){
                    break;
                }
                if(node.isEnd()){
                    if(!exist.contains(
                            node.getWord()
                    )){
                        result.add(
                                new SensitiveMatch(
                                        node.getWord(),
                                        node.getLevel()
                                )
                        );
                        exist.add(
                                node.getWord()
                        );
                    }
                }
            }
        }
        return result;
    }
}