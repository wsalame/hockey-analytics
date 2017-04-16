package com.analytics.hockey.dataappretriever.service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class GoogleUrl extends GenericUrl {

	  @Key
	  private String fields;

	  public GoogleUrl(String encodedUrl) {
	    super(encodedUrl);
	  }

	  /**
	   * @return the fields
	   */
	  public String getFields() {
	    return fields;
	  }

	  /**
	   * @param fields the fields to set
	   */
	  public void setFields(String fields) {
	    this.fields = fields;
	  }
}
