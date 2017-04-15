package com.analytics.hockey.dataappretriever.service;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.path;

import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.inject.Inject;

import spark.Request;
import spark.Route;
import spark.Spark;

public class RestService implements ExposedApiService {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private DataRetriever dataRetriever;
	private JsonFormatter jsonFormatter;
	private PropertyLoader propertyLoader;

	private final String MALFORMED_REQUEST_OUPUT = "Error. Have you malformed your request ?";

	@Inject
	public RestService(DataRetriever dataRetriever, JsonFormatter jsonFormatter, PropertyLoader propertyLoader) {
		this.dataRetriever = dataRetriever;
		this.jsonFormatter = jsonFormatter;
		this.propertyLoader = propertyLoader;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void start() {
		Spark.ipAddress(propertyLoader.getProperty(PropertyConstant.SPARK_HOST.toString()).intern());
		Spark.port(propertyLoader.getPropertyAsInteger(PropertyConstant.SPARK_PORT.toString()));

		// TODO use websockets ?

		initFaviconIcon();

		initBefore();

		path("/api/v1/nhl", () -> {
			initBeforeNhl();

			get("/:season", (request, response) -> {
				try {
					response.status(200);
					response.type("application/json");

					String api = "/api/v1/nhl/2005/teams";

					return api;
				} catch (Exception e) {
					logger.error(e.toString(), e);
					response.status(HttpURLConnection.HTTP_BAD_REQUEST);
					return MALFORMED_REQUEST_OUPUT;
				}
			});

			initSeasonPath();
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
		System.out.println("Spark is ready on localhost:" + Spark.port());
	}

	private void initFaviconIcon() {
		get("/favicon.ico", (request, response) -> {
			return "";
		});
	}

	Route nhlTeamsBySeasonRoute = (request, response) -> {
		try {
			String teamNames = getTeamNames(request.params(":season"));
			response.status(200);
			response.type("application/json");

			return outputPrettyIfNecessary(request, teamNames);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			response.status(HttpURLConnection.HTTP_BAD_REQUEST);
			return MALFORMED_REQUEST_OUPUT;
		}
	};

	private void initSeasonPath() {
		/*
		 * /api/v1/nhl/:season
		 */
		path("/:season", () -> {
			/*
			 * /api/v1/nhl/:season/teams
			 */
			get("/teams", nhlTeamsBySeasonRoute);
		});
	}

	private void initBefore() {
		before("/*", (req, res) -> {
			logger.info("Requested route : " + req.pathInfo());
		});
	}

	private void initBeforeNhl() {
		/*
		 * /api/v1/nhl/
		 */
		before("/*", (req, res) -> {
			boolean authenticated = true;
			if (!authenticated) {
				halt(401, "Please connect"); // TODO redirect to login page
			}
		});
	}

	private String outputPrettyIfNecessary(Request request, String output) throws JsonException {
		return request.queryParams("pretty") == null ? output : jsonFormatter.toPrettyJson(output, 2);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getTeamNames(String season) throws Exception {
		return dataRetriever.getTeams(season, null);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getTotalGoals(String season, String team, Map<String, Object> params) throws Exception {
		return null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getTotalPoints(String season, String team, Map<String, Object> params) throws Exception {
		return null;
	}
}
