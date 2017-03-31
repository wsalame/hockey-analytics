package com.analytics.hockey.dataappretriever.main;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import com.analytics.hockey.dataappretriever.exception.MessageBrokerConnectionException;
import com.analytics.hockey.dataappretriever.model.OnMessageConsummation;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMqConsumer {

	private final static Logger logger = LogManager.getLogger(RabbitMqConsumer.class);

	private AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
	private final Channel channel;
	private volatile static RabbitMqConsumer instance;

	public static RabbitMqConsumer getInstance() {
		if (instance == null) {
			synchronized (RabbitMqConsumer.class) {
				if (instance == null) {
					try {
						instance = new RabbitMqConsumer();
					} catch (MessageBrokerConnectionException e) {
						logger.fatal("Could not connect to RabbitMQ", e);
					}
				}
			}
		}

		return instance;
	}

	private RabbitMqConsumer() throws MessageBrokerConnectionException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (IOException | TimeoutException e) {
			throw new MessageBrokerConnectionException("Could not connect to RabbitMQ", e);
		} finally {
			addHttpClientShutDownHook();
		}
	}

	private void addHttpClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (!asyncHttpClient.isClosed()) {
						asyncHttpClient.close();

					}
				} catch (IOException e) {
					logger.error(e.toString(), e);
				}
			}
		});
	}

	public void send(int total, String uri) throws InterruptedException {
		for (int i = 0; i < total; i++) {
			try {
				asyncHttpClient.prepareGet(uri).execute(new AsyncCompletionHandler<Void>() {
					@Override
					public Void onCompleted(Response response) throws Exception {
						return null;
					}

					@Override
					public void onThrowable(Throwable t) {
						logger.error(t);
					}
				});

			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public <T> void consume(final String taskQueueName, final OnMessageConsummation<T> action)
	        throws IOException, TimeoutException {
		channel.queueDeclare(taskQueueName, false, false, false, null);

		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
			        byte[] body) throws IOException {
				try {
					// Here by passing our own interface, we can ensure it is always wrapped in a try/finally clause
					action.execute(consumerTag, envelope, properties, body);
				} catch (Exception e) {
					logger.error(e);
				} finally {
					channel.basicAck(envelope.getDeliveryTag(), false); // TODO add test qui make sure que le ack est
					                                                    // apelle en cas d'erreur
				}
			}
		};
		channel.basicConsume(taskQueueName, false, consumer);
	}
}
