package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface IsConnected {
	void connect() throws IOException, TimeoutException;

	void addClientShutDownHook();
}
