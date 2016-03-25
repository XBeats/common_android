package com.common.library.http.method;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpMethod {
	protected String url;
	protected Map<String, String> headers;
	private String charset = Charset.defaultCharset().name();
	private String userAgent;
	
	private int connectTimeout = 1000 * 60;
	private int readTimeout = connectTimeout;
	protected boolean haveResponse = true;
	private boolean acceptEncoding = false;
	private boolean keepAlive = true;

	public HttpMethod(String url) {
		this.url = url;
		this.headers = new HashMap<String, String>();
	}

	public String getUrl() {
		return url;
	}

	public abstract String getMethod();

	public abstract String getRequest();

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isAcceptEncoding() {
		return acceptEncoding;
	}

	public void setAcceptEncoding(boolean acceptEncoding) {
		this.acceptEncoding = acceptEncoding;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public void setHeader(String name, String value) {
		headers.clear();
		headers.put(name, value);
	}

	public void setHeaders(Map<String, String> headers) {
		headers.clear();
		headers.putAll(headers);
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addHeaders(Map<String, String> headers) {
		headers.putAll(headers);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public boolean haveResponse() {
		return haveResponse;
	}

	public void haveResponse(boolean haveResponse) {
		this.haveResponse = haveResponse;
	}
	
	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

}
