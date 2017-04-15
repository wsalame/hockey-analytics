package com.analytics.hockey.dataappretriever.utilImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.google.inject.Singleton;

@Singleton
public class DefaultPropertyLoader implements PropertyLoader {
	private final Logger logger = LogManager.getLogger(this.getClass());

	private Map<String, String> keyValue;

	public DefaultPropertyLoader() {
		init();
	}

	private synchronized void init() {
		if (keyValue == null) {
			keyValue = new HashMap<>();
			try {
				InputStream inputStream = this.getClass().getResourceAsStream("/config.properties");

				Properties prop = new Properties();
				prop.load(inputStream);

				inputStream.close();

				prop.keySet().stream().map(key -> (String) key)
				        .forEach(key -> keyValue.put(key, prop.getProperty(key)));
			} catch (IOException ex) {
				logger.fatal(ex, ex);
			}
		}
	}

	@Override
	public String getProperty(String key) {
		return keyValue.get(key);
	}

	@Override
	public Integer getPropertyAsInteger(String key) {
		String value = getProperty(key);
		return value != null ? Integer.valueOf(value) : null;
	}
}