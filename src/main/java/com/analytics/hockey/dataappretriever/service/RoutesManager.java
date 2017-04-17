package com.analytics.hockey.dataappretriever.service;

import com.google.inject.ImplementedBy;

import spark.Route;

@ImplementedBy(RoutesManagerImpl.class)
public interface RoutesManager {
	Route get(String key);
}
