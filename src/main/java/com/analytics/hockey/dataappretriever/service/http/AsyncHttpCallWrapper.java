package com.analytics.hockey.dataappretriever.service.http;

import java.util.ArrayList;
import java.util.List;

import org.asynchttpclient.Param;
import org.asynchttpclient.cookie.Cookie;

public class AsyncHttpCallWrapper {
	private HttpVerb verb;
	private String url;
	private List<Cookie> cookies;
	private List<Param> queryParams;

	private AsyncHttpCallWrapper() {

	}

	public HttpVerb getVerb() {
		return verb;
	}

	public String getUrl() {
		return url;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public List<Param> getQueryParams() {
		return queryParams;
	}

	// Add other org.asynchttpclient.BoundRequestBuilder setters as needed
	public static class Builder {
		private final AsyncHttpCallWrapper call = new AsyncHttpCallWrapper();

		public Builder(String url, HttpVerb verb) {
			url(url);
			verb(verb);
		}

		public Builder verb(HttpVerb verb) {
			call.verb = verb;
			return this;
		}

		public Builder url(String url) {
			call.url = url;
			return this;
		}

		public Builder addCookie(Cookie cookie) {
			if (call.cookies == null) {
				call.cookies = new ArrayList<>();
			}
			call.cookies.add(cookie);
			return this;
		}

		public Builder addQueryParam(String name, String value) {
			if (call.queryParams == null) {
				call.queryParams = new ArrayList<>();
			}
			call.queryParams.add(new Param(name, value));
			return this;
		}

		public AsyncHttpCallWrapper build() {
			return call;
		}
	}
}
