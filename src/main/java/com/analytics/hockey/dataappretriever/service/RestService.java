package com.analytics.hockey.dataappretriever.service;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.*;
import static spark.Spark.halt;
import static spark.Spark.path;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.inject.Inject;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
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

	// Route loginRoute = (request, response) -> {
	// UUID sessionId = UUID.randomUUID();
	// request.session().attribute("sessionId", sessionId.toString());
	//
	// Map<String, Object> attributes = new HashMap<>();
	// attributes.put("sessionId", sessionId.toString());
	// attributes.put("clientId",
	// "717459441893-u56hh7e62dfdbf9t17aof59necvd7256.apps.googleusercontent.com");
	//
	// return new ModelAndView(attributes, "index.ftl");
	// };

	private void print(Object o) {
		System.out.println(o.toString());
	}

	Route callbackRoute = (request, response) -> {
		String sessionId = request.queryMap().get("callback").value();

		String key = request.body();
		print(request.body());
		if (sessionId.equals(request.session().attribute("sessionId"))) {
//			GoogleOAuth.run();
			return "AUTHORIZED";
		}
		String secret = "OxSHkwxmyx67p5CI1n9MzZah";

		

		halt(401, "Please connect");
		return "PLEASE DON'T TRY HACKING ME";
	};

	/**
	 * @inheritDoc
	 */
	@Override
	public void start() {
		Spark.ipAddress(propertyLoader.getProperty(PropertyConstant.SPARK_HOST.toString()).intern());
		Spark.port(propertyLoader.getPropertyAsInteger(PropertyConstant.SPARK_PORT.toString()));

		final Configuration configuration = new Configuration(new Version(2, 3, 23));
		configuration.setClassForTemplateLoading(RestService.class, "/");

		// TODO use websockets ?

		initFaviconIcon();

		before("/*", (req, res) -> {
			logger.info("Requested route : " + req.pathInfo());

		});
		
		post("/Callback", callbackRoute);

		post("/oauth", callbackRoute);

		get("/sup", (req, res) -> {
			System.out.println("SUP !!");
			return Optional.ofNullable(req.session().attribute("sessionId")).orElse("NULL");
		});

		get("/login", (request, response) -> {
			UUID sessionUUID = UUID.randomUUID();
			String sessionId = sessionUUID.toString().intern();
			request.session().attribute("sessionId", sessionId);

			Map<String, Object> attributes = new HashMap<>();
			attributes.put("sessionId", sessionId);
			attributes.put("clientId", "717459441893-u56hh7e62dfdbf9t17aof59necvd7256.apps.googleusercontent.com");

			try {
				StringWriter writer = new StringWriter();
				Template resultTemplate = configuration.getTemplate("templates/index.ftl");

				resultTemplate.process(attributes, writer);
				return writer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "hi";
		});

		after((req, res) -> {

			res.header("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
			res.header("Last-Modified", "{now} GMT");
			res.header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
			res.header("Pragma", "no-cache");
			res.header("", "");

			// res.status(200);
		});

		path("/api/nhl/v1", () -> {
			/*
			 * /api/v1/nhl/
			 */
			before((req, res) -> {
				boolean authenticated = true;
				if (!authenticated) {
					halt(401, "Please connect"); // TODO redirect to login page
				}
			});

			after((req, res) -> {
				res.type("application/json");
				// res.status(200);
			});

			path("/teams", () -> {
				get("", nhlTeamsBySeasonRoute);
				get("/:season", nhlTeamsBySeasonRoute);
			});

			path("/stats", () -> {
				get("", nhlStatsBySeasonRoute);
				get("/:season", nhlStatsBySeasonRoute);
				get("/:season/:team", nhlStatsBySeasonRoute);
			});

			path("/scores", () -> {
				get("/:year", nhlScoresRoute);
				get("/:year/:month", nhlScoresRoute);
				get("/:year/:month/:day", nhlScoresRoute);
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

	Route nhlScoresRoute = (request, response) -> {
		try {
			Integer year = request.params(":month") != null ? Integer.parseInt(request.params(":year")) : null;
			Integer month = request.params(":month") != null ? Integer.parseInt(request.params(":month")) : null;
			Integer day = request.params(":day") != null ? Integer.parseInt(request.params(":day")) : null;

			String scores = dataRetriever.getScores(year, month, day, Collections.emptyMap());
			// String teamNames = getTeamNames(request.params(":season"));
			response.status(200);
			response.type("application/json");

			return outputPrettyIfNecessary(request, scores);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			response.status(HttpURLConnection.HTTP_BAD_REQUEST);
			return MALFORMED_REQUEST_OUPUT;
		}
	};

	// TODO deplacer
	Route nhlStatsBySeasonRoute = (request, response) -> {
		try {
			String stats = dataRetriever.getStats(request.params(":season"), request.params(":team"),
			        Collections.emptyMap());
			// String teamNames = getTeamNames(request.params(":season"));
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

	private String outputPrettyIfNecessary(Request request, String output) throws JsonException {
		return request.queryParams("pretty") == null ? output : jsonFormatter.toPrettyJson(output, 2);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getTeamNames(String season) throws Exception {
		return dataRetriever.getTeams(season, Collections.emptyMap());
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
