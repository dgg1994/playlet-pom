package com.playlet.internal.security.sensitive;

import com.playlet.internal.dao.security.SensitiveWordDao;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 类描述：加载数据库词库
 *
 * @author GeminiSun
 * @date 2026/08/12 09:27
 */
@Component
public class SensitiveWordLoader {

    @Resource
    private SensitiveWordDao sensitiveWordDao;

    /**
     * 加载DFA
     */
    public SensitiveNode load(){
        SensitiveNode root =
                new SensitiveNode();
        List<SensitiveWordEntity> list =
                sensitiveWordDao.selectList(null);
        for(SensitiveWordEntity entity:list){
            insert(
                    root,
                    entity.getWord(),
                    entity.getLevel()
            );
        }
        return root;
    }


    /**
     * 添加敏感词
     */
    private void insert(
            SensitiveNode root,
            String word,
            Integer level){

        SensitiveNode node=root;
        for(char c:word.toCharArray()){
            node.getChildren()
                    .putIfAbsent(
                            c,
                            new SensitiveNode()
                    );
            node =
                    node.getChildren()
                            .get(c);
        }

        node.setEnd(true);
        node.setWord(word);
        node.setLevel(level);
    }
}