package com.analytics.hockey.dataappretriever.elasticsearch.model;

import com.analytics.hockey.dataappretriever.elasticsearch.ElasticsearchUtils.FieldDatatype;

public interface IsElasticsearchField<T> {
	Object getValue(T o);
	
	FieldDatatype getFieldDatatype();
	
	String getJson();
}
