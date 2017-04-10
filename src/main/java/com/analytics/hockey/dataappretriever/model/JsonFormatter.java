package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.exception.JsonException;
import com.analytics.hockey.dataappretriever.util.DefaultJsonFormatter;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultJsonFormatter.class)
public interface JsonFormatter {
	/**
	 * Serializes any object into a String object in a JSON format
	 * 
	 * @param o
	 *            Java object to be serialized
	 * @return A JSON representation of the object
	 * @see {{@link #toPrettyJson(Object, int)}
	 * @throws JsonException
	 *             If any error occured during the serialization
	 */
	String toJson(Object o) throws JsonException;

	/**
	 * Same as {{@link #toJson(Object)}, but with defined indentation
	 */
	String toPrettyJson(Object o, int indent) throws JsonException;

	/**
	 * Adds indentation to String already in JSON format, also know as pretty JSON
	 * 
	 * @param json
	 *            The ugly JSON
	 * @param indent
	 *            Value of indentation (spaces)
	 * @return A pretty JSON representation of the object
	 * @see {{@link #toJson(Object)}
	 * @throws JsonException
	 *             If any error occured during the serialization
	 */
	String toPrettyJson(String json, int indent) throws JsonException;
}