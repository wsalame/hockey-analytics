package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;

public interface DataIndexer {

	void createIndex(String indexName, boolean deleteOldIndexIfExists) throws IOException;
	
	void insertGame(Game game) throws Exception;
}
