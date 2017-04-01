package com.analytics.hockey.dataappretriever.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchUtils.FieldDatatype;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchField;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchIndexable;
import com.analytics.hockey.dataappretriever.exception.GameUnmarshallException;

public class Game implements IsElasticsearchIndexable {

	private String winnerTeam;
	private String loserTeam;
	private String homeTeam;
	private short scoreWinner;
	private short scoreLoser;
	private boolean isRegulatimeTimeWin;
	private Date date;

	public Game(Map<String, Object> gameValues, Date date) throws GameUnmarshallException {
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

	public Date getDate() {
		return date;
	}

	public enum GameElasticsearchField implements IsElasticsearchField<Game> { // TODO add unit tests qui verifie tout
	                                                                           // les json sont differents. Un autre
	                                                                           // test qui retrieve les json de
	                                                                           // HockeyScrapper pour comparer que on a
	                                                                           // les memes

		WINNER_TEAM("winner_team", FieldDatatype.RAW_STRING) {
			@Override
			public Object getValue(Game game) {
				return game.getWinnerTeam();
			}

			@Override
			void setValue(Game game, Object o) {
				game.winnerTeam = (String) o;
			}

		},
		LOSER_TEAM("loser_team", FieldDatatype.RAW_STRING) {
			@Override
			public Object getValue(Game game) {
				return game.getLoserTeam();
			}

			@Override
			void setValue(Game game, Object o) {
				game.loserTeam = (String) o;
			}
		},
		HOME_TEAM("home_team", FieldDatatype.RAW_STRING) {
			@Override
			public Object getValue(Game game) {
				return game.getHomeTeam();
			}

			@Override
			void setValue(Game game, Object o) {
				game.homeTeam = (String) o;
			}

		},
		SCORE_WINNER("score_winner", FieldDatatype.SHORT) {
			@Override
			public Object getValue(Game game) {
				return game.getScoreWinner();
			}

			@Override
			void setValue(Game game, Object o) {
				game.scoreWinner = (short) (int) o;
			}

		},
		SCORE_LOSER("score_loser", FieldDatatype.SHORT) {
			@Override
			public Object getValue(Game game) {
				return game.getScoreLoser();
			}

			@Override
			void setValue(Game game, Object o) {
				game.scoreLoser = (short) (int) o;
			}

		},
		IS_REGULATION_TIME_WIN("is_regulation_time_win", FieldDatatype.BOOLEAN) {
			@Override
			public Object getValue(Game game) {
				return game.isRegulatimeTimeWin();
			}

			@Override
			void setValue(Game game, Object o) {
				game.isRegulatimeTimeWin = (boolean) o;
			}

		};

		private static Map<String, GameElasticsearchField> valuesByJsonMap = new HashMap<>();
		static {
			for (GameElasticsearchField field : GameElasticsearchField.values()) {
				valuesByJsonMap.put(field.getJson(), field);
			}
		}
		private final String json;
		private final FieldDatatype fieldDatatype;

		abstract void setValue(Game game, Object o) throws Exception;

		@Override
		public String getJson() {
			return json;
		}

		private GameElasticsearchField(String json, FieldDatatype fieldDatatype) {
			this.json = json;
			this.fieldDatatype = fieldDatatype;
		}

		@Override
		public FieldDatatype getFieldDatatype() {
			return fieldDatatype;
		}

		public static GameElasticsearchField valueOfCustom(String json) {
			return valuesByJsonMap.get(json);
		}
	}

	@Override
	public Map<String, Object> buildDocument() {
		Map<String, Object> document = new HashMap<>(GameElasticsearchField.values().length);

		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			document.put(field.getJson(), field.getValue(this));
		}

		return document;
	}
}
