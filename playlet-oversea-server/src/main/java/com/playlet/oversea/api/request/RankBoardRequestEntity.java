package com.playlet.oversea.api.request;

import com.playlet.oversea.entity.drama.RankBoardEntity;
import lombok.Data;

import java.util.List;

/**
 * 类描述：榜单请求参数
 *
 * @author GeminiSun
 * @date 2026/07/16 15:16
 */
@Data
public class RankBoardRequestEntity {

    private List<RankBoardEntity> ranks;

    private Integer boardType;

    private Integer sortWeight;

    private Integer topN;

}