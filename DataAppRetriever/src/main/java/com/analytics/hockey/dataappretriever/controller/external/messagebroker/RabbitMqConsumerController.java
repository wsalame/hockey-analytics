package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.model.MessageConsumer;
import com.analytics.hockey.dataappretriever.model.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
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

	private final Logger logger = LogManager.getLogger(this.getClass());
	private final int TIMEOUT_CLOSE_CONNECTION;

	private Channel channel;
	private Connection connection;
	private final PropertyLoader propertyLoader;

	@Inject
	public RabbitMqConsumerController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
		TIMEOUT_CLOSE_CONNECTION = propertyLoader.getPropertyAsInteger("rmq.timeOutCloseConnectionMillis");
	}

	@Override
	public synchronized void start() throws IOException, TimeoutException {
		if (channel == null) {
			//////////////////////////
			// Init connection factory host and port
			//////////////////////////
			String host = propertyLoader.getProperty("rmq.host").intern();
			Integer port = propertyLoader.getPropertyAsInteger("rmq.port");
			ConnectionFactory factory = new ConnectionFactory();
			if (host != null) {
				factory.setHost(host);
			}

			if (port != null) {
				factory.setPort(port);
			}

			//////////////////////////
			// Create connection
			//////////////////////////
			try {
				this.connection = factory.newConnection();
				this.channel = connection.createChannel();
			} finally {
				addClientShutDownHook();
			}
		}
	}

	@Override
	public void awaitInitialization() {
		int MAX_RETRIES = 3;
		for (int i = 0; i < MAX_RETRIES && !this.channel.isOpen(); i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
				throw new IllegalStateException("Could not await");
			}
		}

		if (!this.channel.isOpen()) {
			throw new IllegalStateException("Could not await");
		}
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				channel.close();
				connection.close(TIMEOUT_CLOSE_CONNECTION);
			} catch (IOException | TimeoutException e) {
				logger.error(e.toString(), e);
			}
		}));
	}

	@Override
	public <T> void consume(final String taskQueueName, final OnMessageConsumption<T> action) throws IOException {
		channel.queueDeclare(taskQueueName, false, false, false, null);

		
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
			        byte[] body) throws IOException {
				try {
					// Here by passing our own interface, we can ensure it is always
					// wrapped in a try/finally clause
					action.execute(body);
				} catch (Exception e) {
					logger.error(e);
				} finally {
					channel.basicAck(envelope.getDeliveryTag(), false); // TODO add test
					                                                    // qui make sure
					                                                    // que le ack
					                                                    // est apelle en
					                                                    // cas d'erreur
				}
			}
		};
		String tag = channel.basicConsume(taskQueueName, false, consumer);
		logger.info("Consumer tag received : {}", tag);
	}
}
