package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception;

public class DataStoreException extends Exception {

	private static final long serialVersionUID = 3516336347415481456L;

	public DataStoreException(Exception e) {
		super(e);
	}

	public DataStoreException(String message) {
		super(message);
	}

	public DataStoreException(String message, Exception e) {
		super(message, e);
	}
}
