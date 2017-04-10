package com.analytics.hockey.dataappretriever.model;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface IsConnected {
	/**
	 * Function where is defined the mechanism to close connection with connected
	 * component when the system exists
	 */
	void addClientShutDownHook();

	/**
	 * Awaits that the connection to component is open/ready. It is not always instant as
	 * there could many round-trips of authentification and handshakes.
	 */
	void awaitInitialization();

	/**
	 * Starts the connection with component
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 */
	void start() throws IOException, TimeoutException;
}
