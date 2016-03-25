package com.common.library.http.body;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.common.library.http.ContentType;
import com.common.library.io.OnProgressListener;
import com.common.library.io.utils.IOUtils;

public class FileBody extends HttpBody  {
	protected final File file;
	private long uploadedSize;
	private OnProgressListener progressListener;
    
    public FileBody(File file){
    	this.file = file;
    }
    
    public FileBody(String filePath){
    	this.file = new File(filePath);
    }

    public FileBody(File file, long uploadedSize, OnProgressListener listener){
    	this.file = file;
    	this.uploadedSize = uploadedSize;
    	this.progressListener = listener;
    }
    
    public FileBody(String filePath, long uploadedSize, OnProgressListener listener){
    	this.file = new File(filePath);
    	this.uploadedSize = uploadedSize;
    	this.progressListener = listener;
    }
    
	@Override
	public String getContentType() {
		return ContentType.DEFAULT_BINARY;
	}

	@Override
	public String getContent() {
		throw new UnsupportedOperationException("FileBody does not implement #getContent().");
	}

	@Override
	public long getContentLength() {
		return file.length();
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		FileInputStream fin = new FileInputStream(file);
		IOUtils.copyLarge(fin, outputStream);
		outputStream.flush();
	}
	
	@Override
	public boolean isStreaming() {
		return true;
	}
	
	public File getFile(){
		return file;
	}
	
	public long getUploadedSize(){
		return uploadedSize;
	}
	
	public OnProgressListener getProgressListener(){
		return progressListener;
	}

}
