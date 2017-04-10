package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.FieldDatatype;
import com.analytics.hockey.dataappretriever.model.Team;

public enum TeamElasticsearchField implements IsElasticsearchField<Team> {

	CURRENT_NAME("current_name", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Team team) {
			return team.getCurrentName();
		}
	},
	PAST_NAMES("past_names", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Team team) {
			return team.getPastNames();
		}
	};

	private final String jsonFieldName;
	private final FieldDatatype fieldDatatype;

	private TeamElasticsearchField(String jsonFieldName, FieldDatatype fieldDatatype) {
		this.jsonFieldName = jsonFieldName;
		this.fieldDatatype = fieldDatatype;
	}

	@Override
	public FieldDatatype getFieldDatatype() {
		return fieldDatatype;
	}

	@Override
	public String getJsonFieldName() {
		return jsonFieldName;
	}

	@Override
	public String toString() {
		return this.jsonFieldName;
	}
}