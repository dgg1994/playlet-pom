package com.playlet.internal.utils;

import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务辅助：在 catch 后仍需返回业务错误码时，显式标记回滚。
 */
public final class TransactionUtils {

	private TransactionUtils() {
	}

	/** 当前存在事务时标记 rollback-only，避免 catch+return 误提交 */
	public static void markRollbackOnly() {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
	}
}
