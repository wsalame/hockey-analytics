package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.main.AppConstants;
import com.analytics.hockey.dataappretriever.model.MessageConsumer;
import com.analytics.hockey.dataappretriever.model.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.common.annotations.VisibleForTesting;
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
	private PropertyLoader propertyLoader;

	@Inject
	public RabbitMqConsumerController(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
		TIMEOUT_CLOSE_CONNECTION = propertyLoader.getPropertyAsInteger("rmq.timeOutCloseConnectionMillis");
	}

	public RabbitMqConsumerController() {
		TIMEOUT_CLOSE_CONNECTION = -1;
		this.propertyLoader = null;
	}

	@Override
	public synchronized void start() throws IOException, TimeoutException {
		if (isNotStarted(connection, channel)) {
			//////////////////////////
			// Init connection factory host and port
			//////////////////////////
			String host = propertyLoader.getProperty(AppConstants.RMQ_HOST);
			Integer port = propertyLoader.getPropertyAsInteger(AppConstants.RMQ_PORT);
			ConnectionFactory factory = createConnectionFactory();
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
				addClientShutDownHook();
			} catch(Exception e){
				this.connection = null;
				this.channel = null;
				throw e;
			}
		}
	}
	
	//TODO
//	@VisibleForTesting
//	Channel createChannel(Connection connection) throws IOException{
//		return connection.createChannel();
//	}

	final int MAX_RETRIES = 3; // TODO

	@Override
	public void awaitInitialization() {
		for (int i = 0; !this.channel.isOpen() && i <= MAX_RETRIES; i++) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
				throw new IllegalStateException("Could not await");
			}
		}
	}

	@Override
	public void addClientShutDownHook() {
		getRuntime().addShutdownHook(new Thread(() -> {
			try {
				closeConnections();
			} catch (IOException | TimeoutException e) {
				logger.error(e.toString(), e);
			}
		}));
	}

	@Override
	public <T> void consume(final String taskQueueName, final OnMessageConsumption<T> action) throws IOException {
		channel.queueDeclare(taskQueueName, false, false, false, null);

		Consumer consumer = createConsumer(channel, action);

		String tag = channel.basicConsume(taskQueueName, consumer);
		logger.info("Consumer tag received : {}", tag);
	}
	
	private boolean isNotStarted(Connection connection, Channel channel) {
		return connection == null || channel == null;
	}

	@VisibleForTesting
	<T> Consumer createConsumer(Channel channel, final OnMessageConsumption<T> action) {
		return new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
			        byte[] body) throws IOException {
				RabbitMqConsumerController.this.handleDelivery(action, body, envelope);
			}
		};
	}

	@VisibleForTesting
	<T> void handleDelivery(final OnMessageConsumption<T> action, byte[] body, Envelope envelope) {
		try {
			// Here by passing our own interface, we can ensure it is always
			// wrapped in a try/finally clause
			action.execute(body);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				channel.basicAck(envelope.getDeliveryTag(), false);
			} catch (IOException e) {
				logger.error("Could not send Ack", e);
			}
		}
	}
	
	@VisibleForTesting
	ConnectionFactory createConnectionFactory() {
		return new ConnectionFactory();
	}

	@VisibleForTesting
	Runtime getRuntime() {
		return Runtime.getRuntime();
	}

	@VisibleForTesting
	void closeConnections() throws IOException, TimeoutException {
		channel.close();
		connection.close(TIMEOUT_CLOSE_CONNECTION);
	}

	@VisibleForTesting
	void setChannel(Channel channel) {
		this.channel = channel;
	}

	@VisibleForTesting
	void setConnection(Connection connection) {
		this.connection = connection;
	}

	@VisibleForTesting
	void setPropertyLoader(PropertyLoader propertyLoader) {
		this.propertyLoader = propertyLoader;
	}
}