package com.analytics.hockey.dataappretriever.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum StatsField {
	GOALS_FOR("gf"),
	WINS("wins"),
	ROW("row"),
	POINTS("pts");
	private final String fieldName;

	public static final Set<String> ALL_FIELDS_NAME = Collections.unmodifiableSet(
	        Arrays.asList(StatsField.values()).stream().map(StatsField::toString).collect(Collectors.toSet()));

	StatsField(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String toString() {
		return this.fieldName;
	}
}