package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.analytics.hockey.dataappretriever.model.DataIndexer;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.GameElasticsearchField;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;

@Singleton
public class ElasticsearchWriteController extends AbstractElasticsearchController implements DataIndexer {

	private final Logger logger = LogManager.getLogger(this.getClass());

	private final String SEPARATOR = "/"; // TODO Less ghetto way

	/**
	 * We assume we will never delete an index during normal operations, so it is safe to
	 * store if the mapping already exists or not.
	 */
	private LoadingCache<String, Boolean> mappingParametersCache = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.DAYS).build(new CacheLoader<String, Boolean>() {
		        @Override
		        public Boolean load(String index_and_type) {
			        String[] split = index_and_type.split(SEPARATOR); // TODO less ghetto
			                                                          // way
			        String index = split[0];
			        String type = split[1];
			        final TypesExistsResponse res = getClient().admin().indices().prepareTypesExists(index)
			                .setTypes(type).execute().actionGet();

			        return res.isExists();
		        }
	        });

	public ElasticsearchWriteController() {

	}

	@Override
	public void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException {
		final IndicesExistsResponse res = getClient().admin().indices().prepareExists(indexName).execute().actionGet();
		if (res.isExists() && deleteOldIndexIfExists) {
			deleteIndex(indexName);
		}

		if (!res.isExists() || (res.isExists() && deleteOldIndexIfExists)) {
			XContentBuilder settingsBuilder = null;
			settingsBuilder = XContentFactory.jsonBuilder().startObject();

			/**
			 * Dynamic mapping is disabled to force user to define explicit mapping when
			 * creating new type, which, among others, will reduce risk of bugs. For
			 * example, if we index a list of documents, and the first document happens to
			 * have a NULL value, ES will decide for us which datatype it is (String, int,
			 * etc.). In other words, ES could pick the wrong datatype, and subsequent
			 * documents will fail indexing.
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
				System.err.println("Could not create index");
				throw new RuntimeException();
			}
		}
	}

	private void putMappingIfNotExists(String index, String type) {
		try {
			if (!mappingParametersCache.get(index + SEPARATOR + type)) {
				String mapping = ElasticsearchUtils.buildMappingParametersAsJson(GameElasticsearchField.values());
				getClient().admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute()
				        .actionGet(); // We want to wait for the mapping to be
				                      // acknowledged and added to the buffer
			}
		} catch (IOException | ExecutionException e) {
			logger.error(e, e);
		}
	}

	@Override
	public void insertGame(Game game) throws Exception {
		LocalDate date = game.getDate();

		Integer day = date.getDayOfMonth();
		Integer month = date.getMonthValue();
		Integer year = date.getYear();

		final String index = year.toString();
		final String type = day.toString() + month.toString() + year.toString();
		try {
			putMappingIfNotExists(index, type);
			String documentAsJson = ElasticsearchUtils.toJson(game.buildDocument());

			// Sending the document asynchronously
			getClient().prepareIndex(index, type).setSource(documentAsJson).execute();

		} catch (Exception e) {
			logger.error("Could not insert game in " + index + "/" + "type");
			throw e;
		}
	}

	private void deleteIndex(String indexName) {
		DeleteIndexResponse deleteIndexResponse = getClient().admin().indices().prepareDelete(indexName).execute()
		        .actionGet();
		if (!deleteIndexResponse.isAcknowledged()) {
			System.err.println("Could not delete index");
			throw new RuntimeException();
		}
	}
}