package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

public interface OnMessageConsumption<T> {
	T execute(byte[] body, Object... args) throws Exception;
}
