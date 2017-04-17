package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query.QueryParameters;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query.RangeParameter;

public class CustomBoolQueryBuilder extends BoolQueryBuilder {

	public CustomBoolQueryBuilder() {

	}

	public static CustomBoolQueryBuilder boolQueryBuilder() {
		return new CustomBoolQueryBuilder();
	}

	public CustomBoolQueryBuilder rangeIfRequired(QueryParameters queryParameters) {
		return rangeIfRequired(queryParameters.getRange());
	}

	public CustomBoolQueryBuilder rangeIfRequired(RangeParameter range) {
		if (range != null) {
			this.filter(rangeQuery(range));
		}

		return this;
	}
	
	public static RangeQueryBuilder rangeQuery(RangeParameter range){
		if (range != null) {
			RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(GameElasticsearchField.DATE.getJsonFieldName());

			// It doesn't matter if the returned value from Range#getStart() is NULL.
			// ES handles it as unbounded
			rangeQuery.gte(range.getStart());

			// Same as above
			rangeQuery.lte(range.getEnd());

			// Here ES doesn't like if we explicit a date format with a NULL value
			// (Will
			// throw a NPE)
			if (range.getFormat() != null) {
				rangeQuery.format(range.getFormat());
			}

			return rangeQuery;
		}
		
		return null;
	}

	public CustomBoolQueryBuilder filterTeam(String team) {
		filter(QueryBuilders.boolQuery()
		        .should(QueryBuilders.termQuery(GameElasticsearchField.WINNER_TEAM.getJsonFieldName(), team))
		        .should(QueryBuilders.termQuery(GameElasticsearchField.LOSER_TEAM.getJsonFieldName(), team)));

		return this;
	}
}
