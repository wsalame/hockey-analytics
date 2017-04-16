package com.analytics.hockey.dataappretriever.service;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;

import spark.ResponseTransformer;

public class JsonResponseTransformer {

	private static final JsonFormatter jsonFormatter = GuiceInjector.get(JsonFormatter.class);

	public static String toJson(Object o) throws JsonException {
		return jsonFormatter.toJson(o);
	}

	public static ResponseTransformer toJson() {
		return JsonResponseTransformer::toJson;
	}
}