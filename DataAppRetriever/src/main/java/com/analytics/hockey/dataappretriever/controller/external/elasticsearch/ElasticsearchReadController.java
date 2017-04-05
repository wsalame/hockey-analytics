package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.GameElasticsearchField;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;

@Singleton
public class ElasticsearchReadController extends AbstractElasticsearchController implements DataRetriever {
	private final int NUMBER_OF_TEAMS = 35;
	
	public ElasticsearchReadController(){
		
	}

	// TODO faire que un ca throw un truc plus generique. faire une nouvelle exception
	// DataReadStoreException ou qqch, dont ESRetrieveException va heriter
	@Override
	public String getTeamNames(int season) throws ElasticsearchRetrieveException {
		QueryBuilder qb = QueryBuilders.matchAllQuery();

		TermsBuilder agg = AggregationBuilders.terms("unique_teams").size(NUMBER_OF_TEAMS)
		        .field(GameElasticsearchField.HOME_TEAM.getJson());

		SearchRequestBuilder fullQuery = getClient().prepareSearch(String.valueOf(season)).addAggregation(agg)
		        .setQuery(qb).setSize(0);

		SearchResponse searchResponse = executeActionGet(fullQuery);

		List<String> teams = new ArrayList<>();
		((StringTerms) searchResponse.getAggregations().iterator().next()).getBuckets().stream()
		        .forEach(bucket -> teams.add(bucket.getKeyAsString()));

		try {
			return ElasticsearchUtils.toJson(ImmutableMap.of("teams", teams));
		} catch (JsonException e) {
			throw new ElasticsearchRetrieveException(e.toString(), e);
		}
	}

	private final int AVERGAGE_DOCUMENT_CHARACTERS = 215; //TODO faire un nouveau component qui build les stats

	public enum Days {
		SUNDAY(3),
		MONDAY(4),
		TUESDAY(7),
		WEDNESDAY(4),
		THURSDAY(7),
		FRIDAY(6),
		SATURDAY(12);

		private final int averageNumberOfGames;

		Days(int averageNumberOfGames) {
			this.averageNumberOfGames = averageNumberOfGames;
		}

		public int getAverageNumberOfGames() {
			return averageNumberOfGames;
		}
	}

	@Override
	public String getScores(int season, int day, int month, int year) {
		final String index = String.valueOf(year);
		final String type = String.valueOf(day) + String.valueOf(month) + String.valueOf(year);

		SearchResponse response = getClient().prepareSearch(index).setTypes(type)
		        .setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();

		// To avoid constant resizing of StringBuilder's internal array, we estimate what
		// will be the total number of characters.
		//
		// The current numbers provided in the constants were arbritrary, but we could
		// actually analyze a large set of days and have better averages
		// Of course, there's the possibility that we will make the array too big many
		// times. We'd have to test the function with several values to find the sweet
		// spot of ideal starting capacity
		int estimatedTotalCapacity = Days.values()[LocalDate.of(year, month, day).getDayOfWeek().ordinal()]
		        .getAverageNumberOfGames() * AVERGAGE_DOCUMENT_CHARACTERS;
		StringBuilder sb = new StringBuilder(estimatedTotalCapacity);
		sb.append("[");
		String separator = ",";
		Arrays.asList(response.getHits().hits()).stream()
		        .forEach(x -> sb.append(x.getSourceAsString()).append(separator));

		sb.delete(sb.length() - separator.length(), sb.length());
		sb.append("]");
		return sb.toString();
	}

	// TODO good edge case season 2005 de 1 oct a 15 oct pour Nashville. ca doit donner
	// 20, mais ca donnais 16 a cause que le endDate etait considere comme less au lieu de
	// less than equal. Probleme du a une mauvaise indexation ?
	@Override
	public String getTotalGoals(int season, String team, Long startEpochMillis, Long endEpochMillis)
	        throws ElasticsearchRetrieveException {
		String totalGoalsFieldName = "totalGoals";
		String winnerGoalsAggName = "winnerGoalsAgg";
		String loserGoalAggName = "loserGoalsAgg";

		BoolQueryBuilder qb = QueryBuilders.boolQuery()
		        .filter(QueryBuilders.boolQuery()
		                .should(QueryBuilders.termQuery(GameElasticsearchField.WINNER_TEAM.getJson(), team))
		                .should(QueryBuilders.termQuery(GameElasticsearchField.LOSER_TEAM.getJson(), team)));

		if (startEpochMillis != null || endEpochMillis != null) {
			RangeQueryBuilder rqb = QueryBuilders.rangeQuery("date");
			if (startEpochMillis != null) {
				rqb.gte(startEpochMillis);
			}

			if (endEpochMillis != null) {
				rqb.lte(endEpochMillis);
			}

			qb.must(rqb);
		}

		FilterAggregationBuilder winnerGoalsAggBuilder = AggregationBuilders.filter(winnerGoalsAggName)
		        .filter(QueryBuilders.termQuery(GameElasticsearchField.WINNER_TEAM.getJson(), team))
		        .subAggregation(AggregationBuilders.sum(totalGoalsFieldName)
		                .field(GameElasticsearchField.SCORE_WINNER.getJson()));

		FilterAggregationBuilder loserGoalsAggBuilder = AggregationBuilders.filter(loserGoalAggName)
		        .filter(QueryBuilders.termQuery(GameElasticsearchField.LOSER_TEAM.getJson(), team))
		        .subAggregation(AggregationBuilders.sum(totalGoalsFieldName)
		                .field(GameElasticsearchField.SCORE_LOSER.getJson()));

		SearchRequestBuilder fullQuery = getClient().prepareSearch(String.valueOf(season))
		        .addAggregation(winnerGoalsAggBuilder).addAggregation(loserGoalsAggBuilder).setQuery(qb).setSize(0);

		SearchResponse searchResponse = executeActionGet(fullQuery);

		double total = ((InternalSum) searchResponse.getAggregations().asMap().get(winnerGoalsAggName)
		        .getProperty(totalGoalsFieldName)).getValue();
		total += ((InternalSum) searchResponse.getAggregations().asMap().get(loserGoalAggName)
		        .getProperty(totalGoalsFieldName)).getValue();

		System.out.println((int) total);
		return null;
	}

	@Override
	public String getTotalGoals(int season, String team) throws ElasticsearchRetrieveException {
		return getTotalGoals(season, team, null, null);
	}

	private SearchResponse executeActionGet(SearchRequestBuilder request) throws ElasticsearchRetrieveException {
		SearchResponse response = null;

		try {
			response = request.execute().actionGet();
		} catch (Exception e) {
			throw new ElasticsearchRetrieveException("TODO", e);
		}

		if ((response.isTerminatedEarly() != null && !response.isTerminatedEarly()) || response.isTimedOut()) {
			throw new ElasticsearchRetrieveException("TODO");
		}

		return response;
	}
}
