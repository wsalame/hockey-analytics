package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;

import com.analytics.hockey.dataappretriever.main.DefaultPropertyLoader;
import com.analytics.hockey.dataappretriever.main.PropertyLoader;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.IsConnected;

public class ElasticsearchClientManager implements IsConnected {

	private static final Logger logger = LogManager.getLogger(ElasticsearchClientManager.class);

	private static volatile ElasticsearchClientManager INSTANCE;
	private volatile Client client;
	private final PropertyLoader propertyLoader = GuiceInjector.get(DefaultPropertyLoader.class);

	private ElasticsearchClientManager() {
		try {
			String host = propertyLoader.getProperty("es.host");
			int port = Integer.valueOf(propertyLoader.getProperty("es.port"));
			connect(host, port);
		} catch (Exception e) {
			logger.fatal(e, e);
		} finally {
			addClientShutDownHook();
		}
	}

	@Override
	public void connect(String host, Integer port) throws IOException {
		client = new TransportClientFactory(host, port).build();
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

	public static ElasticsearchClientManager getInstance() { // TODO utiliser guice plutot ? Faire une interface ?
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
