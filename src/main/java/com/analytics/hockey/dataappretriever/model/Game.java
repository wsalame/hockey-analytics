package com.analytics.hockey.dataappretriever.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.GameElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.analytics.hockey.dataappretriever.exception.GameUnmarshallException;
import com.google.common.annotations.VisibleForTesting;

public class Game implements IsElasticsearchIndexable {
	private String winnerTeam;
	private String loserTeam;
	private String homeTeam;
	private short scoreWinner;
	private short scoreLoser;
	private boolean isRegulatimeTimeWin;
	private LocalDate date;

	@VisibleForTesting
	public Game(){
		
	}
	
	public Game(LocalDate date) {
		this.date = date;
	}

	public Game(Map<String, Object> gameValues, LocalDate date) throws GameUnmarshallException {
		this.date = date;

		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			try {
				field.setValue(this, gameValues.get(field.getJsonFieldName()));
			} catch (Exception e) {
				throw new GameUnmarshallException("Error : " + field.getJsonFieldName());
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

	public void setWinnerTeam(String winnerTeam) {
		this.winnerTeam = winnerTeam;
	}

	public void setLoserTeam(String loserTeam) {
		this.loserTeam = loserTeam;
	}

	public void setHomeTeam(String homeTeam) {
		this.homeTeam = homeTeam;
	}

	public void setScoreWinner(short scoreWinner) {
		this.scoreWinner = scoreWinner;
	}

	public void setScoreLoser(short scoreLoser) {
		this.scoreLoser = scoreLoser;
	}

	public void setRegulatimeTimeWin(boolean isRegulatimeTimeWin) {
		this.isRegulatimeTimeWin = isRegulatimeTimeWin;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = new HashMap<>(GameElasticsearchField.values().length);

		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			document.put(field.getJsonFieldName(), field.getIndexingValue(this));
		}

		return document;
	}

	/**
	 * @inheritDoc If a game is played November 29 2006, then we know it's part of the
	 *             2006-2007 season. Therefore "2006-2007" will be returned
	 */
	@Override
	public String buildIndex() {
		LocalDate date = getDate();
		int month = date.getMonthValue();
		int year = date.getYear();

		return buildGamesIndex(month, year);
	}

	/**
	 * @see {@link #buildIndex()}
	 */
	public String buildGamesIndex(int month, int year) {
		String index = month >= 10 ? year + "-" + (year + 1) : (year - 1) + "-" + (year);

		return index;
	}

	/**
	 * @inheritDoc Type will be the month (numeric value) of when the game was played. If
	 *             a game is played November 29 2006, then the 'type' will be "11".
	 */
	@Override
	public String buildType() {
		return String.valueOf(getDate().getMonthValue());
	}
}