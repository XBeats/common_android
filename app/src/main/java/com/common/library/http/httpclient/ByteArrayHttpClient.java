package com.common.library.http.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.common.library.http.method.HttpMethod;
import com.common.library.io.utils.IOUtils;

public class ByteArrayHttpClient extends AbstractHttpClient<byte[]> {

	@Override
	public byte[] execute(HttpMethod httpMethod) throws IOException {
		InputStream inputStream  = doHttpRequest(httpMethod);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copyLarge(inputStream, outputStream);
		return outputStream.toByteArray();
	}

}
