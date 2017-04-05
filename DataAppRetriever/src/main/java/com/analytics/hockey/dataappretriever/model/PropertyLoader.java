package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.util.DefaultPropertyLoader;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultPropertyLoader.class)
public interface PropertyLoader {
	String getProperty(String key);

	Integer getPropertyAsInteger(String key);
}