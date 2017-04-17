package com.analytics.hockey.dataappretriever.service;

import static spark.Spark.*;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.path;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Spark;

@Singleton
public class RestService implements ExposedApiService {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private DataRetriever dataRetriever;
	private PropertyLoader propertyLoader;
	private RoutesManager routesManager;

	@Inject
	public RestService(DataRetriever dataRetriever, PropertyLoader propertyLoader, RoutesManager routesManager) {
		this.dataRetriever = dataRetriever;
		this.propertyLoader = propertyLoader;
		this.routesManager = routesManager;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void start() {
		Spark.ipAddress(propertyLoader.getProperty(PropertyConstant.SPARK_HOST.toString()).intern());
		Spark.port(propertyLoader.getPropertyAsInteger(PropertyConstant.SPARK_PORT.toString()));

		initFaviconIcon();
		
		// TODO use Transformers to automate pretty pretting

		path(BASE_PREFIX_ENDPOINT + BASE_NHL_V1_ENDPOINT, () -> {
			before((req, res) -> {
				boolean authenticated = true; // TODO OAuth
				if (!authenticated) {
					halt(401, "Please connect");
				}
			});

			after((req, res) -> {
				res.type("application/json");
			});

			path(TEAMS_ENDPOINT, () -> {
				get("", routesManager.get(TEAMS_ENDPOINT));
				get("/:season", routesManager.get(TEAMS_ENDPOINT));
				
				post("", routesManager.get(TEAMS_ENDPOINT));
				post("/:season", routesManager.get(TEAMS_ENDPOINT));
			});

			path(STATS_ENDPOINT, () -> {
				get("", routesManager.get(STATS_ENDPOINT));
				get("/:season", routesManager.get(STATS_ENDPOINT));
				get("/:season/:team", routesManager.get(STATS_ENDPOINT));
				
				post("", routesManager.get(STATS_ENDPOINT));
				post("/:season", routesManager.get(STATS_ENDPOINT));
				post("/:season/:team", routesManager.get(STATS_ENDPOINT));
			});

			path(SCORES_ENDPOINT, () -> {
				get("/:year", routesManager.get(SCORES_ENDPOINT));
				get("/:year/:month", routesManager.get(SCORES_ENDPOINT));
				get("/:year/:month/:day", routesManager.get(SCORES_ENDPOINT));
				
				post("/:year", routesManager.get(SCORES_ENDPOINT));
				post("/:year/:month", routesManager.get(SCORES_ENDPOINT));
				post("/:year/:month/:day", routesManager.get(SCORES_ENDPOINT));
			});
		});
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Spark.stop();
			}
		});
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void awaitInitialization() {
		Spark.awaitInitialization();
		logger.info("Spark is ready on localhost:" + Spark.port());
		System.out.println("Spark is ready on localhost:" + Spark.port());
	}

	private void initFaviconIcon() {
		get("/favicon.ico", (request, response) -> {
			return "";
		});
	}

	@Override
	public String getTeams(String season, Map<String, Object> params) throws Exception {
		return dataRetriever.getTeams(season, params);
	}

	@Override
	public String getScores(String index, String type, Integer year, Integer month, Integer day,
	        Map<String, Object> params) throws Exception {
		return dataRetriever.getScores(year, month, day, params);
	}

	@Override
	public String getScores(Integer year, Integer month, Integer day, Map<String, Object> params) throws Exception {
		return dataRetriever.getScores(year, month, day, params);
	}

	@Override
	public String getStats(String season, String team, Map<String, Object> params) throws Exception {
		return dataRetriever.getStats(season, team, params);
	}

	@Override
	public String getStandings(String season, String team, Map<String, Object> params) throws Exception {
		return dataRetriever.getStandings(season, team, params);
	}
}