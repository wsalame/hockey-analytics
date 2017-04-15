package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import com.analytics.hockey.dataappretriever.model.HasDataStatistics;

/**
 * @inheritDoc
 * @see {{@link HasDataStatistics}
 */
public class StatisticsLoader implements HasDataStatistics {
	// GAME
	public enum Days {
		SUNDAY(3),
		MONDAY(4),
		TUESDAY(7),
		WEDNESDAY(4),
		THURSDAY(7),
		FRIDAY(6),
		SATURDAY(12);

		private final int averageNumberOfGames;

		Days(int averageNumberOfGames) {
			this.averageNumberOfGames = averageNumberOfGames;
		}

		public int getAverageNumberOfGames() {
			return averageNumberOfGames;
		}
	}

	private final int AVERAGE_GAME_DOCUMENT_CHARACTERS = 215;

	// TEAM
	private final int AVERAGE_TEAM_DOCUMENT_CHARACTERS = 80;;
	private final int NUMBER_OF_TEAMS = 30;

	/**
	 * @inheritDoc
	 */
	@Override
	public int getNumberOfTeams() {
		return NUMBER_OF_TEAMS;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int getAverageGameDocumentCharacters() {
		return AVERAGE_GAME_DOCUMENT_CHARACTERS;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int getAverageTeamDocumentCharacters() {
		return AVERAGE_TEAM_DOCUMENT_CHARACTERS;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int getAverageNumberOfGamesForGivenDay(int dayOfTheWeek) {
		return Days.values()[dayOfTheWeek].getAverageNumberOfGames();
	}
}
