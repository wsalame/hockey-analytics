package com.analytics.hockey.dataappretriever.service.http;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;

public enum HttpVerb {
	CONNECT {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareConnect(url);
		}
	},
	DELETE {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareDelete(url);
		}
	},
	GET {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareGet(url);
		}
	},
	HEAD {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareHead(url);
		}
	},
	OPTIONS {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareOptions(url);
		}
	},
	PATCH {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.preparePatch(url);
		}
	},
	POST {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.preparePost(url);
		}
	},
	PUT {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.preparePut(url);
		}
	},
	TRACE {
		@Override
		public BoundRequestBuilder prepare(AsyncHttpClient client, String url) {
			return client.prepareTrace(url);
		}
	};

	public abstract BoundRequestBuilder prepare(AsyncHttpClient client, String url);
}