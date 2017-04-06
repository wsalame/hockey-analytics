package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception;

import java.io.IOException;

public class ElasticsearchUnavailableException extends IOException {
	private static final long serialVersionUID = 1L;

	public ElasticsearchUnavailableException(Exception e) {
		super(e);
	}

	public ElasticsearchUnavailableException(String message) {
		super(message);
	}
}