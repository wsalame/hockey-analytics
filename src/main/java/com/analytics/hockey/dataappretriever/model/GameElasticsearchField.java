package com.analytics.hockey.dataappretriever.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.FieldDatatype;
import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.model.IsElasticsearchField;

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
		void setValue(Game game, Object o) {
			game.winnerTeam = (String) o;
		}

	},
	LOSER_TEAM("loser_team", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getLoserTeam();
		}

		@Override
		void setValue(Game game, Object o) {
			game.loserTeam = (String) o;
		}
	},
	HOME_TEAM("home_team", FieldDatatype.RAW_STRING) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getHomeTeam();
		}

		@Override
		void setValue(Game game, Object o) {
			game.homeTeam = (String) o;
		}

	},
	SCORE_WINNER("score_winner", FieldDatatype.SHORT) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getScoreWinner();
		}

		@Override
		void setValue(Game game, Object o) {
			game.scoreWinner = (short) (int) o;
		}

	},
	SCORE_LOSER("score_loser", FieldDatatype.SHORT) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.getScoreLoser();
		}

		@Override
		void setValue(Game game, Object o) {
			game.scoreLoser = (short) (int) o;
		}

	},
	IS_REGULATION_TIME_WIN("is_regulation_time_win", FieldDatatype.BOOLEAN) {
		@Override
		public Object getIndexingValue(Game game) {
			return game.isRegulatimeTimeWin();
		}

		@Override
		void setValue(Game game, Object o) {
			game.isRegulatimeTimeWin = (boolean) o;
		}

	},
	DATE("date", FieldDatatype.DATE) {
		@Override
		public Object getIndexingValue(Game game) {
			// We could store the date as dd-MM-yyyy, or any other format, but it will
			// require internally extra processing to convert (and be able to do
			// comparing). To avoid this, we will directly convert the value to millis,
			// although we actually only care about the day
			return TimeUnit.DAYS.toMillis(game.getDate().toEpochDay());
		}

		@Override
		void setValue(Game game, Object o) throws Exception {
		}
	};

	private final String json;
	private final FieldDatatype fieldDatatype;
	private static Map<String, GameElasticsearchField> valuesByJsonMap = new HashMap<>();
	static {
		for (GameElasticsearchField field : GameElasticsearchField.values()) {
			valuesByJsonMap.put(field.getJson(), field);
		}
	}

	private GameElasticsearchField(String json, FieldDatatype fieldDatatype) {
		this.json = json;
		this.fieldDatatype = fieldDatatype;
	}

	abstract void setValue(Game game, Object o) throws Exception;

	@Override
	public String getJson() {
		return json;
	}

	@Override
	public FieldDatatype getFieldDatatype() {
		return fieldDatatype;
	}

	public static GameElasticsearchField valueOfCustom(String json) {
		return valuesByJsonMap.get(json);
	}
}