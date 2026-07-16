package com.playlet.internal.api.request;

import com.playlet.internal.entity.drama.RankBoardEntity;
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