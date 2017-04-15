package com.analytics.hockey.dataappretriever.model;

import java.util.Map;

import com.analytics.hockey.dataappretriever.service.RestService;
import com.google.inject.ImplementedBy;

@ImplementedBy(RestService.class)
public interface ExposedApiService extends IsConnected {
	String getTeamNames(String season) throws Exception;

	String getTotalGoals(String season, String team, Map<String, Object> params) throws Exception;

	String getTotalPoints(String season, String team, Map<String, Object> params) throws Exception;
}