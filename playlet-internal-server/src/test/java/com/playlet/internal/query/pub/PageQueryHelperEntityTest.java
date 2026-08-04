package com.playlet.internal.query.pub;

import com.playlet.internal.constants.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageQueryHelperEntityTest {

	@Test
	void clampsPageSizeAndDefaultsPageNumber() {
		PageQueryHelperEntity page = new PageQueryHelperEntity();
		page.setPageSize(10000);
		page.setPageNumber(0);
		assertEquals(Constants.MAX_PAGESIZE, page.getPageSize());
		assertEquals(Constants.PAGENUMBER, page.getPageNumber());
	}

	@Test
	void defaultsWhenNull() {
		PageQueryHelperEntity page = new PageQueryHelperEntity();
		page.setPageSize(null);
		page.setPageNumber(null);
		assertEquals(Constants.PAGESIZE, page.getPageSize());
		assertEquals(Constants.PAGENUMBER, page.getPageNumber());
	}

	@Test
	void keepsValidSize() {
		PageQueryHelperEntity page = new PageQueryHelperEntity();
		page.setPageSize(20);
		page.setPageNumber(3);
		assertEquals(20, page.getPageSize());
		assertEquals(3, page.getPageNumber());
	}
}
