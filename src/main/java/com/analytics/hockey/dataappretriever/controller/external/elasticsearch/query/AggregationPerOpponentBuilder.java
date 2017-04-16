package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query;

import java.util.Map;
import java.util.Optional;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.model.StatsField;

/**
 * Augmented {@link TermsBuilder}. For a given stat (win, goal for, etc.) grouped by
 * opponent, it is always the same code, except for the fields to retrieve. This wrapper
 * is intended to avoid duplicating code. It can also take into account the desired size,
 * and sort the field.
 * 
 */
public class AggregationPerOpponentBuilder extends TermsBuilder {

	public AggregationPerOpponentBuilder(String aggName, GameElasticsearchField groupBy, StatsField aggFieldName,
	        GameElasticsearchField sumField) {
		super(aggName);
		field(groupBy.getJsonFieldName())
		        .subAggregation(AggregationBuilders.sum(getName()).field(sumField.getJsonFieldName()));
	}

	/**
	 * Adds a sort to the builder. Checks if the field that makes the aggregation is part
	 * of the body request {{@link #QueryParameters}. If it is, a sort is added either
	 * ascending (asc) or descending (desc), according once again to the user's input in
	 * the body
	 * 
	 * @see {{@link #QueryParameters}
	 * @param queryParameters
	 *            Object containing the range
	 */
	public AggregationPerOpponentBuilder sortIfRequired(QueryParameters queryParameters) {
		Map<String, String> sorts = queryParameters.getSort().getSorts();

		String fieldName = getName();

		if (sorts.containsKey(fieldName)) {
			Order order = Order.aggregation(fieldName, isAsc(sorts.get(fieldName)));
			order(order);
		}

		return this;
	}

	/**
	 * Sets the size of returned element for the a given aggregation according to the
	 * query parameters. If the size is not defined, the default value is set.
	 * 
	 * @param termsBuilder
	 *            Aggregation which its size is to be set.
	 * @param queryParameters
	 *            Object containing the size
	 */
	public AggregationPerOpponentBuilder sizeIfRequired(QueryParameters queryParameters, int defaultSize) {
		int size = Optional.ofNullable(queryParameters.getSize()).orElse(defaultSize);
		size(size);

		return this;
	}

	/**
	 * Returns true if the input is "asc" (ignores case), meaning ascending. Does not test
	 * if it is instead "desc". In other words, anything that does not match "asc" will
	 * implied to be descending, which is the default behaviour we want.
	 * 
	 * @param input
	 *            The input to test
	 * @return true if the input is "asc", otherwise false.
	 */
	private boolean isAsc(String input) {
		// We can use == because left String was intern'ed, and right String is a
		// literal. This is an overkill here, but it's just to showcase
		// a feature of the JVM
		return input.toLowerCase().intern() == "asc";
	}
}