package com.analytics.hockey.dataappretriever.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.TeamElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapperUtils;
import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.DataIndexer;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.HockeyScrapper;
import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.model.MessageConsumer;
import com.analytics.hockey.dataappretriever.model.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.analytics.hockey.dataappretriever.model.Team;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class InitServices {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final DataRetriever dataRetriever;
	private final MessageConsumer messageConsumer;
	private final HockeyScrapper hockeyScrapper;
	private final ExposedApiService exposedApiService;
	private final DataIndexer dataIndexer;

	@Inject
	public InitServices(DataRetriever dataRetriever, MessageConsumer messageConsumer, HockeyScrapper hockeyScrapper,
	        ExposedApiService exposedApiService, DataIndexer dataIndexer) throws Exception {
		this.dataRetriever = dataRetriever;
		this.messageConsumer = messageConsumer;
		this.hockeyScrapper = hockeyScrapper;
		this.exposedApiService = exposedApiService;
		this.dataIndexer = dataIndexer;
	}

	public void startServices() throws IOException, ElasticsearchRetrieveException, ExecutionException {
		List<IsConnected> services = Lists.newArrayList(this.dataRetriever, this.dataIndexer, this.messageConsumer,
		        this.hockeyScrapper);

		List<IsConnected> failedServices = Collections.synchronizedList(new ArrayList<IsConnected>());
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, services.size()));

		services.stream().forEach(service -> executor.submit(() -> {
			try {
				service.start();
				service.awaitInitialization();
			} catch (Exception e) {
				logger.fatal(service.getClass().getSimpleName() + " could not be started", e);
				failedServices.add(service);
			}

			logger.info(service.getClass().getSimpleName() + " was started");
		}));

		executor.shutdown();

		try {
			boolean allFinished = executor.awaitTermination(15, TimeUnit.SECONDS) && failedServices.isEmpty();

			if (allFinished) {
				initConsummation();

				this.exposedApiService.start();
				this.exposedApiService.awaitInitialization();
			} else {
				throwCouldNotStartServices(failedServices);
			}
		} catch (InterruptedException | TimeoutException | DataStoreException e) {
			logger.fatal(e, e);
			throwCouldNotStartServices(failedServices);
		}
	}

	private void initConsummation() throws IOException, DataStoreException {
		consumeTeams();
		consumeGames();
	}

	private void throwCouldNotStartServices(List<IsConnected> failedServices) {
		throw new IllegalStateException(
		        "COULD NOT START SOME OF THE SERVICES : " + Arrays.toString(failedServices.toArray()));
	}

	private void consumeGames() throws DataStoreException, IOException {
		for (Integer i = 2005; i <= 2016; i++) {
			final String index = i.toString() + "-" + (i + 1);
			dataIndexer.createIndex(index, false);
		}

		String GAMES_TASK_QUEUE_NAME = GuiceInjector.get(PropertyLoader.class)
		        .getProperty(PropertyConstant.RMQ_QUEUE_NAME_GAMES.toString());
		messageConsumer.consume(GAMES_TASK_QUEUE_NAME, new OnMessageConsumption<Void>() {
			@Override
			public Void execute(byte[] body, Object... args) throws Exception {
				String message = new String(body, "UTF-8");
				logger.info(message);
				HockeyScrapperUtils.unmarshallGames(message).parallelStream().forEach(game -> safeInsertGame(game));
				return null;
			}
		});
	}

	private void consumeTeams() throws IOException, DataStoreException {
		dataIndexer.createIndex(new Team().buildIndex(), false);

		String TEAMS_TASK_QUEUE_NAME = GuiceInjector.get(PropertyLoader.class)
		        .getProperty(PropertyConstant.RMQ_QUEUE_NAME_TEAMS.toString());
		messageConsumer.consume(TEAMS_TASK_QUEUE_NAME, new OnMessageConsumption<Void>() {
			@Override
			public Void execute(byte[] body, Object... args) throws Exception {
				String message = new String(body, "UTF-8");
				logger.info(message);

				List<Object> responseList = HockeyScrapperUtils.responseToList(message);
				safeIndexTeams(responseList);
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void safeIndexTeams(List<Object> responseList) {
		try {
			for (Object o : responseList) {
				Map<String, Object> responseMap = (Map<String, Object>) o;
				String currentName = (String) responseMap.get(TeamElasticsearchField.CURRENT_NAME.getJsonFieldName());
				List<String> pastNames = (ArrayList<String>) responseMap
				        .get(TeamElasticsearchField.PAST_NAMES.getJsonFieldName());
				Team team = new Team(currentName, pastNames);
				dataIndexer.indexDocument(team);
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	private void safeInsertGame(Game game) {
		try {
			dataIndexer.indexDocument(game);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}
}