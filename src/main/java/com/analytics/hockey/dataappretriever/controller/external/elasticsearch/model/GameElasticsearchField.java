package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.FieldDatatype;
import com.analytics.hockey.dataappretriever.model.Game;

public enum GameElasticsearchField implements IsElasticsearchField<Game> {
    // TODO add
    // unit tests
    // qui verifie
    // tout
    // les json sont differents. Un autre
    // test qui retrieve les json de
    // HockeyScrapper pour comparer que on a
    // les memes

	WINNER_TEAM("winner_team", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getWinnerTeam();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setWinnerTeam((String) o);
		}

	},
	LOSER_TEAM("loser_team", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getLoserTeam();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setLoserTeam((String) o);
		}
	},
	HOME_TEAM("home_team", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getHomeTeam();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setHomeTeam((String) o);
		}

	},
	SCORE_WINNER("score_winner", FieldDatatype.SHORT) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getScoreWinner();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setScoreWinner((short) (int) o);
		}

	},
	SCORE_LOSER("score_loser", FieldDatatype.SHORT) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getScoreLoser();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setScoreLoser((short) (int) o);
		}

	},
	IS_REGULATION_TIME_WIN("is_regulation_time_win", FieldDatatype.BOOLEAN) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.isRegulatimeTimeWin();
		}

		@Override
		public void setValue(Game game, Object o) {
			game.setRegulatimeTimeWin((boolean) o);
		}

	},
	DATE("date", FieldDatatype.DATE) { // TODO changer le json date en quelque chose
	                                   // dautre genre day_date
		@Override
		public Object getIndexingValue(Game game) {
			// We could store the date as dd-MM-yyyy, or any other format, but it will
			// require internally extra processing to convert (and be able to do
			// comparing). To avoid this, we will directly convert the value to millis,
			// although we actually only care about the day
			return TimeUnit.DAYS.toMillis(game.getDate().toEpochDay());
		}

		@Override
		public void setValue(Game game, Object o) throws Exception {
			// Date is in a different format coming from the scrapper. It is already set
			// in the constructor of object {@link Game}
		}
	};

	private final String jsonFieldName;
	private final FieldDatatype fieldDatatype;
	private static Map<String, GameElasticsearchField> valuesByJsonMap = new HashMap<>();
	static {
		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			valuesByJsonMap.put(field.getJsonFieldName(), field);
		}
	}

	private GameElasticsearchField(String jsonFieldName, FieldDatatype fieldDatatype) {
		this.jsonFieldName = jsonFieldName;
		this.fieldDatatype = fieldDatatype;
	}

	public abstract void setValue(Game game, Object o) throws Exception;

	@Override
	public String getJsonFieldName() {
		return jsonFieldName;
	}

	@Override
	public FieldDatatype getFieldDatatype() {
		return fieldDatatype;
	}

	public static GameElasticsearchField valueOfCustom(String json) {
		return valuesByJsonMap.get(json);
	}
}