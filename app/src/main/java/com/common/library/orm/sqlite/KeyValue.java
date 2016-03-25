package com.common.library.orm.sqlite;

/**
 * A simple key-value container class.
 * 
 * @author zhangfei
 * 
 */
final class KeyValue {
	public final String key;
	public final Object value;

	public KeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}
}
