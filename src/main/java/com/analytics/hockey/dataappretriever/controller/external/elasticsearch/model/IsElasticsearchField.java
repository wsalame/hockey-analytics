package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.FieldDatatype;

public interface IsElasticsearchField<T> {
	Object getIndexingValue(T o);
	
	FieldDatatype getFieldDatatype();
	
	String getJsonFieldName();
}
