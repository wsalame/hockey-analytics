package com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.analytics.hockey.dataappretriever.exception.GameUnmarshallException;
import com.analytics.hockey.dataappretriever.model.Game;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HockeyScrapperUtils {
	private static final TypeReference<LinkedHashMap<String, Object>> mapObjectMapperTypeRef = new TypeReference<LinkedHashMap<String, Object>>() {
	};

	private static final TypeReference<List<Object>> listObjectMapperTypeRef = new TypeReference<List<Object>>() {
	};

	private HockeyScrapperUtils() {

	}

	public static List<Game> unmarshallGames(String responseAsJson)
	        throws JsonParseException, JsonMappingException, IOException {
		List<Game> games = null;

		Map<String, Object> responseMap = responseToMap(responseAsJson);
		games = extractGames(responseMap, extractDate(responseMap));

		return games;
	}

	@SuppressWarnings("unchecked")
	private static List<Game> extractGames(Map<String, Object> responseMap, LocalDate date)
	        throws GameUnmarshallException {
		List<Object> o = (List<Object>) responseMap.get("games"); // TODO hardcoded values
		List<Game> games = o.stream().map(x -> new Game((Map<String, Object>) x, date)).collect(Collectors.toList());

		return games;
	}

	private static LocalDate extractDate(Map<String, Object> responseMap) {
		return LocalDate.of((int) responseMap.get("year"), (int) responseMap.get("month"),
		        (int) responseMap.get("day"));
	}

	public static Map<String, Object> responseToMap(String responseAsJson)
	        throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(responseAsJson, mapObjectMapperTypeRef);
	}

	public static List<Object> responseToList(String responseAsJson)
	        throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(responseAsJson, listObjectMapperTypeRef);
	}
}