package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.ElasticsearchUnavailableException;

public class TransportClientFactory {

	private Map<String, String> settings;
	private String host;
	private Integer port;

	public TransportClientFactory(String host, Integer port) {
		this(new HashMap<String, String>(), host, port);
	}

	public TransportClientFactory(Map<String, String> settings, String host, Integer port) {
		this.settings = settings;
		this.host = host;
		this.port = port;
	}

	/**
	 * Builds a client that knows how to communicate wit the cluster, using the settings,
	 * host, and port defined in the constructor
	 * 
	 * @return A client that knows how to communicate with cluster
	 * @throws ElasticsearchUnavailableException
	 *             If connection to Elasticsearch cluster is not possible
	 */
	public TransportClient build() throws ElasticsearchUnavailableException {
		TransportClient client = null;

		try {
			InetAddress inetAddress = InetAddress.getByName(host);
			Settings s = Settings.builder().put(this.settings).build();
			client = TransportClient.builder().settings(s).build()
			        .addTransportAddress(new InetSocketTransportAddress(inetAddress, port));
		} catch (Exception e) {
			throw new ElasticsearchUnavailableException("Could not connect to Elasticsearch");
		}

		verifyConnection(client);

		return client;
	}

	/**
	 * Cluster could be connectable, but in an invalid state. This verifies that there are
	 * at least 1 node connectable.
	 * 
	 * @param client
	 *            Client that will be used to connect to the cluster
	 * @throws ElasticsearchUnavailableException
	 */
	private void verifyConnection(TransportClient client) throws ElasticsearchUnavailableException {
		List<DiscoveryNode> nodes = client.connectedNodes();
		if (nodes.isEmpty()) {
			throw new ElasticsearchUnavailableException("No nodes available. Verify Elasticsearch is running!");
		}
	}
}