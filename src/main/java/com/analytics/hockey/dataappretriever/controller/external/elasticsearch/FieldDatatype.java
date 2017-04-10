package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * 
 * Representents the different data types as defined up until the 2.4 version
 * https://www.elastic.co/guide/en/elasticsearch/reference/2.4/mapping-types.html
 * 
 * An extra type called RAW_STRING was added for convience. This is the same as a string,
 * but that is not analyzed
 * 
 * All STRING will have both a field that is analyzed, and a second one <name>.raw that is
 * not analyzed
 * 
 * Every constant knows how to build itself for the mapping during the indexation
 */
public enum FieldDatatype {
    // Strings
	RAW_STRING("string") {
		@Override
		public void buildMappingField(XContentBuilder builder) throws IOException {
			builder.field("type", getFieldDataType());
			builder.field("index", "not_analyzed");
		}
	},
	STRING("string") {
		@Override
		public void buildMappingField(XContentBuilder builder) throws IOException {
			builder.field("type", getFieldDataType());
			builder.field("analyzer", "english");
			builder.startObject("fields").startObject("raw").field("type", "string").field("index", "not_analyzed")
			        .endObject().endObject();
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

	private FieldDatatype(String fieldDataType) {
		this.fieldDataType = fieldDataType;
	}

	public void buildMappingField(XContentBuilder builder) throws IOException {
		builder.field("type", this.toString());
	}

	public String getFieldDataType() {
		return this.fieldDataType;
	}

	@Override
	public String toString() {
		return this.fieldDataType;
	}
}