package com.analytics.hockey.dataappretriever.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.TeamElasticsearchField;

public class Team implements IsElasticsearchIndexable {

	private String currentName;
	private List<String> pastNames;

	public Team() {
	}

	public Team(String currentName, List<String> pastNames) {
		this.currentName = currentName;
		this.pastNames = pastNames;
	}

	public String getCurrentName() {
		return currentName;
	}

	public void setCurrentName(String currentName) {
		this.currentName = currentName;
	}

	public List<String> getPastNames() {
		return pastNames;
	}

	public void setPastNames(List<String> pastNames) {
		this.pastNames = pastNames;
	}

	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = new HashMap<>(TeamElasticsearchField.values().length);

		for (TeamElasticsearchField field : TeamElasticsearchField.values()) {
			document.put(field.getJsonFieldName(), field.getIndexingValue(this));
		}

		return document;
	}

	public List<String> getAllNames() {
		List<String> names = new ArrayList<>(1 + pastNames.size());
		names.add(currentName);
		names.addAll(pastNames);
		return names;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String buildIndex() {
		return "teams";
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String buildType() {
		return "teams";
	}
}