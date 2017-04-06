package com.analytics.hockey.dataappretriever.model;

import java.util.Map;

import com.analytics.hockey.dataappretriever.service.RestService;
import com.google.inject.ImplementedBy;

@ImplementedBy(RestService.class)
public interface ExposedApiService extends IsConnected {

	String getTeamNames(int season) throws Exception;

	String getTotalGoals(int season, String team, Map<String, Object> params) throws Exception;

	String getTotalPoints(int season, String team, Map<String, Object> params) throws Exception;
}
