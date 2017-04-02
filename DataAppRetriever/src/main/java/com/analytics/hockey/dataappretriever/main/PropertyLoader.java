package com.analytics.hockey.dataappretriever.main;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultPropertyLoader.class)
public interface PropertyLoader {
	String getProperty(String key);
}