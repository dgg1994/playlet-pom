package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.AppVersionCheckRequest;
import com.playlet.internal.api.response.AppVersionCheckRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.AppVersionContext;
import com.playlet.internal.config.heard.DeviceTypeContext;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.AppVersionConstants;
import com.playlet.internal.dao.version.AppVersionConfigDao;
import com.playlet.internal.dao.version.AppVersionI18nDao;
import com.playlet.internal.entity.version.AppVersionConfigEntity;
import com.playlet.internal.entity.version.AppVersionI18nEntity;
import com.playlet.internal.enums.AppVersionChannelEnums;
import com.playlet.internal.enums.AppVersionPlatformEnums;
import com.playlet.internal.enums.DeviceTypeEnums;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.VersionApiService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
public class VersionApiServiceImpl extends BaseApiService implements VersionApiService {

	@Autowired
	private AppVersionConfigDao appVersionConfigDao;

	@Autowired
	private AppVersionI18nDao appVersionI18nDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase check(@RequestBody(required = false) AppVersionCheckRequest request,
			HttpServletRequest httpRequest) {
		if (request == null) {
			request = new AppVersionCheckRequest();
		}
		fillFromHeader(request);

		if (StringUtils.isEmpty(request.getPlatform()) || request.getVersionCode() == null) {
			return setResultError(I18nUtil.getMessage("version_param_required"));
		}
		if (!AppVersionPlatformEnums.isValid(request.getPlatform())) {
			return setResultError(I18nUtil.getMessage("version_platform_invalid"));
		}
		if (request.getVersionCode() < 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}

		String platform = request.getPlatform().trim().toLowerCase();
		String channel = StringUtils.isEmpty(request.getChannel()) ? AppVersionConstants.DEFAULT_CHANNEL
				: request.getChannel().trim().toLowerCase();
		if (!AppVersionChannelEnums.isValid(channel)) {
			channel = AppVersionConstants.DEFAULT_CHANNEL;
		}

		AppVersionConfigEntity latest = appVersionConfigDao.findLatestForCheck(platform, channel);
		if (latest == null && !AppVersionConstants.DEFAULT_CHANNEL.equals(channel)) {
			latest = appVersionConfigDao.findLatestForCheck(platform, AppVersionConstants.DEFAULT_CHANNEL);
		}

		AppVersionCheckRespEntity resp = new AppVersionCheckRespEntity();
		if (latest == null) {
			resp.setNeedUpdate(false);
			resp.setForceUpdate(false);
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		}

		Integer clientCode = request.getVersionCode();
		Integer latestCode = latest.getVersionCode() == null ? 0 : latest.getVersionCode();
		// 是否需要更新
		boolean needUpdate = clientCode < latestCode;
		// 强制更新
		boolean forceUpdate = needUpdate && latest.getIsForce() != null && latest.getIsForce() == 1;

        resp.setNeedUpdate(needUpdate);
		resp.setForceUpdate(forceUpdate);
		resp.setVersionCode(latest.getVersionCode());
		resp.setVersionName(latest.getVersionName());
		resp.setDownloadUrl(mediaUrlService.sign(latest.getDownloadUrl()));

		if (needUpdate) {
			AppVersionI18nEntity i18n = resolveI18n(latest.getId());
			if (i18n != null) {
				resp.setTitle(i18n.getTitle());
				resp.setContent(i18n.getContent());
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 从请求头中获取设备类型，并填充到 request 中。
	 */
	private void fillFromHeader(AppVersionCheckRequest request) {
		if (StringUtils.isEmpty(request.getPlatform())) {
			String deviceType = DeviceTypeContext.getDeviceType();
			request.setPlatform(mapDeviceTypeToPlatform(deviceType));
		}
		if (request.getVersionCode() == null) {
			Integer code = parseVersionCode(AppVersionContext.getAppVersion());
			if (code != null) {
				request.setVersionCode(code);
			}
		}
		if (StringUtils.isEmpty(request.getVersionName())) {
			request.setVersionName(AppVersionContext.getAppVersion());
		}
	}

	/**
	 * 映射设备类型到平台。
	 */
	private static String mapDeviceTypeToPlatform(String deviceType) {
		if (StringUtils.isEmpty(deviceType)) {
			return null;
		}
		String v = deviceType.trim();
		if (v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_ANDROID.getName()) || "1".equals(v)
				|| "android".equalsIgnoreCase(v)) {
			return AppVersionPlatformEnums.ANDROID.getCode();
		}
		if (v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_IOS.getName()) || "2".equals(v)
				|| "ios".equalsIgnoreCase(v)
				|| v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_IPAD.getName()) || "4".equals(v)) {
			return AppVersionPlatformEnums.IOS.getCode();
		}
		if (v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_WEB.getName()) || "3".equals(v)
				|| "web".equalsIgnoreCase(v)) {
			return AppVersionPlatformEnums.WEB.getCode();
		}
		if (v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_DESKTOP.getName()) || "5".equals(v)) {
			return AppVersionPlatformEnums.WINDOWS.getCode();
		}
		if (v.equalsIgnoreCase(DeviceTypeEnums.CLIENT_EXTENSION.getName()) || "6".equals(v)) {
			return AppVersionPlatformEnums.WEB.getCode();
		}
		return v.toLowerCase();
	}

	/**
	 * 解析版本名到 version_code：1.3.2 => 10302；纯数字则直接返回。
	 */
	private static Integer parseVersionCode(String version) {
		if (StringUtils.isEmpty(version)) {
			return null;
		}
		String v = version.trim();
		if (v.matches("^\\d+$")) {
			try {
				return Integer.parseInt(v);
			} catch (Exception e) {
				return null;
			}
		}
		String[] parts = v.split("\\.");
		if (parts.length == 0) {
			return null;
		}
		try {
			int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
			int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
			int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
			return major * 10000 + minor * 100 + patch;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取版本多语言信息。
	 */
	private AppVersionI18nEntity resolveI18n(Integer versionId) {
		String language = LanguageContext.getLanguage();
		AppVersionI18nEntity i18n = null;
		if (!StringUtils.isEmpty(language)) {
			i18n = appVersionI18nDao.findByVersionIdAndLangue(versionId, language);
		}
		if (i18n == null && !AppVersionConstants.FALLBACK_LANGUE.equalsIgnoreCase(language)) {
			i18n = appVersionI18nDao.findByVersionIdAndLangue(versionId, AppVersionConstants.FALLBACK_LANGUE);
		}
		if (i18n == null) {
			i18n = appVersionI18nDao.findByVersionIdAndLangue(versionId, "en");
		}
		if (i18n == null) {
			List<AppVersionI18nEntity> list = appVersionI18nDao.findByVersionId(versionId);
			if (list != null && !list.isEmpty()) {
				i18n = list.get(0);
			}
		}
		return i18n;
	}
}
