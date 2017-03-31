package com.analytics.hockey.dataappretriever.elasticsearch;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.analytics.hockey.dataappretriever.elasticsearch.model.IsElasticsearchField;

public class ElasticsearchUtils {
	private static final Logger logger = LogManager.getLogger(ElasticsearchUtils.class);

	public enum FieldDatatype {
	    // Strings
		RAW_STRING("string"),
		STRING("string"),
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

	public static String buildMappingParametersAsJson(IsElasticsearchField<?>[] values) throws IOException {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			builder.startObject("_all").field("enabled", false).endObject();
			builder.startObject("properties");

			for (IsElasticsearchField<?> param : values) {
				builder.startObject(param.getJson());

				switch (param.getFieldDatatype()) {
				case STRING:
					builder.field("analyzer", "english");
					builder.startObject("fields").startObject("raw").field("type", "string")
					        .field("index", "not_analyzed").endObject().endObject(); // TODO still useful ?
					break;
				case RAW_STRING:
					builder.field("index", "not_analyzed");
					break;
				case DATE:
					builder.startObject("date").field("type", "date").field("format", "dd-MM-yyyy").endObject();
				default:
				}

				builder.field("type", param.getFieldDatatype().toString());
				builder.endObject();
			}

			builder.endObject().endObject();
			return builder.string();
		} catch (IOException e) {
			logger.error("Error build JSON mapping", e);
			throw e;
		}
	}
}
