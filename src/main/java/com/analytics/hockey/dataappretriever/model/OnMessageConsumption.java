package com.analytics.hockey.dataappretriever.model;

public interface OnMessageConsumption<T> {
	T execute(byte[] body, Object... args) throws Exception;
}
