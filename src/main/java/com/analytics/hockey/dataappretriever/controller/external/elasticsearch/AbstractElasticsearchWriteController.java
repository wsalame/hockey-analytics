package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;

import com.analytics.hockey.dataappretriever.model.DataIndexer;

public abstract class AbstractElasticsearchWriteController extends AbstractElasticsearchController
        implements DataIndexer {

	protected boolean isExists(String index) {
		return getClient().admin().indices().prepareExists(index).execute().actionGet().isExists();
	}

	protected RefreshResponse refresh(String... indices) {
		return getClient().admin().indices().prepareRefresh(indices).execute().actionGet();
	}
}
