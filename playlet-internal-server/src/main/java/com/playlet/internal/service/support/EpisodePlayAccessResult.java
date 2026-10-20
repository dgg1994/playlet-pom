package com.playlet.internal.service.support;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单集播放策略判定结果（内部）。
 */
@Data
public class EpisodePlayAccessResult {

	private Integer assetId;
	private Integer dramaId;
	private Integer setNum;
	/** 是否可播放（可签发视频地址） */
	private boolean canPlay;
	/** 是否已有解锁记录 */
	private boolean unlocked;
	/** 是否有效会员 */
	private boolean member;
	/** 是否会员短剧 */
	private boolean memberDrama;
	/** drama_asset.unlock_type */
	private Integer unlockType;
	/** 金币价 */
	private Integer priceCoin;
	/** 现金价 */
	private BigDecimal priceYuan;
	/** 拦截原因：NEED_LOGIN / NEED_MEMBER / NEED_AD / NEED_PAY / NOT_ON_SHELF；可播时 null */
	private String blockReason;
	/** 已解锁方式；未解锁为 null */
	private String unlockWay;
}
