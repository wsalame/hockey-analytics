package com.analytics.hockey.dataappretriever.controller.external.elasticsearch;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;
import org.mockito.Mockito;

import com.analytics.hockey.dataappretriever.model.Game;
import com.analytics.hockey.dataappretriever.model.Team;

/**
 * 
 * Every season goes from October (month = 10) to April (month = 4) the following year
 *
 */
public class ElasticsearchTest { // TODO rename class name

	@Test
	public void buildGameIndex_lastMonthOfFirstYear() {
		Game g = new Game(LocalDate.of(2015, 12, 31));
		assertEquals(g.buildIndex(), "2015-2016");
	}

	@Test
	public void buildGameIndex_firstMonthOfFirstYear() {
		Game g = new Game(LocalDate.of(2015, 10, 1));
		assertEquals(g.buildIndex(), "2015-2016");
	}

	@Test
	public void buildGameIndex_firstMonthOfSecondYear() {
		Game g = new Game(LocalDate.of(2015, 1, 1));
		assertEquals(g.buildIndex(), "2014-2015");
	}

	@Test
	public void buildGameIndex_lastMonthOfSecondYear() {
		Game g = new Game(LocalDate.of(2015, 4, 30));
		assertEquals(g.buildIndex(), "2014-2015");
	}

	@Test
	public void buildGameIndex_isCallingBuildIndexWithPrimitivesTypesExtractedFromGameObject() {
		// We are making sure #buildIndex(Game) is calling the overloaded function
		// #buildIndex(int, int), and is not reimplementing its own way of
		// building the index. This way, we don't need to add extra tests for it

		// given
		int year = 2015;
		int month = 4;
		Game mockedGame = Mockito.spy(Game.class);
		mockedGame.setDate(LocalDate.of(year, month, 1));

		// when
		mockedGame.buildIndex();

		// then
		Mockito.verify(mockedGame).buildGamesIndex(month, year);
	}

	@Test
	public void buildTeamIndex_returnsDefaultValue() {
		assertEquals(new Team().buildIndex(), "teams");
	}

	@Test
	public void buildTeamType_returnsDefaultValue() {
		assertEquals(new Team().buildType(), "teams");
	}
}