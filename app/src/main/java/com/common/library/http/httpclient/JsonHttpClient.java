package com.common.library.http.httpclient;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.common.library.http.method.HttpMethod;
import com.common.library.io.utils.IOUtils;

public class JsonHttpClient extends AbstractHttpClient<JSONObject> {

	@Override
	public JSONObject execute(HttpMethod httpMethod) throws IOException {
		InputStream inputStream = doHttpRequest(httpMethod);
		String jsonStr = IOUtils.toString(inputStream);
		try {
			return new JSONObject(jsonStr);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

}
