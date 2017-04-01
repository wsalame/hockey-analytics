package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchUtils.FieldDatatype;

public interface IsElasticsearchField<T> {
	Object getValue(T o);
	
	FieldDatatype getFieldDatatype();
	
	String getJson();
}
