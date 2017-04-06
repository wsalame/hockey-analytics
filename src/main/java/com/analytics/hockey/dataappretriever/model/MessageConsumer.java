package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;

import com.analytics.hockey.dataappretriever.controller.external.messagebroker.RabbitMqConsumerController;
import com.google.inject.ImplementedBy;

@ImplementedBy(RabbitMqConsumerController.class)
public interface MessageConsumer extends IsConnected {
	<T> void consume(String taskQueueName, OnMessageConsumption<T> action) throws IOException;
}
