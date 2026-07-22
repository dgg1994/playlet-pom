-- ============================================================
-- 观影礼形态 B：时长阶梯（home.watchGift 摘要 + claim 领取）
--
-- 发奖复用已有表 user_coin_ledger：
--   biz_type = WATCH_GIFT
--   biz_id   = WATCH_GIFT:{yyyy-MM-dd}:{gear_index}  （同日同档幂等，只发一次）
-- 余额变更：app_account.coin_balance
-- ============================================================

-- ------------------------------------------------------------
-- 1. 观影礼全局配置
-- 说明：通常只保留一行 status=1；控制自然日切分、防刷与日累计上限。
--       status=0 时 C 端 home.watchGift 返回 null，不再累计/领取。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `watch_gift_global_config` (
  `id`                       INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `timezone`                 VARCHAR(32)  NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '签到/观影礼共用的自然日时区，用于计算今日 biz_date；建议 Asia/Shanghai',
  `min_report_interval_sec`  INT          NOT NULL DEFAULT 10 COMMENT '两次有效计入的最小间隔(秒)；小于该间隔的上报可忽略，用于防连刷',
  `max_delta_sec_per_report` INT          NOT NULL DEFAULT 120 COMMENT '单次观看上报最多计入的秒数；客户端 delta 超过则截断，默认120',
  `max_daily_seconds`        INT          NOT NULL DEFAULT 7200 COMMENT '单用户当日 watch_seconds 累计封顶(秒)；超过不再增加，默认7200=2小时',
  `status`                   TINYINT      NOT NULL DEFAULT 1 COMMENT '功能开关：1启用观影礼 0停用；停用时不返回摘要、不累计、不可claim',
  `setTime`                  DATETIME     NOT NULL COMMENT '配置创建时间',
  `gmtModified`              DATETIME     NOT NULL COMMENT '配置最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观影礼全局配置：时区、防刷参数、日封顶、启停开关（通常一行启用）';

-- ------------------------------------------------------------
-- 2. 观影礼奖励阶梯
-- 说明：一行对应 UI 一个奖励格；按 target_seconds 升序展示。
--       gear_index 为领取接口入参；state(done/claimable/locked) 由进度表计算，不落本表。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `watch_gift_reward_config` (
  `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `gear_index`      INT          NOT NULL COMMENT '档位序号，从1开始且唯一；POST claim?gearIndex= 使用该值；写入 claimed_gears 与流水 biz_id',
  `target_seconds`  INT          NOT NULL COMMENT '达标所需「当日累计有效观看秒数」；展示分钟约等于 target_seconds/60（如1800→30分钟）',
  `reward_coin`     INT          NOT NULL DEFAULT 0 COMMENT '该档领取成功后发放的金币数量，写入 user_coin_ledger.change_amt',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '档位开关：1对用户可见并可参与领取 0下架不进入摘要列表',
  `remark`          VARCHAR(255) DEFAULT NULL COMMENT '运营备注/对内说明，如「观看30分钟」；可不下发客户端',
  `setTime`         DATETIME     NOT NULL COMMENT '档位创建时间',
  `gmtModified`     DATETIME     NOT NULL COMMENT '档位最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gear_index` (`gear_index`) COMMENT '档位号唯一，保证 claim 入参与配置一一对应',
  KEY `idx_target_seconds` (`status`, `target_seconds`) COMMENT '按启用状态+达标秒数排序拉取阶梯'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观影礼奖励阶梯配置：档位、达标秒数、奖励金币；UI 横滑多档来源于此表';

-- ------------------------------------------------------------
-- 3. 用户观影礼每日进度
-- 说明：每用户每天一行（uk_uid_biz_date）。
--       watch_seconds 支撑「今日已看 X 分钟」；claimed_gears 支撑 Claimed/可领判断。
--       跨自然日新开一行，进度从 0 重新累计。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_watch_gift_progress` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `uid`              VARCHAR(64)  NOT NULL COMMENT '用户ID，对应 app_account 用户标识',
  `biz_date`         VARCHAR(10)  NOT NULL COMMENT '业务归属日 yyyy-MM-dd，按 global_config.timezone 计算；换日则新行',
  `watch_seconds`    INT          NOT NULL DEFAULT 0 COMMENT '当日已计入的有效观看总秒数；reportWatch 累计写入；home 展示今日观影时长',
  `claimed_gears`    VARCHAR(128) NOT NULL DEFAULT '' COMMENT '当日已成功领取的档位列表，逗号分隔，如 1,2；claim 成功后追加 gear_index；用于判断 done',
  `last_report_time` DATETIME     DEFAULT NULL COMMENT '上次成功计入观看时长的时间；与 min_report_interval_sec 配合做间隔校验',
  `setTime`          DATETIME     NOT NULL COMMENT '该日进度行首次创建时间',
  `gmtModified`      DATETIME     NOT NULL COMMENT '最近一次累计时长或领取奖励的更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid_biz_date` (`uid`, `biz_date`) COMMENT '同一用户同一自然日仅一条进度',
  KEY `idx_biz_date` (`biz_date`) COMMENT '按业务日查询/统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户观影礼每日进度：累计秒数、已领档位、上次上报时间；支撑 watchGift 摘要与 claim';

-- ============================================================
-- 初始化数据（可按运营调整）
-- ============================================================

INSERT INTO `watch_gift_global_config`
(`timezone`, `min_report_interval_sec`, `max_delta_sec_per_report`, `max_daily_seconds`, `status`, `setTime`, `gmtModified`)
VALUES
('Asia/Shanghai', 10, 120, 7200, 1, NOW(), NOW());

INSERT INTO `watch_gift_reward_config`
(`gear_index`, `target_seconds`, `reward_coin`, `status`, `remark`, `setTime`, `gmtModified`)
VALUES
(1,  600,  30, 1, '观看10分钟',  NOW(), NOW()),
(2, 1200,  30, 1, '观看20分钟',  NOW(), NOW()),
(3, 1800,  30, 1, '观看30分钟',  NOW(), NOW()),
(4, 2700,  30, 1, '观看45分钟',  NOW(), NOW()),
(5, 3600,  30, 1, '观看60分钟',  NOW(), NOW()),
(6, 5400,  30, 1, '观看90分钟',  NOW(), NOW()),
(7, 7200,  30, 1, '观看120分钟', NOW(), NOW());
