package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception;

public class ElasticsearchRetrieveException extends Exception {
	  private static final long serialVersionUID = 3438888609545661068L;

	  public ElasticsearchRetrieveException(Exception e) {
	    super(e);
	  }

	  public ElasticsearchRetrieveException(String message) {
	    super(message);
	  }

	  public ElasticsearchRetrieveException(String message, Exception e) {
	    super(message + "\n" + e.toString(), e);
	  }
	}
