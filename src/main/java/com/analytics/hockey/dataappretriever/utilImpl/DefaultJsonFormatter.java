package com.analytics.hockey.dataappretriever.utilImpl;

import java.io.IOException;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Singleton;

@Singleton
public class DefaultJsonFormatter implements JsonFormatter {
	private final ObjectMapper defaultMapper = new ObjectMapper();
	private final ObjectMapper prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * @inheritDoc
	 */
	@Override
	public String toJson(Object o) throws JsonException {
		try {
			return defaultMapper.writeValueAsString(o);
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
		String validJson = !isInEnveloppe(json) ? wrapInEnveloppe(json) : json;
		try {
			return prettyMapper.writeValueAsString(prettyMapper.readValue(validJson, JsonNode.class));
		} catch (IOException e) {
			throw new JsonException(e.toString(), e);
		}
	}

	private String wrapInEnveloppe(String json) {
		return "[" + json + "]";
	}

	private boolean isInEnveloppe(String json) {
		return json.startsWith("[") && json.endsWith("]");
	}
}