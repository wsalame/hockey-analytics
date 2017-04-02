package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.Game.GameElasticsearchField;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;

@Singleton
public class ElasticsearchWriteController {

	private final static Logger logger = LogManager.getLogger(ElasticsearchWriteController.class);

	public ElasticsearchWriteController() {

	}

	public Client getClient() {
		return ElasticsearchClientManager.getInstance().getClient();
	}

	public void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException {
		final IndicesExistsResponse res = getClient().admin().indices().prepareExists(indexName).execute().actionGet();
		if (res.isExists() && deleteOldIndexIfExists) {
			deleteIndex(indexName);
		}

		if (!res.isExists() || (res.isExists() && deleteOldIndexIfExists)) {
			XContentBuilder settingsBuilder = null;
			settingsBuilder = XContentFactory.jsonBuilder().startObject();
			
			/**
			 * Dynamic mapping is disabled to force user to define explicit mapping when creating new type, which, among
			 * others, will reduce risk of bugs. For example, if we index a list of documents, and the first document
			 * happens to have a NULL value, ES will decide for us which datatype it is (String, int, etc.). In other
			 * words, ES could pick the wrong datatype, and subsequent documents will fail indexing.
			 */
			settingsBuilder.startObject("index").field("number_of_shards", 1).field("number_of_replicas", 0)
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

	/**
	 * We assume we will never delete an index during normal operations, so it is safe to store if the mapping already
	 * exists or not.
	 */
	private volatile LoadingCache<String, Boolean> mappingParametersExistsCache = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.DAYS).build(new CacheLoader<String, Boolean>() {
		        @Override
		        public Boolean load(String index_and_type) {
			        String[] split = index_and_type.split(SEPARATOR); // TODO less ghetto way
			        String index = split[0];
			        String type = split[1];
			        final TypesExistsResponse res = getClient().admin().indices().prepareTypesExists(index)
			                .setTypes(type).execute().actionGet();

			        return res.isExists();
		        }
	        });

	private final String SEPARATOR = "/"; // TODO Less ghetto way

	public void putMappingIfNotExists(String index, String type) {
		try {
			if (!mappingParametersExistsCache.get(index + SEPARATOR + type)) {
				String mapping = ElasticsearchUtils.buildMappingParametersAsJson(GameElasticsearchField.values());
				getClient().admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute()
				        .actionGet(); // We want to wait for the mapping to be added
			}
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertGame(Game game, String index, String type) throws Exception {
		try {
			String documentAsJson = toJson(game.buildDocument());

			getClient().prepareIndex(index, type).setSource(documentAsJson).execute(); // Async by nature
		} catch (Exception e) {
			logger.error("Could not insert game in " + index + "/" + "type");
			throw e;
		}
	}

	public String getScoresAsJson(Date date, String index, String type, String id) {

		GetResponse response = getDocumentGetResponse(index, type, id);
		return response.getSourceAsString();
	}

	private void deleteIndex(String indexName) {
		DeleteIndexResponse deleteIndexResponse = getClient().admin().indices().prepareDelete(indexName).execute()
		        .actionGet();
		if (!deleteIndexResponse.isAcknowledged()) {
			System.err.println("Could not delete index");
			throw new RuntimeException();
		}
	}

	private GetResponse getDocumentGetResponse(String indexName, String type, String id) {
		return getClient().prepareGet(indexName, type, id).execute().actionGet();
	}

	public static String toJson(Map<String, Object> document) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(document);
	}
}