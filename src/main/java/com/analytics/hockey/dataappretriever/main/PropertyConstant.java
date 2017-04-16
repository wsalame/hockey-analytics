package com.analytics.hockey.dataappretriever.main;

public enum PropertyConstant {
    // Rabbit MQ
	RMQ_PORT("rmq.port"),
	RMQ_HOST("rmq.host"),
	RMQ_QUEUE_NAME_GAMES("rmq.queueName.games"),
	RMQ_QUEUE_NAME_TEAMS("rmq.queueName.teams"),
	RMQ_TIME_OUT_MILLIS("rmq.timeOutCloseConnectionMillis"),
	RMQ_MAX_RETRIES("rmq.maxRetries"),
    // Hockey scrapper
	HOCKEY_SCRAPPER_HOST("hockeyScrapper.host"),
	HOCKEY_SCRAPPER_PORT("hockeyScrapper.port"),
    // Elasticsearch cluster
	ES_HOST("es.host"),
	ES_TRANSPORT_PORT("es.transport.port"),
	ES_NUMBER_SHARDS("es.numberOfShards"),
	ES_NUMBER_REPLICAS("es.numberOfReplicas"),
	ES_MAX_WINDOW_SIZE("es.windowSize"),
    // Spark
	SPARK_HOST("spark.host"),
	SPARK_PORT("spark.port");

	private final String name;

	private PropertyConstant(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
