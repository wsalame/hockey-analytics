package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchWriteController;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.google.inject.ImplementedBy;

@ImplementedBy(ElasticsearchWriteController.class)
public interface DataIndexer extends IsConnected {
	/**
	 * Creates the Elasticsearch index, with possibility of deleting previous index. If we
	 * decide to not delete the previous index, and the index exists, the operation is
	 * simply ignored.
	 * 
	 * @param indexName
	 *            The index name
	 * @param deleteOldIndexIfExists
	 *            If true, delete index that matches the same name. If false, ignore
	 *            operation.
	 * @throws DataStoreException
	 * @throws IOException 
	 */
	void createIndex(String indexName, boolean deleteOldIndexIfExists) throws DataStoreException, IOException;

	void indexDocument(IsElasticsearchIndexable o) throws DataStoreException;

	void deleteIndex(String indexName) throws DataStoreException; 
}
