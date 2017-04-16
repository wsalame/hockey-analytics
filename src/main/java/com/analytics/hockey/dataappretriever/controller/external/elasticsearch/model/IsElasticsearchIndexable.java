package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import java.util.Map;

/**
 * Your model should implements this interface if it is intended to be indexed in
 * Elasticsearch. By implementing this interface, your model will know how to build itself
 * to make itself indexable and searchable.
 */
public interface IsElasticsearchIndexable {
	/**
	 * Builds document that will be inserted into an Elasticsearch Index in a map format.
	 * They key represents the field name, and the value can be pretty much any Object
	 * that can be serialized to a JSON format representations. This includes all
	 * primitives types and collections.
	 * 
	 * @return Document to be indexed
	 */
	Map<String, Object> buildDocument();

	/**
	 * Builds the index name where document will be stored or retrieved
	 * 
	 * @see {@link #buildGamesIndex(Game)}
	 * @return Elasticsearch index name where the teams will be stored
	 */
	String buildIndex();

	/**
	 * Builds the type name where document will be stored or retrieved
	 * 
	 * @see {@link #buildIndex()}
	 * @return Elasticsearch type name where the document will be stored
	 */
	String buildType();

	/**
	 * Overrides default index's builder TODO Implement generic object that knows how to
	 * build the index
	 * 
	 * @param index
	 *            Index name to use
	 */
	// void setIndex(String index);

	/**
	 * Overrides default index's builder TODO Implement generic object that knows how to
	 * build the type
	 * 
	 * @param type
	 *            Type name to use
	 */
	// void setType(String type);
}
