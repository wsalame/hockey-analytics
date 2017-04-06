package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.analytics.hockey.dataappretriever.util.DefaultPropertyLoader;

public abstract class AbstractElasticsearchController implements IsConnected {

	private final Logger logger = LogManager.getLogger(this.getClass());

	private Client client;
	private final PropertyLoader propertyLoader = GuiceInjector.get(DefaultPropertyLoader.class);

	@Override
	public void awaitInitialization() {
		client.admin().cluster().prepareClusterStats().execute().actionGet(); // TODO
		                                                                      // preparehealth
		                                                                      // to green
	}

	@Override
	public synchronized void start() { // TODO a voir si on garde ici
		if (client == null) {
			try {
				String host = propertyLoader.getProperty("es.host").intern();
				int port = Integer.valueOf(propertyLoader.getProperty("es.port"));
				client = new TransportClientFactory(host, port).build();
			} catch (Exception e) {
				logger.fatal(e, e);
			} finally {
				addClientShutDownHook();
			}
		}
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			client.close();
		}));
	}

	protected TransportClient  getClient() {
		return (TransportClient) client;
	}
}