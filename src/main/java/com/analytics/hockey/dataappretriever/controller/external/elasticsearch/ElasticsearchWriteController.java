package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ElasticsearchWriteController extends AbstractElasticsearchWriteController {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final String INDEX_TYPE_SEPARATOR = "/";

	// We assume we will never delete an index during normal operations, so it is safe to
	// store if the mapping already exists or not.
	private LoadingCache<String, Boolean> gamesMappingParametersCache = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.DAYS).build(new CacheLoader<String, Boolean>() {
		        @Override
		        public Boolean load(String index_and_type) {
			        // Following ES' convention <index>/<type>/<id>
			        String[] split = index_and_type.split(INDEX_TYPE_SEPARATOR);
			        String index = split[0];
			        String type = split[1];
			        final TypesExistsResponse res = getClient().admin().indices().prepareTypesExists(index)
			                .setTypes(type).execute().actionGet();

			        return res.isExists();
		        }
	        });

	@Inject
	public ElasticsearchWriteController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
	}

	public ElasticsearchWriteController() {

	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException, DataStoreException {
		boolean isExists = isExists(indexName); // Blocking call
		if (isExists && deleteOldIndexIfExists) {
			deleteIndex(indexName);
		}

		if (!isExists || (isExists && deleteOldIndexIfExists)) {
			XContentBuilder settingsBuilder = null;
			settingsBuilder = XContentFactory.jsonBuilder().startObject();

			/**
			 * Dynamic mapping is disabled to force user to define explicit mapping when
			 * creating new type, which, among others, will reduce risk of bugs. For
			 * example, if we index a list of documents, and the first document happens to
			 * have a NULL value in one of its attribute, ES will decide for us which
			 * datatype it is (String, int, etc.). In other words, ES could pick the wrong
			 * datatype, and subsequent documents will fail indexing.
			 * 
			 * The number of shards and replicas depends on our infrastructure and how big
			 * the data set is. If we have only one node, then it might be not smart to
			 * add replicas. If we add a replica, they will just fight for resources (i.e
			 * : CPU scheduling, memory). Although, there is a point that we might want a
			 * failover solution, if reindexing a whole new shard is complicated and/or
			 * too long. In other words, ideally, we'd have two nodes.
			 */
			settingsBuilder.startObject("index").field("number_of_shards", 1).field("number_of_replicas", 1)
			        .field("mapper.dynamic", false);

			settingsBuilder.endObject();

			CreateIndexRequestBuilder createIndexRequestBuilder = getClient().admin().indices().prepareCreate(indexName)
			        .setSettings(settingsBuilder.string());

			CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
			if (!response.isAcknowledged()) {
				logger.error("Could not create index");
				throw new DataStoreException("Could not create index");
			}
		}
	}

	private void putMapping(String index, String type, String mapping) {
		getClient().admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute().actionGet(); // We
	}

	private void putMappingIfNotExists(String index, String type) {
		try {
			if (!gamesMappingParametersCache.get(index + INDEX_TYPE_SEPARATOR + type)) {
				String mapping = ElasticsearchUtils.buildMappingParametersAsJson(GameElasticsearchField.values());
				putMapping(index, type, mapping);
				gamesMappingParametersCache.put(index + INDEX_TYPE_SEPARATOR + type, true);
			}
		} catch (IOException | ExecutionException e) {
			logger.error(e, e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void deleteIndex(String indexName) throws DataStoreException {
		DeleteIndexResponse deleteIndexResponse = getClient().admin().indices().prepareDelete(indexName).execute()
		        .actionGet();
		if (!deleteIndexResponse.isAcknowledged()) {
			logger.error("Could not delete index");
			throw new DataStoreException("Could not delete index");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void indexDocument(IsElasticsearchIndexable indexableObject) throws DataStoreException {
		final String index = indexableObject.buildIndex();
		final String type = indexableObject.buildType();

		indexDocument(index, type, indexableObject);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void indexDocument(String index, String type, IsElasticsearchIndexable indexableObject)
	        throws DataStoreException {
		try {
			putMappingIfNotExists(index, type);
			String documentAsJson = ElasticsearchUtils.toJson(indexableObject.buildDocument());

			// Sending the document asynchronously
			IndexRequestBuilder requestBuilder = getClient().prepareIndex(index, type).setSource(documentAsJson);
			requestBuilder.execute();
		} catch (Exception e) {
			System.out.println("ERROR");
			logger.error("Could not insert game in " + index + "/" + "type");
			throw new DataStoreException("Could not insert game in " + index + "/" + "type");
		}
	}
}