package com.analytics.hockey.dataappretriever.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.analytics.hockey.dataappretriever.exception.GameUnmarshallException;

public class Game implements IsElasticsearchIndexable {
	String winnerTeam;
	String loserTeam;
	String homeTeam;
	short scoreWinner;
	short scoreLoser;
	boolean isRegulatimeTimeWin;
	LocalDate date;

	public Game(Map<String, Object> gameValues, LocalDate date) throws GameUnmarshallException {
		this.date = date;

		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			try {
				field.setValue(this, gameValues.get(field.getJson()));
			} catch (Exception e) {
				throw new GameUnmarshallException("Error : " + field.getJson());
			}
		}
	}

	public String getWinnerTeam() {
		return winnerTeam;
	}

	public String getLoserTeam() {
		return loserTeam;
	}

	public String getHomeTeam() {
		return homeTeam;
	}

	public short getScoreWinner() {
		return scoreWinner;
	}

	public int getScoreLoser() {
		return scoreLoser;
	}

	public boolean isRegulatimeTimeWin() {
		return isRegulatimeTimeWin;
	}

	public LocalDate getDate() {
		return date;
	}

	
	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = new HashMap<>(GameElasticsearchField.values().length);

		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			document.put(field.getJson(), field.getIndexingValue(this));
		}

		return document;
	}
}
