package com.common.library.http.body.multipart;

import com.common.library.http.body.HttpBody;

class WrappedFormBody {
	private HttpBody httpBody;
	private String fieldName;

	public WrappedFormBody(String fieldName, HttpBody httpBody) {
		this.fieldName = fieldName;
		this.httpBody = httpBody;
	}

	public HttpBody getHttpBody() {
		return httpBody;
	}

	public void setHttpBody(HttpBody httpBody) {
		this.httpBody = httpBody;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

}
