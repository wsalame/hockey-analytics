package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import java.util.Map;

public interface IsElasticsearchIndexable {
	Map<String, Object> buildDocument();
}
