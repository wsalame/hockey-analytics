package com.analytics.hockey.dataappretriever.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import com.analytics.hockey.dataappretriever.controller.ElasticsearchWriteController;
import com.analytics.hockey.dataappretriever.main.HockeyScrapperUtils;
import com.analytics.hockey.dataappretriever.main.RabbitMqConsumer;
import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.OnMessageConsummation;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class InitServices {

	public InitServices() throws Exception {

	}

	Integer day = 10;
	Integer month = 12;
	Integer year = 2005;

	final String index = year.toString();
	final String type = day.toString() + month.toString() + year.toString();

	public void init() throws IOException, TimeoutException, InterruptedException {

		ElasticsearchWriteController c = ElasticsearchWriteController.getInstance();
		c.createIndex(index, false);

		rb();
	}

	private void rb() throws IOException, TimeoutException, InterruptedException {
		RabbitMqConsumer rb = RabbitMqConsumer.getInstance();

		String TASK_QUEUE_NAME = "hockeyQueue";

		String uri = "http://localhost:8989/nhl/v1/" + day + "/" + month + "/" + year;

		rb.consume(TASK_QUEUE_NAME, new OnMessageConsummation<Void>() {
			@Override
			public Void execute(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			        throws Exception {
				String message = new String(body, "UTF-8");
				ElasticsearchWriteController.getInstance().putMappingIfNotExists(index, type);
				for (Game game : HockeyScrapperUtils.unmarshall(message)) {
					ElasticsearchWriteController.getInstance().insertGame(game, index, type);
					System.out.println(Arrays.toString(game.buildDocument().entrySet().toArray()));
				}
				return null;
			}
		});
		rb.send(1, uri);
	}
}
