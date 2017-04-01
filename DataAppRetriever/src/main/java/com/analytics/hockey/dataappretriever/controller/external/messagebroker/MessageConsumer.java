package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

import java.io.IOException;

import com.analytics.hockey.dataappretriever.model.IsConnected;

public interface MessageConsumer extends IsConnected {
	<T> void consume(String taskQueueName, OnMessageConsumption<T> action) throws IOException;
}
