package com.analytics.hockey.dataappretriever.exception;

public class JsonException extends Exception {

	private static final long serialVersionUID = -1960133156046764852L;

	public JsonException(String message, Exception e) {
		super(message, e);
	}
}
