package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.main.PropertyLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Singleton
public class RabbitMqConsumerController implements MessageConsumer {

	private final static Logger logger = LogManager.getLogger(RabbitMqConsumerController.class);
	private final int TIMEOUT_CLOSE_CONNECTION;

	private Channel channel;
	private Connection connection;

	@Inject
	public RabbitMqConsumerController(PropertyLoader propertyLoader) throws IOException, TimeoutException {
		TIMEOUT_CLOSE_CONNECTION = Integer.valueOf(propertyLoader.getProperty("rmq.timeOutCloseConnection_ms"));
		String host = propertyLoader.getProperty("rmq.host");
		connect(host, null);
	}

	@Override
	public void connect(String host, Integer port) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);

		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} finally {
			addClientShutDownHook();
		}
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					channel.close();
					connection.close(TIMEOUT_CLOSE_CONNECTION);
				} catch (IOException | TimeoutException e) {
					logger.error(e.toString(), e);
				}
			}
		});
	}

	@Override
	public <T> void consume(final String taskQueueName, final OnMessageConsumption<T> action) throws IOException {
		channel.queueDeclare(taskQueueName, false, false, false, null);

		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
			        byte[] body) throws IOException {
				try {
					// Here by passing our own interface, we can ensure it is always wrapped in a try/finally clause
					action.execute(body);
				} catch (Exception e) {
					logger.error(e);
				} finally {
					channel.basicAck(envelope.getDeliveryTag(), false); // TODO add test qui make sure que le ack
					                                                    // est apelle en cas d'erreur
				}
			}
		};
		channel.basicConsume(taskQueueName, false, consumer);
	}
}
