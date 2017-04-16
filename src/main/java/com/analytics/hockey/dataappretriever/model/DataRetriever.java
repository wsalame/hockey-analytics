package com.analytics.hockey.dataappretriever.model;

import java.util.Map;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchReadController;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.google.inject.ImplementedBy;

@ImplementedBy(ElasticsearchReadController.class)
public interface DataRetriever extends IsConnected {
	/**
	 * Return all team names. If it's for a defined season, return the exact names for
	 * given season. The returned name will be the "current" name for the given season.
	 * For example, in 2007-2007, it was the "Coyotes Phoenix", and not "Coyotes Arizona"
	 * 
	 * Otherwise return all time names, where an object will have both the current name
	 * and the past names.
	 * 
	 * @param season
	 *            Season we are interested in to retrieve the team names. Can be null for
	 *            all time names
	 * @param params
	 *            Request's body parameters in a map format
	 * @return Results in JSON format
	 * @throws DataStoreException
	 */
	String getTeams(String season, Map<String, Object> params) throws DataStoreException;

	String getScores(String index, String type, Integer year, Integer month, Integer day, Map<String, Object> params) throws DataStoreException;

	String getScores(Integer year, Integer month, Integer day, Map<String, Object> params) throws DataStoreException;

	String getStats(String season, String team, Map<String, Object> params) throws DataStoreException;

	String getStandings(String season, String team, Map<String, Object> params) throws DataStoreException;
}