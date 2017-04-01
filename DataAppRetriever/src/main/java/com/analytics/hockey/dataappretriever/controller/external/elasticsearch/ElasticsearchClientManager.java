package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;

import com.analytics.hockey.dataappretriever.model.IsConnected;

public class ElasticsearchClientManager implements IsConnected{

	private static final Logger logger = LogManager.getLogger(ElasticsearchClientManager.class);

	private final String HOST_ADRESS = "127.0.0.1";
	private final int PORT = 9300;
	private static volatile ElasticsearchClientManager INSTANCE;
	private volatile Client client;

	private ElasticsearchClientManager() {
		try {
			connect();
		} catch (Exception e) {
			logger.fatal("Error in initClient", e);
		} finally {
			addClientShutDownHook();
		}
	}
	
	@Override
	public void connect() throws IOException {
		client = new TransportClientFactory(HOST_ADRESS, PORT).build();
	}

	@Override
	public void addClientShutDownHook() {
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
