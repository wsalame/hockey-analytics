package com.analytics.hockey.dataappretriever.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TeamTest {
	@Test
	public void buildTeamIndex_returnsDefaultValue() {
		assertEquals(new Team().buildIndex(), "teams");
	}

	@Test
	public void buildTeamType_returnsDefaultValue() {
		assertEquals(new Team().buildType(), "teams");
	}
}
