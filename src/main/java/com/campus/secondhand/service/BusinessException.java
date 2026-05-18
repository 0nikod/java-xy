package com.campus.secondhand.service;

/**
 * 业务规则校验失败时抛出的运行时异常。
 */
public class BusinessException extends RuntimeException {

	/**
	 * 创建业务异常。
	 */
	public BusinessException(String message) {
		super(message);
	}
}
