package com.analytics.hockey.dataappretriever.model;

import java.util.Map;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.util.DefaultJsonFormatter;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultJsonFormatter.class)
public interface JsonFormatter {
	String toJson(Object o) throws JsonException;

	String toPrettyJson(Map<String, Object> document, int indent) throws JsonException;

	String toPrettyJson(String json, int indent) throws JsonException;
}