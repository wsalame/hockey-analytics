package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchReadController;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.google.inject.ImplementedBy;

@ImplementedBy(ElasticsearchReadController.class)
public interface DataRetriever extends IsConnected{
	String getTeamNames(int season) throws ElasticsearchRetrieveException;

	String getTotalGoals(int season, String team, Long startEpochMillis, Long endEpochMillis) throws ElasticsearchRetrieveException;
	
	String getTotalGoals(int season, String team) throws ElasticsearchRetrieveException;
	
	String getScores(int season, int day, int month, int year) throws ElasticsearchRetrieveException;
}
