package com.common.library.http.httpclient;

import java.io.IOException;
import java.io.InputStream;

import com.common.library.http.method.HttpMethod;
import com.common.library.io.utils.IOUtils;

public class TextHttpClient extends AbstractHttpClient<String> {

	@Override
	public String execute(HttpMethod httpMethod) throws IOException {
		InputStream inputStream = doHttpRequest(httpMethod);
		return IOUtils.toString(inputStream);
	}
}
