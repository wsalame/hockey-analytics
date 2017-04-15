package com.analytics.hockey.dataappretriever.controller.external.elasticsearch.query;

public class RangeParameter {
	private Object start;
	private Object end;
	private String format;

	public RangeParameter() {

	}

	public Object getStart() {
		return start;
	}

	public void setStart(Object start) {
		this.start = start;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Object getEnd() {
		return end;
	}

	public void setEnd(Object end) {
		this.end = end;
	}
}
