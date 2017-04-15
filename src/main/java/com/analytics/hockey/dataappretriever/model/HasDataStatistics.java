package com.analytics.hockey.dataappretriever.model;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.StatisticsLoader;
import com.google.inject.ImplementedBy;

/**
 * Implementation of this interface is intended to contains several statistics regarding
 * the data, to help optimize our implementations. For example, it would be possible to
 * retrieve the typical number of characters in a document containing the score of a game.
 * For example, if we know a typical Friday contains 6 games, and every game document is
 * about 100 characters, then we could initialize a StringBuilder of 100*6 capacity. This
 * way, we avoid constant resizing of the internal array.
 * 
 * The initial idea was to have all those value dynamically loaded at startup of the
 * server, and/or after some expiration date, but by lack of time, the values are
 * hardcoded for now.
 */
@ImplementedBy(StatisticsLoader.class)
public interface HasDataStatistics {
	/**
	 * Number of teams in a season
	 * 
	 * @return Number of teams in a season
	 */
	int getNumberOfTeams();

	/**
	 * Get the average number of characters in a game document
	 * 
	 * @return The average number of characters in a game document
	 */
	int getAverageGameDocumentCharacters();

	/**
	 * Get the average number of characters in a team document
	 * 
	 * @return The average number of characters in a team document
	 */
	int getAverageTeamDocumentCharacters();

	/**
	 * Get the average number of games for a given day of the week. First day is Sunday.
	 * Zero based
	 * 
	 * @param dayOfTheWeek
	 *            Ordinal value of the day of the week. Zero based
	 * @return The average number of games for given day of the week
	 */
	int getAverageNumberOfGamesForGivenDay(int dayOfTheWeek);
}
