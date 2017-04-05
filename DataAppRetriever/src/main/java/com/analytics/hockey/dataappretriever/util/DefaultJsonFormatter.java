package com.analytics.hockey.dataappretriever.util;

import java.io.IOException;
import java.util.Map;

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

	@Override
	public String toJson(Object o) throws JsonException {

		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new JsonException(e.toString(), e);
		}
	}

	@Override
	public String toPrettyJson(Map<String, Object> document, int indent) throws JsonException {
		throw new UnsupportedOperationException("Missing implementation");
	}

	@Override
	public String toPrettyJson(String json, int indent) throws JsonException {
		try {
			return writer.writeValueAsString(mapper.readValue(json, Object.class));
		} catch (IOException e) {
			throw new JsonException(e.toString(), e);
		}
	}
}