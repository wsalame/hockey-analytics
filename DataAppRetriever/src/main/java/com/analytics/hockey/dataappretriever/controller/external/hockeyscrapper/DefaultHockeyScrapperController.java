package com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import com.analytics.hockey.dataappretriever.model.HockeyScrapper;
import com.analytics.hockey.dataappretriever.service.http.AsyncHttpCallWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultHockeyScrapperController implements HockeyScrapper {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private AsyncHttpClient client; // TODO should be binded to more generic client

	@Inject
	public DefaultHockeyScrapperController(AsyncHttpClient client) {
		this.client = client;
	}

	@Override
	public synchronized void start() {
		// This might look like it does not make sense to put in here, but it's consistent
		// with the rest of the IsConnected implementations, where we had to explicitly
		// had to start a connection in IsConnect#start()
		addClientShutDownHook();
	}

	@Override
	public void awaitInitialization() {
		// client.
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (!client.isClosed()) {
					client.close();
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
		}));
	}

	@Override
	public void sendHttpRequest(AsyncHttpCallWrapper call) {
		try {
			call.getVerb().prepare(client, call.getUrl()).execute(new AsyncCompletionHandler<Void>() {
				@Override
				public Void onCompleted(Response response) throws Exception {
					return null;
				}

				@Override
				public void onThrowable(Throwable t) {
					logger.error(t, t);
				}
			});
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
}