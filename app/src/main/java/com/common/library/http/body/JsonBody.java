package com.common.library.http.body;

import org.json.JSONObject;

import com.common.library.http.ContentType;

public class JsonBody extends TextBody {

	public JsonBody(String json) {
		super(json);
	}
	
	public JsonBody(JSONObject json) {
		super(json.toString());
	}
	
	@Override
	public String getContentType() {
		return ContentType.APPLICATION_JSON;
	}

	@Override
	public String getContent() {
		return text;
	}

	@Override
	public long getContentLength() {
		return text.length();
	}

}
