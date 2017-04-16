package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.extractor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.HasAggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query.QueryParameters;
import com.analytics.hockey.dataappretriever.model.StatsField;

public class PerOpponenentStatisticsExtractor {
	private final String mainAggName;
	private final String goalsPerOpponentAggName;
	private final String winsPerOpponentAggName;
	private final Aggregations aggregations;
	private Map<String, Object> winsByTeamRelatedStats = Collections.emptyMap();
	private Map<String, Object> goalsByTeamRelatedStats = Collections.emptyMap();

	public PerOpponenentStatisticsExtractor(String mainAggName, String goalsPerOpponentAggName, String winsPerOpponentAggName,
	        Aggregations aggregations) {
		this.mainAggName = mainAggName;
		this.goalsPerOpponentAggName = goalsPerOpponentAggName;
		this.winsPerOpponentAggName = winsPerOpponentAggName;
		this.aggregations = aggregations;
	}

	/**
	 * Gathers the buckets and extracts data to retrieve wins related stats (wins, row,
	 * pts). Filter the fields according to the request's body
	 */
	public PerOpponenentStatisticsExtractor generateWinsPerOpponent(QueryParameters queryParameters) {
		InternalFilter byTeamAggratedInternalFilter = (InternalFilter) aggregations.asMap().get(mainAggName);
		Map<String, Aggregation> aggregationsMap = byTeamAggratedInternalFilter.getAggregations().asMap();
		winsByTeamRelatedStats = mapWins((StringTerms) aggregationsMap.get(winsPerOpponentAggName), queryParameters);
		return this;
	}

	/**
	 * Gathers the buckets and extracts data to retrieve goals related stats. Filter the
	 * fields according to the request's body
	 */
	public PerOpponenentStatisticsExtractor generateGoalsPerOpponent(QueryParameters queryParameters) {
		InternalFilter byTeamAggratedInternalFilter = (InternalFilter) aggregations.get(mainAggName);
		Map<String, Aggregation> aggregationsMap = byTeamAggratedInternalFilter.getAggregations().asMap();
		goalsByTeamRelatedStats = mapGoals((StringTerms) aggregationsMap.get(goalsPerOpponentAggName), queryParameters);
		return this;
	}

	public Map<String, Object> buildStats() {
		Map<String, Object> statsPerOpponent = mergeToNewMap(winsByTeamRelatedStats, goalsByTeamRelatedStats);
		return statsPerOpponent;
	}

	private Map<String, Object> mergeToNewMap(Map<String, Object> winsRelatedStats,
	        Map<String, Object> goalsRelatedStats) {
		Map<String, Object> o = new LinkedHashMap<>(winsRelatedStats);

		for (Entry<String, Object> entry : goalsRelatedStats.entrySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> pointsByTeam = (Map<String, Object>) winsRelatedStats.get(entry.getKey());

			Map<String, Object> stats = pointsByTeam == null ? new LinkedHashMap<>()
			        : new LinkedHashMap<>(pointsByTeam);
			o.put(entry.getKey(), stats);
			stats.put(StatsField.GOALS_FOR.getFieldName(), entry.getValue());
		}

		return o;
	}

	/**
	 * Filter the fields according to the request's body
	 */
	private Map<String, Object> mapGoals(StringTerms terms, QueryParameters queryParameters) {
		final Map<String, Object> goalsMap = new LinkedHashMap<>();
		if (queryParameters.getFields().contains(StatsField.GOALS_FOR.getFieldName())) {
			// Key is the opponent team name
			terms.getBuckets().stream().forEach(x -> goalsMap.put(x.getKeyAsString(), getInternalSumValue(x)));
		}

		return goalsMap;
	}

	private Map<String, Object> mapWins(StringTerms s, QueryParameters queryParameters) {
		Map<String, Object> main = new LinkedHashMap<>();

		for (Bucket x : s.getBuckets()) {
			Map<String, Object> o = new LinkedHashMap<>();
			String opponentTeamName = x.getKeyAsString();

			int totalWins = (int) x.getDocCount();
			int totalRow = getInternalSumValue(x);
			int points = totalWins * 2;

			if (queryParameters.getFields().contains(StatsField.POINTS.getFieldName())) {
				o.put(StatsField.POINTS.getFieldName(), points);
			}

			if (queryParameters.getFields().contains(StatsField.ROW.getFieldName())) {
				o.put(StatsField.ROW.getFieldName(), totalRow);
			}

			if (queryParameters.getFields().contains(StatsField.WINS.getFieldName())) {
				o.put(StatsField.WINS.getFieldName(), totalWins);
			}

			if (!o.isEmpty()) {
				main.put(opponentTeamName, o); // Stats are grouped by opponent
			}
		}

		return main;
	}

	private int getInternalSumValue(HasAggregations b) {
		return (int) ((InternalSum) b.getAggregations().iterator().next()).getValue();
	}
}
