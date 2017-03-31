package com.analytics.hockey.dataappretriever.elasticsearch;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class TransportClientFactory {

	private Map<String, String> settings;
	private String clientTransportHost;
	private Integer clientTransportPort;

	public TransportClientFactory(String clientTransportHost, Integer clientTransportPort) {
		this(new HashMap<String, String>(), clientTransportHost, clientTransportPort);
	}

	public TransportClientFactory(Map<String, String> settings, String clientTransportHost,
	        Integer clientTransportPort) {
		this.settings = settings;
		this.clientTransportHost = clientTransportHost;
		this.clientTransportPort = clientTransportPort;
	}

	public TransportClient build() throws ElasticsearchUnavailableException {
		TransportClient client = null;

		try {
			InetAddress inetAddress = InetAddress.getByName(clientTransportHost);
			Settings s = Settings.builder().put(this.settings).build();
			client = TransportClient.builder().settings(s).build()
			        .addTransportAddress(new InetSocketTransportAddress(inetAddress, clientTransportPort));
		} catch (Exception e) {
			throw new ElasticsearchUnavailableException("Could not connect to Elasticsearch");
		}

		verifyConnection(client);

		return client;
	}

	private void verifyConnection(TransportClient client) throws ElasticsearchUnavailableException {
		List<DiscoveryNode> nodes = client.connectedNodes();
		if (nodes.isEmpty()) {
			throw new ElasticsearchUnavailableException("No nodes available. Verify Elasticsearch is running!");
		}
	}
}