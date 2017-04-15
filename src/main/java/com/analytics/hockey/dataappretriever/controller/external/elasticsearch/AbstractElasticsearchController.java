package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.client.Client;

import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.analytics.hockey.dataappretriever.utilImpl.DefaultPropertyLoader;
import com.google.common.annotations.VisibleForTesting;

public abstract class AbstractElasticsearchController implements IsConnected {

	private final Logger logger = LogManager.getLogger(this.getClass());

	private Client client;
	private final PropertyLoader propertyLoader = GuiceInjector.get(DefaultPropertyLoader.class);

	/**
	 * @inheritDoc
	 */
	@Override
	public void awaitInitialization() { // TODO tests
		ClusterHealthRequestBuilder prepareHealth = client.admin().cluster().prepareHealth("*");

		if (getNumberOfReplicas() == 0) {
			// Yellow == Primary shards are ready, but some or all of the replicas haven't
			// been allocated. If we don't have any replica, then it will always be at
			// best yellow
			prepareHealth.setWaitForYellowStatus().execute().actionGet();
		} else {
			// Green == Primary shards, and replicas are ready
			prepareHealth.setWaitForGreenStatus().execute().actionGet();
		}
	}

	@VisibleForTesting
	int getNumberOfReplicas() {
		return propertyLoader.getPropertyAsInteger(PropertyConstant.ES_NUMBER_REPLICAS.toString());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public synchronized void start() {
		if (client == null) {
			try {
				String host = propertyLoader.getProperty(PropertyConstant.ES_HOST.toString()).intern();
				int port = Integer.valueOf(propertyLoader.getProperty(PropertyConstant.ES_PORT.toString()));
				client = new TransportClientFactory(host, port).build();
			} catch (Exception e) {
				logger.fatal(e, e);
			} finally {
				addClientShutDownHook();
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			client.close();
		}));
	}

	protected Client getClient() {
		return client;
	}
}