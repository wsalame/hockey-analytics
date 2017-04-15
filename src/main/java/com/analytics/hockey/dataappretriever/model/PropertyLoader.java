package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.utilImpl.DefaultPropertyLoader;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultPropertyLoader.class)
public interface PropertyLoader {
	/**
	 * Retrieves the property mapped with the key, or null if there is no mapping for the
	 * key
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if this map
	 *         contains no mapping for the key
	 */
	String getProperty(String key);

	/**
	 * @see #getProperty(String)
	 */
	Integer getPropertyAsInteger(String key);
}