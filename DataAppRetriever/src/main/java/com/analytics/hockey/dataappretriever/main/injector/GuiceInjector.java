package com.analytics.hockey.dataappretriever.main.injector;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceInjector {
	private static volatile GuiceInjector instance;
	private Injector injector;

	private GuiceInjector() {
	}

	public static GuiceInjector getInstance() {
		if (instance == null) {
			synchronized (GuiceInjector.class) {
				if (instance == null) {
					instance = new GuiceInjector();
				}
			}
		}

		return instance;
	}

	public static <T> T get(Class<T> clazz) {
		return getInstance().injector.getInstance(clazz);
	}

	public void install(AbstractModule module) {
		injector = Guice.createInjector(module);
	}
}
