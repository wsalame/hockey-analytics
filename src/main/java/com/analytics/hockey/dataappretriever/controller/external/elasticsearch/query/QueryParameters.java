package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.exception.DataStoreException;
import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.model.JsonFormatter;
import com.analytics.hockey.dataappretriever.model.StatsField;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Sample request. Will be transformed to POJO for easier retrieval
 * 
 * 
 * <pre>
{
	"size":5,
	"fields": ["wins", "points", "gf"],
	"range":{
		"start": 1334317138000,
		"end": 1492083539000,
		"format": "epoch_millis"
	},
	"sort" : {
		"wins" : "asc",
		"points" : "desc",
		"gf" : "desc"
	}
}
 * </pre>
 * 
 */
public class QueryParameters {
	private static final Logger logger = LogManager.getLogger(QueryParameters.class);

	private Integer size;
	private Set<String> fields = new LinkedHashSet<>();
	private RangeParameter range;
	private SortParameter sort;
	private JsonFormatter formatter = GuiceInjector.get(JsonFormatter.class);

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Set<String> getFields() {
		return fields;
	}

	public void setFields(Set<String> fields) {
		this.fields = fields;
	}

	public RangeParameter getRange() {
		return range;
	}

	public void setRange(RangeParameter range) {
		this.range = range;
	}

	public SortParameter getSort() {
		return sort;
	}

	public void setSort(SortParameter sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		try {
			return formatter.toJson(this);
		} catch (JsonException e) {
			// logger.error
			// TODo
			return "Error serializing object";
		}
	}

	/**
	 * Intended to transform a request's body into a {@link QueryParameters} object for
	 * easier manipulation. If no fields are defined in the body request, it is implied
	 * that all fields are required to be sent in the response.
	 * 
	 * @param params
	 *            Request's body in a Map format
	 * @return POJO representation of the request's body
	 */
	public static QueryParameters toQueryParameters(Map<String, Object> params) throws DataStoreException {
		QueryParameters queryParameters = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			queryParameters = mapper.convertValue(params, QueryParameters.class);

			if (queryParameters.getFields().isEmpty()) {
				queryParameters.getFields().addAll(StatsField.ALL_FIELDS_NAME);
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
			throw new DataStoreException(
			        "Body malformed. Verify if defined params are in accordance with the documentation");
		}

		return queryParameters;
	}
}