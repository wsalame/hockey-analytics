package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;

public enum FieldDatatype {
    // Strings
	RAW_STRING("string") {
		@Override
		public void buildMappingField(XContentBuilder builder) throws IOException {
			builder.field("analyzer", "english");
			builder.startObject("fields").startObject("raw").field("type", "string").field("index", "not_analyzed")
			        .endObject().endObject(); // TODO still useful ?
		}
	},
	STRING("string") {
		@Override
		public void buildMappingField(XContentBuilder builder) throws IOException {
			builder.field("index", "not_analyzed");
		}
	},
    // Boolean
	BOOLEAN("boolean"),
    // Date
	DATE("date"),
    // Numerics
	SHORT("short"),
	LONG("long"),
	DOUBLE("double"),
	INTEGER("integer");

	private String fieldDataType;

	FieldDatatype(String fieldDataType) {
		this.fieldDataType = fieldDataType;
	}

	public void buildMappingField(XContentBuilder builder) throws IOException {
		builder.field("type", this.toString());
	}

	public String getFieldDataType() {
		return this.fieldDataType;
	}

	public boolean requiresAnalyzed() {
		return this == FieldDatatype.STRING;
	}

	public boolean isRawFieldOnly() {
		return this == FieldDatatype.RAW_STRING;
	}

	public boolean isString() {
		return this == FieldDatatype.STRING || this == FieldDatatype.RAW_STRING;
	}

	public boolean isNumeric() {
		return this == FieldDatatype.INTEGER || this == FieldDatatype.SHORT || this == FieldDatatype.DOUBLE
		        || this == FieldDatatype.LONG;
	}

	public boolean isBoolean() {
		return this == FieldDatatype.BOOLEAN;
	}

	public boolean isDate() {
		return this == FieldDatatype.DATE;
	}

	@Override
	public String toString() {
		return this.fieldDataType;
	}
}