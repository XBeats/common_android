package com.common.library.http.method;

import com.common.library.http.body.HttpBody;

public class PostMethod extends HttpMethod {
	public static final String METHOD = "POST";
	private HttpBody httpBody;

	public PostMethod(String url) {
		super(url);
	}

	@Override
	public String getMethod() {
		return METHOD;
	}
	
	@Override
	public String getRequest() {
		return httpBody.getContent();
	}
	
	public <T extends HttpBody> void setBody(T httpBody) {
		this.httpBody = httpBody;
	}

	public HttpBody getBody() {
		return httpBody;
	}

}
