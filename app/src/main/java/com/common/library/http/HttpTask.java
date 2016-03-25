package com.common.library.http;

import java.io.IOException;

import android.util.Log;

import com.common.library.http.HttpTask.TaskResponse;
import com.common.library.http.httpclient.TextHttpClient;
import com.common.library.http.method.GetMethod;
import com.common.library.http.method.HttpMethod;
import com.common.library.http.method.PostMethod;
import com.common.library.thread.ThreadWork;
import com.google.gson.Gson;

public abstract class HttpTask extends ThreadWork<HttpMethod, Void, String, TaskResponse> {
	private static final String TAG = "HttpTask";

	public HttpTask(Tracker tracker) {
		super(tracker);
	}

	@Override
	protected TaskResponse doInBackground(HttpMethod... params) {
		HttpMethod method = params[0];

		try{
			TextHttpClient httpClient = new TextHttpClient();
			String response = httpClient.execute(method);
			tryPrintLog(method, response);
			return new TaskResponse(method.getRequest(), response);
		}catch(IOException e){
			e.printStackTrace();
			publishError(e.getMessage());
			return null;
		}catch(UnsupportedOperationException e){
			// should not be happened, since only none stream request was allowed here.
			return null;
		}
	}

	private void tryPrintLog(HttpMethod method, String response){
		boolean canPrintLog = false;
		if(GetMethod.METHOD.equals(method.getMethod())){
			canPrintLog = true;
		}else if(PostMethod.METHOD.equals(method.getMethod())){
			PostMethod httpPost = (PostMethod) method;
			canPrintLog = httpPost.getBody().isStreaming();
		}
		
		if(canPrintLog){
			Log.d(TAG, "http url:" + method.getUrl());
			Log.d(TAG, "http request:" + method.getRequest());
			Log.d(TAG, "http response:" + response);
		}
	}
	
	public void doRequest(HttpMethod method, boolean cancelPreviewTasks) {
		executeParallel(cancelPreviewTasks, method);
	}
	
	public void doRequest(HttpMethod method) {
		executeParallel(false, method);
	}

	public static class TaskResponse {
		private String request;
		private String response;

		public TaskResponse(String request, String response) {
			this.request = request;
			this.response = response;
		}

		public String getRequest() {
			return request;
		}

		public String getResponse() {
			return response;
		}

		public <T> T getRequest(Class<T> requestClass) {
			return new Gson().fromJson(request, requestClass);
		}

		public <T> T getResponseContent(Class<T> responseClass) {
			return new Gson().fromJson(response, responseClass);
		}
	}

}