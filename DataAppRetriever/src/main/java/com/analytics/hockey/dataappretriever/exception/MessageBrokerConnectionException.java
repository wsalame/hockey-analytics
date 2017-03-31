package com.analytics.hockey.dataappretriever.exception;

public class MessageBrokerConnectionException extends Exception {
	private static final long serialVersionUID = 9211050772024169872L;

	public MessageBrokerConnectionException(String message, Exception e) {
		super(message, e);
	}

}
