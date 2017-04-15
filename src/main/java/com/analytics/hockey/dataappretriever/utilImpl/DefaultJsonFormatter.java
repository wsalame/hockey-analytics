package com.analytics.hockey.dataappretriever.utilImpl;

import java.io.IOException;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Singleton;

@Singleton
public class DefaultJsonFormatter implements JsonFormatter {
	private final ObjectMapper mapper = new ObjectMapper();
	private final ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

	/**
	 * @inheritDoc
	 */
	@Override
	public String toJson(Object o) throws JsonException {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new JsonException(e.toString(), e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toPrettyJson(Object o, int indent) throws JsonException {
		throw new UnsupportedOperationException("Missing implementation");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toPrettyJson(String json, int indent) throws JsonException {
		try {
			return writer.writeValueAsString(mapper.readValue(json, Object.class));
		} catch (IOException e) {
			throw new JsonException(e.toString(), e);
		}
	}
}