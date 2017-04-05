package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface IsConnected {
	void addClientShutDownHook();
	
	void awaitInitialization();
	
	void start() throws IOException, TimeoutException;
}
