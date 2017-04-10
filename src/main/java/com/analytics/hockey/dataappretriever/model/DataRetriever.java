package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchReadController;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.google.inject.ImplementedBy;

@ImplementedBy(ElasticsearchReadController.class)
public interface DataRetriever extends IsConnected{
	String getTeamNames(String season) throws ElasticsearchRetrieveException;

	String getTotalGoals(String season, String team, Long startEpochMillis, Long endEpochMillis) throws ElasticsearchRetrieveException;
	
	String getTotalGoals(String season, String team) throws ElasticsearchRetrieveException;
	
	String getScores(int day, int month, int year) throws ElasticsearchRetrieveException;
}