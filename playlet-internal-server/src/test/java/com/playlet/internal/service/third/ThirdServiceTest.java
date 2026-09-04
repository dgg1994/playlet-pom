package com.playlet.internal.service.third;

import com.playlet.internal.PlayletInternalServerApplication;
import com.playlet.internal.api.request.BankcardApplyRequest;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.response.KycCountryResp;
import com.playlet.internal.api.response.KycStatusResp;
import com.playlet.internal.api.response.ThirdBankcardBalanceResp;
import com.playlet.internal.api.response.ThirdBankcardProductResp;
import com.playlet.internal.api.response.ThirdUserBankcardResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = PlayletInternalServerApplication.class)
class ThirdServiceTest {

	@Autowired
	private ThirdService thirdService;

	@Test
	void registerUser() {
		System.out.println(thirdService.registerUser("2451970800@qq.com", ""));
	}

	@Test
	void listKycCountries() {
		List<KycCountryResp> list = thirdService.listKycCountries(null);
		System.out.println(list);
	}

	@Test
	void getKycStatus() {
		KycStatusResp status = thirdService.getKycStatus(102430L);
		System.out.println(status);
	}

	@Test
	void applyKyc() {
		KycApplyRequest body = new KycApplyRequest();
		body.setFirstName("SAN");
		body.setLastName("ZHANG");
		body.setIdNo("1234567890");
		body.setEmail("123456@gmail.com");
		body.setNationCode("SGP");
		body.setCertType(1);
		body.setIdUrl("https://example.com/id-front.jpg");
		body.setIdBackUrl("https://example.com/id-back.jpg");
		body.setBirthday("2000-11-26");
		body.setCountryCode("SGP");
		body.setAreaCode("86");
		body.setPhone("15611230048");
		thirdService.applyKyc(102430L, body);
	}

	@Test
	void listCardProducts() {
		List<ThirdBankcardProductResp> list = thirdService.listCardProducts();
		System.out.println(list);
	}

	@Test
	void listUserCards() {
		List<ThirdUserBankcardResp> list = thirdService.listUserCards(102430L);
		System.out.println(list);
	}

	@Test
	void applyBankcard() {
		BankcardApplyRequest body = new BankcardApplyRequest();
		body.setProductId(1000);
		System.out.println(thirdService.applyBankcard(102430L, body));
	}

	@Test
	void getBankcardBalance() {
		ThirdBankcardBalanceResp balance = thirdService.getBankcardBalance(102430L, 101934L);
		System.out.println(balance);
	}

}
