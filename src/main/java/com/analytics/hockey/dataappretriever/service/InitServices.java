package com.analytics.hockey.dataappretriever.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchWriteController;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchRetrieveException;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapperUtils;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.DataRetriever;
import com.analytics.hockey.dataappretriever.model.ExposedApiService;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.HockeyScrapper;
import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.model.MessageConsumer;
import com.analytics.hockey.dataappretriever.model.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class InitServices {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private final DataRetriever dataRetriever;
	private final ElasticsearchWriteController elasticsearchWriteCtrl;
	private final MessageConsumer messageConsumer;
	private final HockeyScrapper hockeyScrapper;
	private final ExposedApiService exposedApiService;

	@Inject
	public InitServices(DataRetriever dataRetriever, ElasticsearchWriteController elasticsearchWriteCtrl,
	        MessageConsumer messageConsumer, HockeyScrapper hockeyScrapper, ExposedApiService exposedApiService)
	        throws Exception {
		this.dataRetriever = dataRetriever;
		this.elasticsearchWriteCtrl = elasticsearchWriteCtrl;
		this.messageConsumer = messageConsumer;
		this.hockeyScrapper = hockeyScrapper;
		this.exposedApiService = exposedApiService;
	}

	public void startServices() throws IOException, ElasticsearchRetrieveException, ExecutionException {
		List<IsConnected> services = Lists.newArrayList(this.dataRetriever, this.elasticsearchWriteCtrl,
		        this.messageConsumer, this.hockeyScrapper);

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
				rb();
				this.exposedApiService.start();
				System.out.println("Starting API....");
				this.exposedApiService.awaitInitialization();
			} else {
				throwCouldNotStartServices(failedServices);
			}
		} catch (InterruptedException | TimeoutException e) {
			logger.fatal(e, e);
			throwCouldNotStartServices(failedServices);
		}
	}

	private void throwCouldNotStartServices(List<IsConnected> failedServices) {
		throw new IllegalStateException(
		        "COULD NOT START SOME OF THE SERVICES : " + Arrays.toString(failedServices.toArray()));
	}

	private void rb() throws IOException {
		String TASK_QUEUE_NAME = GuiceInjector.get(PropertyLoader.class).getProperty("rmq.queueName");
		messageConsumer.consume(TASK_QUEUE_NAME, new OnMessageConsumption<Void>() {
			@Override
			public Void execute(byte[] body, Object... args) throws Exception {
				String message = new String(body, "UTF-8");
				HockeyScrapperUtils.unmarshall(message).parallelStream().forEach(game -> safeInsertGame(game));
				return null;
			}
		});

//		Integer month = 10;
//		Integer year = 2005;
//
//		final String index = year.toString();
		// elasticsearchWriteCtrl.createIndex(index, false);
		//
		// for (int day = 1; day <= 5; day++) {
		// String url = "http://localhost:8989/nhl/v1/" + day + "/" + month + "/" + year;
		// hockeyScrapper.sendHttpRequest(new AsyncHttpCallWrapper.Builder(url,
		// HttpVerb.GET).build());
		// }
	}

	private void safeInsertGame(Game game) {
		try {
			elasticsearchWriteCtrl.insertGame(game);
			System.out.println(Arrays.toString(game.buildDocument().entrySet().toArray()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}