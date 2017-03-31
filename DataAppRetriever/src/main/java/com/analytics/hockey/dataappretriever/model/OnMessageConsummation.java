package com.analytics.hockey.dataappretriever.model;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

public interface OnMessageConsummation<T> {
	T execute(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
	        byte[] body) throws Exception;
}
