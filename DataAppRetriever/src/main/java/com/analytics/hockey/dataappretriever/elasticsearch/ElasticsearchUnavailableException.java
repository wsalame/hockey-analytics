package com.analytics.hockey.dataappretriever.elasticsearch;

public class ElasticsearchUnavailableException extends Exception {
	private static final long serialVersionUID = 1L;

	public ElasticsearchUnavailableException(Exception e) {
		super(e);
	}

	public ElasticsearchUnavailableException(String message) {
		super(message);
	}
}