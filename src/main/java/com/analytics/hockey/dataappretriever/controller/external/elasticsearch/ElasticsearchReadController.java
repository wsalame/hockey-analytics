package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.extractor.PerOpponenentStatisticsExtractor;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query.AggregationPerOpponentBuilder;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query.QueryParameters;
import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.HasDataStatistics;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.analytics.hockey.dataappretriever.model.StatsField;
import com.analytics.hockey.dataappretriever.model.Team;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ElasticsearchReadController extends AbstractElasticsearchReadController {

	private final String separator = ",";
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final HasDataStatistics statsLoader;
	private final int DEFAULT_SIZE = 10;
	private final int MAX_WINDOW_SIZE;

	@Inject
	public ElasticsearchReadController(HasDataStatistics statsLoader, PropertyLoader propertyLoader) {
		this.statsLoader = statsLoader;
		this.propertyLoader = propertyLoader;
		this.MAX_WINDOW_SIZE = propertyLoader.getPropertyAsInteger(PropertyConstant.ES_MAX_WINDOW_SIZE.toString());
	}

	@VisibleForTesting
	ElasticsearchReadController() {
		this.statsLoader = null;
		this.MAX_WINDOW_SIZE = -1;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getTeams(String season, Map<String, Object> params) throws DataStoreException {
		List<String> teams = new ArrayList<>();
		QueryParameters queryParameters = QueryParameters.toQueryParameters(params);

		// If it's for a defined season, return the exact names for given season. The
		// returned name will be the "current" name for the given season. For example, in
		// 2007-2007, it was the "Coyotes Phoenix", and not "Coyotes Arizona"
		if (season != null) {
			TermsBuilder agg = AggregationBuilders.terms("unique_teams")
			        .field(GameElasticsearchField.HOME_TEAM.getJsonFieldName())
			        .size(Optional.ofNullable(queryParameters.getSize()).orElse(statsLoader.getNumberOfTeams()));

			BoolQueryBuilder qb = CustomBoolQueryBuilder.boolQueryBuilder().rangeIfRequired(queryParameters.getRange());

			SearchRequestBuilder fullQuery = getClient().prepareSearch(season).addAggregation(agg).setQuery(qb);

			SearchResponse response = executeActionGet(fullQuery);

			((StringTerms) response.getAggregations().iterator().next()).getBuckets().stream()
			        .forEach(bucket -> teams.add(bucket.getKeyAsString()));
			try {
				return ElasticsearchUtils.toJson(ImmutableMap.of("teams", teams));
			} catch (JsonException e) {
				throw new ElasticsearchRetrieveException("Error serializing teams", e);
			}
		} else {
			// Otherwise return all time names, where an object will have both the current
			// name and the past names
			SearchRequestBuilder fullQuery = getClient().prepareSearch(new Team().buildIndex()).setQuery(QueryBuilders.matchAllQuery())
			        .setSize(Optional.ofNullable(queryParameters.getSize()).orElse(statsLoader.getNumberOfTeams()));

			SearchResponse response = executeActionGet(fullQuery);

			int estimatedTotalCapacity = statsLoader.getAverageTeamDocumentCharacters()
			        * statsLoader.getNumberOfTeams();
			
			return joinSourceHits(response, separator, estimatedTotalCapacity);
		}
	}

	private int getSizeOrDefault(QueryParameters queryParameters, int defaultSize) {
		return Optional.ofNullable(queryParameters.getSize()).orElse(defaultSize);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getScores(Integer year, Integer month, Integer day, Map<String, Object> params)
	        throws DataStoreException {
		final String index = new Game().buildGamesIndex(year, month);
		final String type = new Game().buildType(month);

		return getScores(index, type, year, month, day, params);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getScores(String index, String type, Integer year, Integer month, Integer day,
	        Map<String, Object> params) throws DataStoreException {
		QueryParameters queryParameters = QueryParameters.toQueryParameters(params);

		SearchResponse response = getClient().prepareSearch(index).setTypes(type)
		        .setSize(getSizeOrDefault(queryParameters, MAX_WINDOW_SIZE)).setQuery(QueryBuilders.matchAllQuery())
		        .execute().actionGet();

		return joinSourceHits(response, separator,
		        Optional.ofNullable(estimateTotalCapacity(year, month, day)).orElse(16));
	}

	/**
	 * @see {@link HasDataStatistics}
	 */
	private Integer estimateTotalCapacity(Integer year, Integer month, Integer day) {
		Integer estimatedTotalCapacity = null;
		if (month != null && day != null) {
			estimatedTotalCapacity = statsLoader
			        .getAverageNumberOfGamesForGivenDay(LocalDate.of(year, month, day).getDayOfWeek().ordinal())
			        * statsLoader.getAverageGameDocumentCharacters();
		}

		return estimatedTotalCapacity;
	}

	public int getTotalGoals(String season, String team, Map<String, Object> params) throws DataStoreException {
		String totalGoalsFieldName = "totalGoals";
		String winnerGoalsAggName = "winnerGoalsAgg";
		String loserGoalAggName = "loserGoalsAgg";

		BoolQueryBuilder qb = CustomBoolQueryBuilder.boolQueryBuilder().filterTeam(team)
		        .rangeIfRequired(QueryParameters.toQueryParameters(params));

		FilterAggregationBuilder winnerGoalsAggBuilder = AggregationBuilders.filter(winnerGoalsAggName)
		        .filter(QueryBuilders.termQuery(GameElasticsearchField.WINNER_TEAM.getJsonFieldName(), team))
		        .subAggregation(AggregationBuilders.sum(totalGoalsFieldName)
		                .field(GameElasticsearchField.SCORE_WINNER.getJsonFieldName()));

		FilterAggregationBuilder loserGoalsAggBuilder = AggregationBuilders.filter(loserGoalAggName)
		        .filter(QueryBuilders.termQuery(GameElasticsearchField.LOSER_TEAM.getJsonFieldName(), team))
		        .subAggregation(AggregationBuilders.sum(totalGoalsFieldName)
		                .field(GameElasticsearchField.SCORE_LOSER.getJsonFieldName()));

		SearchRequestBuilder fullQuery = getClient().prepareSearch(String.valueOf(season))
		        .addAggregation(winnerGoalsAggBuilder).addAggregation(loserGoalsAggBuilder).setQuery(qb).setSize(0);

		SearchResponse searchResponse = executeActionGet(fullQuery);

		double total = ((InternalSum) searchResponse.getAggregations().asMap().get(winnerGoalsAggName)
		        .getProperty(totalGoalsFieldName)).getValue();
		total += ((InternalSum) searchResponse.getAggregations().asMap().get(loserGoalAggName)
		        .getProperty(totalGoalsFieldName)).getValue();

		return (int) total;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getStats(String season, String team, Map<String, Object> params) throws DataStoreException {
		final String BY_TEAM_MAIN_AGG = "byTeamMainAgg";
		final String GOALS_BY_TEAM_AGG_NAME = "goals";
		final String WINS_BY_TEAM_AGG_NAME = "winsByTeamAgg";

		QueryParameters queryParameters = QueryParameters.toQueryParameters(params);
		/*****************************
		 * Query part
		 ******************************/
		BoolQueryBuilder qb = CustomBoolQueryBuilder.boolQueryBuilder().filterTeam(team)
		        .rangeIfRequired(queryParameters.getRange());

		/*****************************
		 * Aggregations per team related stats
		 ******************************/
		TermsBuilder goalsAggBuilder = new AggregationPerOpponentBuilder(GOALS_BY_TEAM_AGG_NAME,
		        GameElasticsearchField.LOSER_TEAM, StatsField.GOALS_FOR, GameElasticsearchField.SCORE_WINNER)
		                .sizeIfRequired(queryParameters, DEFAULT_SIZE).sortIfRequired(queryParameters);

		TermsBuilder winsAggBuilder = new AggregationPerOpponentBuilder(WINS_BY_TEAM_AGG_NAME,
		        GameElasticsearchField.LOSER_TEAM, StatsField.WINS,
		        GameElasticsearchField.IS_REGULATION_OR_OVERTIME_WIN).sizeIfRequired(queryParameters, DEFAULT_SIZE)
		                .sortIfRequired(queryParameters);

		FilterAggregationBuilder byTeamAgg = AggregationBuilders.filter(BY_TEAM_MAIN_AGG)
		        .filter(QueryBuilders.termQuery(GameElasticsearchField.WINNER_TEAM.getJsonFieldName(), team))
		        .subAggregation(goalsAggBuilder).subAggregation(winsAggBuilder);

		/*****************************
		 * Execution part
		 ******************************/
		SearchRequestBuilder fullQuery = getClient().prepareSearch(season).setQuery(qb).addAggregation(byTeamAgg)
		        .setSize(0);

		SearchResponse searchResponse = executeActionGet(fullQuery);

		/*****************************
		 * Extraction of the aggregations
		 ******************************/
		// Per opponent stats
		Map<String, Object> statsPerOpponent = new PerOpponenentStatisticsExtractor(BY_TEAM_MAIN_AGG,
		        GOALS_BY_TEAM_AGG_NAME, WINS_BY_TEAM_AGG_NAME, searchResponse.getAggregations())
		                .generateGoalsPerOpponent(queryParameters).generateWinsPerOpponent(queryParameters)
		                .buildStats();

		Map<String, Object> outerMap = new LinkedHashMap<>();

		if (queryParameters.getFields().contains(StatsField.GOALS_FOR)) {
			int totalGoals = getTotalGoals(season, team, params);
			outerMap.put(StatsField.GOALS_FOR.toString(), totalGoals);
		}

		outerMap.put("stats_per_opponent", statsPerOpponent);
		try {
			String resultJson = ElasticsearchUtils.toJson(outerMap);
			return resultJson;
		} catch (JsonException e) {
			logger.error(e.toString(), e);
			throw new DataStoreException("Error serializing the results");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getStandings(String season, String team, Map<String, Object> params) throws DataStoreException {
		throw new UnsupportedOperationException("Missing implementation");
	}
}