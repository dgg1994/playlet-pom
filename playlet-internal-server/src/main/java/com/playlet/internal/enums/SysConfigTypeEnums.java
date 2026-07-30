package com.playlet.internal.enums;

import com.playlet.internal.entity.system.DicEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @category 三方接口状态码类型
 * @author Hlin
 *
 */
public enum SysConfigTypeEnums {
	USER_AGREEMENT(1, "user_agreement", "用户协议", 1, "用户协议", "User Agreement", "用戶協議", "User Agreement", "ユーザー契約",
			"Kullanıcı Sözleşmesi", "Contrato do Usuário"),
	PRIVATE_AGREEMENT(2, "private_agreement", "隐私协议", 1, "隐私协议", "Privacy Policy", "隱私協議", "Privacy Policy",
			"プライバシーポリシー", "Gizlilik Politikası", "política de Privacidade"),
	ABOUT_US(3, "about_us", "关于我们", 1, "关于我们", "about Us", "關於我們", "about Us", "私たちについて", "Hakkımızda", "sobre nós"),
	CONTACT_US(4, "contact_us", "联系我们", 1, "联系我们", "Contact Us", "聯絡我們", "Contact Us", "お問い合わせ", "Bize Ulaşın",
			"Contate-nos"),
	SUPPORT(5, "support", "客服", 2, "客服", "customer service", "客服", "customer service", "カスタマーサービス",
			"Müşteri Hizmetleri", "atendimento ao Cliente");

	private Integer index;

	private String name;

	private String lable;

	private Integer type;

	private String zhName;

	private String enName;

	private String twName;

	private String koName; // 韩语

	private String jaName; // 日语

	private String trName; // 土耳其语

	private String ptName;

	private SysConfigTypeEnums(Integer index, String name, String lable, Integer type, String zhName, String enName,
                               String twName, String koName, String jaName, String trName, String ptName) {
		this.index = index;
		this.name = name;
		this.lable = lable;
		this.type = type;
		this.zhName = zhName;
		this.enName = enName;
		this.twName = twName;
		this.koName = koName;
		this.jaName = jaName;
		this.trName = trName;
		this.ptName = ptName;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getZhName() {
		return zhName;
	}

	public void setZhName(String zhName) {
		this.zhName = zhName;
	}

	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public String getTwName() {
		return twName;
	}

	public void setTwName(String twName) {
		this.twName = twName;
	}

	public String getKoName() {
		return koName;
	}

	public void setKoName(String koName) {
		this.koName = koName;
	}

	public String getJaName() {
		return jaName;
	}

	public void setJaName(String jaName) {
		this.jaName = jaName;
	}

	public String getTrName() {
		return trName;
	}

	public void setTrName(String trName) {
		this.trName = trName;
	}

	public String getPtName() {
		return ptName;
	}

	public void setPtName(String ptName) {
		this.ptName = ptName;
	}

	public static List<DicEntity> getProtocolType() {
		SysConfigTypeEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getType() == 1) {
				DicEntity dicEntity = new DicEntity();
				dicEntity.setId(typeEnum.getIndex());
				dicEntity.setName(typeEnum.getLable());
				dicEntity.setLable(typeEnum.getType().toString());
				list.add(dicEntity);
			}
		}
		return list;
	}

	public static List<DicEntity> getJumpAddress() {
		SysConfigTypeEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getType() == 2) {
				DicEntity dicEntity = new DicEntity();
				dicEntity.setId(typeEnum.getIndex());
				dicEntity.setName(typeEnum.getLable());
				list.add(dicEntity);
			}
		}
		return list;
	}

	public static List<DicEntity> getLists() {
		SysConfigTypeEnums[] typeEnums = values();
		List<DicEntity> list = new ArrayList<>();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			DicEntity dicEntity = new DicEntity();
			dicEntity.setId(typeEnum.getIndex());
			dicEntity.setName(typeEnum.getLable());
			list.add(dicEntity);
		}
		return list;
	}

	public static String getName(int i) {
		SysConfigTypeEnums[] typeEnums = values();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getIndex().equals(i)) {
				return typeEnum.getLable();
			}
		}
		return null;
	}

	public static Integer getLable(int i) {
		SysConfigTypeEnums[] typeEnums = values();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getIndex().equals(i)) {
				return typeEnum.getType();
			}
		}
		return null;
	}

	public static String getType(int i) {
		SysConfigTypeEnums[] typeEnums = values();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getIndex().equals(i)) {
				return typeEnum.getName();
			}
		}
		return null;
	}

	public static String getLanguageName(int i, String language) {
		SysConfigTypeEnums[] typeEnums = values();
		for (SysConfigTypeEnums typeEnum : typeEnums) {
			if (typeEnum.getIndex().equals(i)) {
				if (LanguageEnums.EN_US.getName().equals(language)) {
					return typeEnum.getEnName();
				} else if (LanguageEnums.ZH_CN.getName().equals(language)) {
					return typeEnum.getZhName();
				} else if (LanguageEnums.ZH_TW.getName().equals(language)) {
					return typeEnum.getTwName();
				} else if (LanguageEnums.KO.getName().equals(language)) { // 韩语
					return typeEnum.getKoName();
				} else if (LanguageEnums.JA.getName().equals(language)) { // 日语
					return typeEnum.getJaName();
				} else if (LanguageEnums.TR.getName().equals(language)) { // 土耳其语
					return typeEnum.getTrName();
				} else if (LanguageEnums.PT_BR.getName().equals(language)) { // 巴西
					return typeEnum.getTrName();
				}
			}
		}
		return null;
	}

}