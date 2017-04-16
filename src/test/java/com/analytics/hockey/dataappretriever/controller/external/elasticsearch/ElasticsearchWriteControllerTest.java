package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapperUtils;
import com.analytics.hockey.dataappretriever.main.injector.AppModule;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;

public class ElasticsearchWriteControllerTest {

	@Spy
	ElasticsearchWriteController spyEsController;

	@Inject
	ElasticsearchWriteController realEsController;

	@Inject
	ElasticsearchReadController dataRetriever;

	@Mock
	Client client;

	@Inject
	PropertyLoader propertyLoader;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		GuiceInjector.getInstance().install(new AppModule());
		GuiceInjector.getInstance().getInjector().injectMembers(this);
	}

	@Test
	public void awaitInitialization_verifyYellowStatusIsChecked_whenNoReplicas() {
		/*** given ***/
		ClusterHealthRequestBuilder clusterHealthRequestBuilder = mock(ClusterHealthRequestBuilder.class);
		int numberOfReplicas = 0;

		/*** when ***/
		doReturn(clusterHealthRequestBuilder).when(spyEsController).prepareHealth(anyString());
		doReturn(numberOfReplicas).when(spyEsController).getNumberOfReplicas();
		doNothing().when(spyEsController).executeClusterHealthRequestBuilder(clusterHealthRequestBuilder);

		spyEsController.awaitInitialization();
		/*** then ***/
		verify(clusterHealthRequestBuilder).setWaitForYellowStatus();
		verify(clusterHealthRequestBuilder, never()).setWaitForGreenStatus();
	}

	@Test
	public void awaitInitialization_verifyGreenStatusIsChecked_whenAtLeastOneReplica() {
		/*** given ***/
		ClusterHealthRequestBuilder clusterHealthRequestBuilder = mock(ClusterHealthRequestBuilder.class);
		int numberOfReplicas = 1;

		doReturn(clusterHealthRequestBuilder).when(spyEsController).prepareHealth(anyString());
		doReturn(numberOfReplicas).when(spyEsController).getNumberOfReplicas();
		doNothing().when(spyEsController).executeClusterHealthRequestBuilder(clusterHealthRequestBuilder);

		/*** when ***/
		spyEsController.awaitInitialization();

		/*** then ***/
		verify(clusterHealthRequestBuilder).setWaitForGreenStatus();
		verify(clusterHealthRequestBuilder, never()).setWaitForYellowStatus();
	}

	private void beforeRealController() {
		realEsController.start();
		realEsController.awaitInitialization();
	}

	@Test
	public void createIndex_successfulCreation() throws IOException, DataStoreException, InterruptedException {
		/*** given ***/
		String index = "testindex";

		/*** when ***/
		beforeRealController();
		realEsController.createIndex(index, true);

		/*** then ***/
		realEsController.refresh(index);
		assertTrue(realEsController.isExists(index));
	}

	@Test
	public void indexDocument_gameDocument_successfulIndexation() throws JsonParseException, JsonMappingException,
	        IOException, DataStoreException, TimeoutException, InterruptedException {
		/*** given ***/

		/**
		 * <pre>
			{  
			   "date":1165017600000,
			   "loser_team":"Minnesota Wild",
			   "winner_team":"Dallas Stars",
			   "is_row":false,
			   "home_team":"Dallas Stars",
			   "score_loser":3,
			   "score_winner":4
			}
		 * </pre>
		 */
		Game game = HockeyScrapperUtils
		        .unmarshallGames(
		                "{\"year\": 2007, \"games\": [{\"home_team\": \"Buffalo Sabres\", \"score_loser\": 1, \"winner_team\": \"Buffalo Sabres\", \"loser_team\": \"New York Islanders\", \"score_winner\": 3, \"is_row\": true}], \"day\": 1, \"month\": 1}")
		        .iterator().next();

		String index = "testindex";
		String type = "testtype";
		/*** when ***/
		beforeRealController();
		realEsController.createIndex(index, true);
		realEsController.refresh(index);

		realEsController.indexDocument(index, type, game);
		realEsController.refresh(index);

		// TODO This is a workaround for CI Travis. Need to inject custom ES
		// elasticsearch.yml in CI environment, with increased memory and throughput.
		//
		// This problem is happening because mapping tasks cannot be forced to be merged
		// like {@link ElasticsearchWriteController#refresh(String)}. There is no out of
		// the box way to wait for all tasks to clear.
		Thread.sleep(1000);

		/*** then ***/

		// Retrieve scores
		dataRetriever.start();
		dataRetriever.awaitInitialization();
		String scores = dataRetriever.getScores(index, type, game.getDate().getYear(), game.getDate().getMonthValue(),
		        game.getDate().getDayOfMonth(), Collections.emptyMap());

		// Transform response
		Map<String, Object> responseMap = HockeyScrapperUtils.responseToMap(scores);

		// Do the checks
		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			Object expected = field.getIndexingValue(game);
			Object actual = responseMap.get(field.getJsonFieldName());

			// When the map is created out of the JSON, an Integer value is used for
			// numeric values, but we have fields that are actually of type short, which
			// makes the test fail. We will transform both to Double, which will cover all
			// future cases
			if (expected instanceof Number) {
				expected = ((Number) expected).doubleValue();
				actual = ((Number) actual).doubleValue();
			}

			assertEquals(expected, actual);
		}
	}
}