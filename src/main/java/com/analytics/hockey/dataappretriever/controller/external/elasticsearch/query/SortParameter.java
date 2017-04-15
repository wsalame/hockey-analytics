package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class SortParameter {
	// We don't know which fields the user is going to use, so we store the key,value
	// pair in map.
	private Map<String, String> sorts = new LinkedHashMap<>();

	public SortParameter() {

	}

	@JsonAnyGetter
	public Map<String, String> getSorts() {
		return sorts;
	}

	@JsonAnySetter
	public void set(String name, String value) {
		sorts.put(name, value);
	}
}
