package com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import com.analytics.hockey.dataappretriever.model.HockeyScrapper;
import com.analytics.hockey.dataappretriever.service.http.AsyncHttpCallWrapper;
import com.google.inject.Singleton;

@Singleton
public class DefaultHockeyScrapperController implements HockeyScrapper {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private AsyncHttpClient asyncHttpClient;

	public DefaultHockeyScrapperController() {

	}

	@Override
	public synchronized void start() throws IOException, TimeoutException {
		if (asyncHttpClient == null) {
			asyncHttpClient = new DefaultAsyncHttpClient();
			addClientShutDownHook(); //TODO mauvais endroit ?
		}
	}

	@Override
	public void awaitInitialization() {
	}

	@Override
	public void addClientShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (!asyncHttpClient.isClosed()) {
					asyncHttpClient.close();
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
		}));
	}

	@Override
	public void sendHttpRequest(AsyncHttpCallWrapper call) {
		try {
			call.getVerb().prepare(asyncHttpClient, call.getUrl()).execute(new AsyncCompletionHandler<Void>() {
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