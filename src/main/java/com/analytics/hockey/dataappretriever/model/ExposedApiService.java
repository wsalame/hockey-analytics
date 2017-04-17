package com.analytics.hockey.dataappretriever.model;

import java.util.Map;

import com.analytics.hockey.dataappretriever.service.RestService;
import com.google.inject.ImplementedBy;

@ImplementedBy(RestService.class)
public interface ExposedApiService extends IsConnected {
	String getTeams(String season, Map<String, Object> params) throws Exception;

	String getScores(String index, String type, Integer year, Integer month, Integer day, Map<String, Object> params)
	        throws Exception;

	String getScores(Integer year, Integer month, Integer day, Map<String, Object> params) throws Exception;

	String getStats(String season, String team, Map<String, Object> params) throws Exception;

	String getStandings(String season, String team, Map<String, Object> params) throws Exception;

	// Endpoints
	final String BASE_PREFIX_ENDPOINT = "/api";
	final String BASE_NHL_V1_ENDPOINT = "/nhl/v1";
	final String TEAMS_ENDPOINT = "/teams";
	final String STATS_ENDPOINT = "/stats";
	final String SCORES_ENDPOINT = "/scores";
}