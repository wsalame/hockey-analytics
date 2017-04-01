package com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper;

import com.analytics.hockey.dataappretriever.model.IsConnected;
import com.analytics.hockey.dataappretriever.service.http.AsyncHttpCallWrapper;

public interface HockeyScrapper extends IsConnected {
	void sendHttpRequest(AsyncHttpCallWrapper call);
}
