package com.analytics.hockey.dataappretriever.service;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.google.inject.Inject;
import com.google.inject.Provider;

import spark.Request;
import spark.Route;

public class RoutesManagerImpl implements RoutesManager {
	private final Logger logger = LogManager.getLogger(this.getClass());
	private final Map<String, Route> routes = new HashMap<>();
	private JsonFormatter jsonFormatter;

	private final String MALFORMED_REQUEST_OUPUT = "Error. Have you malformed your request ?";
	private final Provider<ExposedApiService> exposedApiServiceProvider;

	@Inject
	public RoutesManagerImpl(JsonFormatter jsonFormatter, Provider<ExposedApiService> exposedApiServiceProvider) {
		this.jsonFormatter = jsonFormatter;
		this.exposedApiServiceProvider = exposedApiServiceProvider;

		createRoutes();
	}

	private void createRoutes() {
		Route nhlScoresRoute = (request, response) -> {
			try {
				Integer year = request.params(":month") != null ? Integer.parseInt(request.params(":year")) : null;
				Integer month = request.params(":month") != null ? Integer.parseInt(request.params(":month")) : null;
				Integer day = request.params(":day") != null ? Integer.parseInt(request.params(":day")) : null;

				String scores = exposedApiServiceProvider.get().getScores(year, month, day, bodyToMap(request));
				response.status(200);
				response.type("application/json"); //TODO
				return outputPrettyIfNecessary(request, scores);
			} catch (Exception e) {
				logger.error(e.toString(), e);
				response.status(HttpURLConnection.HTTP_BAD_REQUEST);
				return MALFORMED_REQUEST_OUPUT;
			}
		};

		Route nhlStatsBySeasonRoute = (request, response) -> {
			try {
				String stats = exposedApiServiceProvider.get().getStats(request.params(":season"),
				        request.params(":team"), bodyToMap(request));
				response.status(200);
				response.type("application/json");

				return outputPrettyIfNecessary(request, stats);
			} catch (Exception e) {
				logger.error(e.toString(), e);
				response.status(HttpURLConnection.HTTP_BAD_REQUEST);
				return MALFORMED_REQUEST_OUPUT;
			}
		};

		Route nhlTeamsBySeasonRoute = (request, response) -> {
			try {
				String teams = exposedApiServiceProvider.get().getTeams(request.params(":season"),
				        bodyToMap(request));
				response.status(200);
				response.type("application/json");

				return outputPrettyIfNecessary(request, teams);
			} catch (Exception e) {
				logger.error(e.toString(), e);
				response.status(HttpURLConnection.HTTP_BAD_REQUEST);
				return MALFORMED_REQUEST_OUPUT;
			}
		};

		routes.put(ExposedApiService.TEAMS_ENDPOINT, nhlTeamsBySeasonRoute);
		routes.put(ExposedApiService.STATS_ENDPOINT, nhlStatsBySeasonRoute);
		routes.put(ExposedApiService.SCORES_ENDPOINT, nhlScoresRoute);
	}

	private Map<String, Object> bodyToMap(Request request) throws JsonException {
		if(request.body().isEmpty()){
			return Collections.emptyMap();
		}
		return jsonFormatter.toMap(request.body());
	}

	private String outputPrettyIfNecessary(Request request, String output) throws JsonException {
		return request.queryParams("pretty") == null ? output : jsonFormatter.toPrettyJson(output, 2);
	}

	@Override
	public Route get(String key) {
		return routes.get(key);
	}
}
