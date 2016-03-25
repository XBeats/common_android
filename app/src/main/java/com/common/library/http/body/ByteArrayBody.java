package com.common.library.http.body;

import java.io.IOException;
import java.io.OutputStream;

import com.common.library.http.ContentType;

public class ByteArrayBody extends HttpBody {
	protected final byte[] data;
	
	public ByteArrayBody(byte[] data){
		this.data = data;
	}
	
	@Override
	public String getContent() {
		throw new UnsupportedOperationException("ByteArrayBody does not implement #getContent().");
	}

	@Override
	public long getContentLength() {
		return data.length;
	}

	@Override
	public String getContentType() {
		return ContentType.DEFAULT_BINARY;
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		outputStream.write(data);
		outputStream.flush();
	}
	
	@Override
	public boolean isStreaming() {
		return true;
	}

}
