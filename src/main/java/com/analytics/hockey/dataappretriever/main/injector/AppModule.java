package com.analytics.hockey.dataappretriever.main.injector;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(AsyncHttpClient.class).to(DefaultAsyncHttpClient.class);

		// The other bindings in the app are defined by the @ImplementedBy annotation
	}
}
