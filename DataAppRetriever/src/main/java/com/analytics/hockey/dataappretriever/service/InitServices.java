package com.analytics.hockey.dataappretriever.service;

import java.io.IOException;
import java.util.Arrays;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchWriteController;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapper;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapperUtils;
import com.analytics.hockey.dataappretriever.controller.external.messagebroker.MessageConsumer;
import com.analytics.hockey.dataappretriever.controller.external.messagebroker.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.service.http.AsyncHttpCallWrapper;
import com.analytics.hockey.dataappretriever.service.http.HttpVerb;
import com.google.inject.Inject;

public class InitServices {

	private final ElasticsearchWriteController elasticsearchWriteCtrl;
	private final MessageConsumer broker;
	private final HockeyScrapper hockeyScrapper;

	@Inject
	public InitServices(ElasticsearchWriteController elasticsearchWriteCtrl, MessageConsumer broker,
	        HockeyScrapper hockeyScrapper) throws Exception {
		this.elasticsearchWriteCtrl = elasticsearchWriteCtrl;
		this.broker = broker;
		this.hockeyScrapper = hockeyScrapper;
	}

	Integer day = 10;
	Integer month = 12;
	Integer year = 2005;

	final String index = year.toString();
	final String type = day.toString() + month.toString() + year.toString();

	public void init() throws IOException {
		elasticsearchWriteCtrl.createIndex(index, false);

		rb();
	}

	private void rb() throws IOException {
		String TASK_QUEUE_NAME = "games";

		String url = "http://localhost:8989/nhl/v1/" + day + "/" + month + "/" + year;

		broker.consume(TASK_QUEUE_NAME, new OnMessageConsumption<Void>() {
			@Override
			public Void execute(byte[] body, Object... args) throws Exception {
				String message = new String(body, "UTF-8");
				elasticsearchWriteCtrl.putMappingIfNotExists(index, type);

				HockeyScrapperUtils.unmarshall(message).parallelStream()
				        .forEach(game -> safeInsertGame(game, index, type));
				return null;
			}
		});

		hockeyScrapper.sendHttpRequest(new AsyncHttpCallWrapper.Builder(url, HttpVerb.GET).build());
	}

	private void safeInsertGame(Game game, String index, String type) {
		try {
			elasticsearchWriteCtrl.insertGame(game, index, type);
			System.out.println(Arrays.toString(game.buildDocument().entrySet().toArray()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}