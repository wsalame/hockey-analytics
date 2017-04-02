package com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper;

import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.service.http.AsyncHttpCallWrapper;
import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHockeyScrapperController.class)
public interface HockeyScrapper extends IsConnected {
	void sendHttpRequest(AsyncHttpCallWrapper call);
}