package com.analytics.hockey.dataappretriever.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Singleton;

@Singleton
public class DefaultPropertyLoader implements PropertyLoader {

	private Map<String, String> keyValue;

	public DefaultPropertyLoader() {
		init();
	}

	private void init() {
		keyValue = new HashMap<>();
		try {
			InputStream inputStream = this.getClass().getResourceAsStream("/config.properties");

			Properties prop = new Properties();
			prop.load(inputStream);

			inputStream.close();

			prop.keySet().stream().map(key -> (String) key).forEach(key -> keyValue.put(key, prop.getProperty(key)));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String getProperty(String key) {
		return keyValue.get(key);
	}
}