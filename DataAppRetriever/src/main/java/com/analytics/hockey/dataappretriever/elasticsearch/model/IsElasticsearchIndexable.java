package com.analytics.hockey.dataappretriever.elasticsearch.model;

import java.util.Map;

public interface IsElasticsearchIndexable {
	Map<String, Object> buildDocument();
}
