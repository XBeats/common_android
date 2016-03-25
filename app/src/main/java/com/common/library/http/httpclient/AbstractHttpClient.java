package com.common.library.http.httpclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.annotation.TargetApi;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.common.library.http.body.HttpBody;
import com.common.library.http.method.GetMethod;
import com.common.library.http.method.HttpMethod;
import com.common.library.http.method.PostMethod;
import com.common.library.io.utils.IOUtils;

public abstract class AbstractHttpClient<T> {
	private static final String TAG = "http client";
	protected final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MiB

	public abstract T execute(HttpMethod httpMethod) throws IOException;

	protected InputStream doHttpRequest(HttpMethod httpMethod) throws IOException {
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		// create URL for POST method or GET method
		URL url = null;
		if(GetMethod.METHOD.equals(httpMethod.getMethod())){
			url = new URL(httpMethod.getUrl() + "?" + httpMethod.getRequest());
		}else{
			url = new URL(httpMethod.getUrl());
		}

		try {
			connection = (HttpURLConnection) url.openConnection();
			
			// only "POST" method need do as follows
			if(PostMethod.METHOD.equals(httpMethod.getMethod())){
				connection.setDoInput(true);
				connection.setDoOutput(true);
			}
			
			connection.setRequestMethod(httpMethod.getMethod());
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(false);
			connection.setReadTimeout(httpMethod.getReadTimeout());
			connection.setConnectTimeout(httpMethod.getConnectTimeout());
			connection.setRequestProperty("Charset", httpMethod.getCharset());
			
			if(httpMethod.isKeepAlive()){
				connection.setRequestProperty("Connection", "Keep-Alive");
			}
			
			if(!TextUtils.isEmpty(httpMethod.getUserAgent())){
				connection.setRequestProperty("User-Agent", httpMethod.getUserAgent());
			}
			
			if(httpMethod.isAcceptEncoding()){
				connection.setRequestProperty("Accept-Encoding", "gzip");
			}
			
			if(PostMethod.METHOD.equals(httpMethod.getMethod())){
				// set content type for POST
				PostMethod httpPost = (PostMethod) httpMethod;
				HttpBody httpBody = httpPost.getBody();
				connection.setRequestProperty("Content-Type", httpBody.getContentType());
				 connection.setRequestProperty("Content-Length", String.valueOf(httpBody.getContentLength()));
				
				// disable cache for write output stream
				if(httpBody.isStreaming()){
					// connection.setFixedLengthStreamingMode(fileSize);// not available until API 19
					connection.setChunkedStreamingMode(0);
				}
			}
			
			// set extra headers
			Map<String, String> headers = httpMethod.getHeaders();
			if (headers != null && headers.size() > 0) {
				for (String key : headers.keySet()) {
					connection.setRequestProperty(key, headers.get(key));
				}
			}
			
			// do connect
			connection.connect();
			outputStream = connection.getOutputStream();

			// write data for POST
			if(PostMethod.METHOD.equals(httpMethod.getMethod())){
				PostMethod httpPost = (PostMethod) httpMethod;
				HttpBody httpBody = httpPost.getBody();
				httpBody.writeTo(outputStream);
			}
			
			// read data if need
			// note: getInputStream() must be called after data has been write to OutputStream
			inputStream = connection.getInputStream();
			if(httpMethod.haveResponse()){
				String encoding  = connection.getContentEncoding();
				if(!TextUtils.isEmpty(encoding) && encoding.contains("gzip")){
					return new GZIPInputStream(inputStream);
				}else{
					return inputStream;
				}
			}else{
				printLog(httpMethod);
				return null;
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
			IOUtils.close(connection);
		}
	}
	
	protected void printLog(HttpMethod httpMethod, String response){
		String request = null;
		try{
			request = httpMethod.getRequest();
		}catch(UnsupportedOperationException e){
			// since some body are streaming, have no text content
		}
		
		if(request != null && request.length() > 0){
			Log.d(TAG, "http url:" + httpMethod.getUrl());
			Log.d(TAG, "http request:" + request);
			Log.d(TAG, "http response:" + response);
		}
	}
	
	protected void printLog(HttpMethod httpMethod){
		String request = null;
		try{
			request = httpMethod.getRequest();
		}catch(UnsupportedOperationException e){
			// since some body are streaming, have no text content
		}
		
		if(request != null && request.length() > 0){
			Log.d(TAG, "http url:" + httpMethod.getUrl());
			Log.d(TAG, "http request:" + request);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void enableHttpCache(long cacheSize, File httpCacheDir) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			try {
				HttpResponseCache.install(httpCacheDir, cacheSize);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			try {
				Class.forName("android.net.http.HttpResponseCache").getMethod("install", File.class, long.class)
						.invoke(null, httpCacheDir, cacheSize);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
	
	public int getCACHE_SIZE() {
		return CACHE_SIZE;
	}

}
