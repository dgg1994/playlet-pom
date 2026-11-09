package com.playlet.internal.service.banner.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.banner.BannerManageDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.banner.BannerManageEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.service.banner.BannerManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端 Banner 与开屏实现
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class BannerManageServiceImpl implements BannerManageService {

    /** jumpType 枚举 */
    public static final int JUMP_TYPE_H5      = 1;
    public static final int JUMP_TYPE_DRAMA   = 2;
    public static final int JUMP_TYPE_MEMBER   = 3;

    /** position 枚举 */
    public static final int POSITION_HOME_TOP  = 1;
    public static final int POSITION_HOME_SWIPE = 2;
    public static final int POSITION_APP_SPLASH = 3;

    @Autowired
    private BannerManageDao bannerManageDao;
    @Autowired
    private DramaDao dramaDao;

    @Override
    @SysLogAnnotation(module = "Banner与开屏", type = "POST", remark = "Banner列表")
    public ResponseBase findList(@RequestBody(required = false) BannerManageEntity entity) {
        if (entity == null) {
            entity = new BannerManageEntity();
        }
        // keyword 去空格
        if (entity.getKeyword() != null) {
            entity.setKeyword(entity.getKeyword().trim());
        }
        PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
        List<BannerManageEntity> list = bannerManageDao.findAdminList(entity);
        if (list == null) {
            list = new ArrayList<>();
        }
        PageInfo<BannerManageEntity> page = new PageInfo<>(list);
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "Banner与开屏", type = "POST", remark = "新增Banner")
    public ResponseBase save(@RequestBody BannerManageEntity entity) {
        try {
            String err = validate(entity, true);
            if (err != null) {
                return setResultError(err);
            }
            if (entity.getSortWeight() == null) {
                entity.setSortWeight(0);
            }
            GenericityUtil.setDate(entity);
            bannerManageDao.insert(entity);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("save banner error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "Banner与开屏", type = "POST", remark = "编辑Banner")
    public ResponseBase update(@RequestBody BannerManageEntity entity) {
        try {
            if (entity.getId() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            BannerManageEntity old = bannerManageDao.selectById(entity.getId());
            if (old == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            String err = validate(entity, false);
            if (err != null) {
                return setResultError(err);
            }
            GenericityUtil.updateDate(entity);
            bannerManageDao.updateById(entity);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("update banner id={} error", entity.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "Banner与开屏", type = "POST", remark = "删除Banner")
    public ResponseBase delete(@RequestBody BannerManageEntity entity) {
        try {
            if (entity.getId() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            bannerManageDao.deleteById(entity.getId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("delete banner id={} error", entity.getId(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 统一参数校验
     * @param creating 是否新增（新增时所有必填都检查；编辑时只校验非空字段）
     */
    private String validate(BannerManageEntity e, boolean creating) {
        if (e == null) {
            return I18nUtil.getMessage("base_error");
        }

        // title
        if (creating && StringUtils.isEmpty(e.getTitle())) {
            return "标题不能为空";
        }
        if (e.getTitle() != null && e.getTitle().length() > 64) {
            return "标题长度不能超过 64";
        }

        // previewText 长度
        if (e.getPreviewText() != null && e.getPreviewText().length() > 8) {
            return "缩略文案长度不能超过 8";
        }

        // imageUrl
        if (creating && StringUtils.isEmpty(e.getImageUrl())) {
            return "图片不能为空";
        }

        // jumpType
        if (creating && e.getJumpType() == null) {
            return "请选择跳转类型";
        }
        if (e.getJumpType() != null) {
            if (e.getJumpType() < JUMP_TYPE_H5 || e.getJumpType() > JUMP_TYPE_MEMBER) {
                return "跳转类型无效";
            }
            if (JUMP_TYPE_H5 == e.getJumpType()) {
                // jumpType=1：URL 格式校验
                if (StringUtils.isEmpty(e.getJumpValue())) {
                    return "请填写 H5 地址";
                }
                try {
                    new URL(e.getJumpValue().trim());
                } catch (MalformedURLException ex) {
                    return "H5 地址格式无效";
                }
            } else if (JUMP_TYPE_DRAMA == e.getJumpType()) {
                // jumpType=2：短剧存在性校验（编辑时才查，新增也查）
                if (StringUtils.isEmpty(e.getJumpValue())) {
                    return "请填写短剧 ID";
                }
                try {
                    Integer dramaId = Integer.parseInt(e.getJumpValue().trim());
                    DramaEntity drama = dramaDao.selectById(dramaId);
                    if (drama == null) {
                        return "短剧不存在";
                    }
                } catch (NumberFormatException nfe) {
                    return "短剧 ID 必须为数字";
                }
            }
            // jumpType=3：会员开通页，jumpValue 可空，不校验
        }

        // position
        if (creating && e.getPosition() == null) {
            return "请选择投放位置";
        }
        if (e.getPosition() != null) {
            if (e.getPosition() < POSITION_HOME_TOP || e.getPosition() > POSITION_APP_SPLASH) {
                return "投放位置无效";
            }
        }

        // 时间
        if (creating && e.getStartTime() == null) {
            return "请选择开始日期";
        }
        if (creating && e.getEndTime() == null) {
            return "请选择结束日期";
        }
        if (e.getStartTime() != null && e.getEndTime() != null) {
            if (e.getEndTime().before(e.getStartTime())) {
                return "结束日期不能早于开始日期";
            }
        }

        return null;
    }
}
