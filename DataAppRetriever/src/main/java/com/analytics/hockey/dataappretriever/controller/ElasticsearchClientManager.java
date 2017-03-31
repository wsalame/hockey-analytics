package com.analytics.hockey.dataappretriever.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;

import com.analytics.hockey.dataappretriever.elasticsearch.ElasticsearchUnavailableException;
import com.analytics.hockey.dataappretriever.elasticsearch.TransportClientFactory;

public class ElasticsearchClientManager {

	private static final Logger logger = LogManager.getLogger(ElasticsearchClientManager.class);

	private final String HOST_ADRESS = "127.0.0.1";
	private final int PORT = 9300;
	private static volatile ElasticsearchClientManager INSTANCE;
	private volatile Client client;

	private ElasticsearchClientManager() {
		try {
			client = new TransportClientFactory(HOST_ADRESS, PORT).build();
		} catch (ElasticsearchUnavailableException e) {
			logger.fatal("Error in initClient", e);
		} finally {
			addClientShutDownHook();
		}
	}

	private void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				client.close();
			}
		});
	}

	public static ElasticsearchClientManager getInstance() {
		if (INSTANCE == null) {
			synchronized (ElasticsearchClientManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new ElasticsearchClientManager();
				}
			}
		}

		return INSTANCE;
	}

	public Client getClient() {
		return client;
	}
}
