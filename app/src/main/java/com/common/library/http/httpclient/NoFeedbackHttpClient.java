package com.common.library.http.httpclient;

import java.io.IOException;
import java.io.InputStream;

import com.common.library.http.method.HttpMethod;

public class NoFeedbackHttpClient extends AbstractHttpClient<Void> {
	
	@Override
	protected InputStream doHttpRequest(HttpMethod httpMethod) throws IOException {
		httpMethod.haveResponse(false);
		return super.doHttpRequest(httpMethod);
	}

	@Override
	public Void execute(HttpMethod httpMethod) throws IOException {
		return null;
	}

}
