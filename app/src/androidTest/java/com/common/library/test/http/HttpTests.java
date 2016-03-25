package com.common.library.test.http;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.common.library.http.body.ByteArrayBody;
import com.common.library.http.body.FileBody;
import com.common.library.http.body.JsonBody;
import com.common.library.http.body.TextBody;
import com.common.library.http.body.UrlEncodedFormBody;
import com.common.library.http.body.multipart.MultipartBody;
import com.common.library.http.httpclient.ByteArrayHttpClient;
import com.common.library.http.httpclient.JsonHttpClient;
import com.common.library.http.httpclient.TextHttpClient;
import com.common.library.http.method.GetMethod;
import com.common.library.http.method.HttpMethod;
import com.common.library.http.method.PostMethod;
import com.common.library.io.OnProgressListener;

import android.test.AndroidTestCase;

public class HttpTests extends AndroidTestCase{

	public void testGetMethod(){
		TextHttpClient client = new TextHttpClient();
		
		HttpMethod method = new GetMethod("www.baidu.com");
		method.addHeader("key1", "value1");
		method.addHeader("key2", "value2");
		
		try {
			String textResult = client.execute(method);
			System.out.println(textResult);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testPostMethodByteArrayBody(){
		ByteArrayHttpClient client = new ByteArrayHttpClient();
		
		ByteArrayBody body = new ByteArrayBody("hello world".getBytes());
		PostMethod method = new PostMethod("www.baidu.com");
		method.setBody(body);
		
		try {
			byte[] result = client.execute(method);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testPostMethodJsonBody(){
		JsonHttpClient client = new JsonHttpClient();
		
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("name", "zhangsan");
			jsonObj.put("age", 110);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JsonBody body = new JsonBody(jsonObj);
		PostMethod method = new PostMethod("www.baidu.com");
		method.setBody(body);
		
		try {
			JSONObject json = client.execute(method);
			System.out.println(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testPostMethodUrlEncodedFormBody(){
		TextHttpClient client = new TextHttpClient();
		
		UrlEncodedFormBody body = new UrlEncodedFormBody();
		body.addFormData("key1", "value1");
		body.addFormData("key2", "value2");
		
		PostMethod method = new PostMethod("www.baidu.com");
		method.setBody(body);
		
		try {
			String result = client.execute(method);
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testPostMethodFileBody(){
		TextHttpClient client = new TextHttpClient();
		
		File file = new File("D://hello_world.txt");
//		FileBody body = new FileBody(file);
		FileBody body = new FileBody(file, 0, new OnProgressListener() {
			
			@Override
			public void onProgress(int percentage, String tag) {
				// UI 进度更新
			}
			
			@Override
			public void onError(String errorMsg, String tag) {
				// UI 报错
			}
			
			@Override
			public void onCompleted(String tag) {
				// UI 提示完成
			}
		} );
		PostMethod method = new PostMethod("www.baidu.com");
		method.setBody(body);
		
		try {
			String result = client.execute(method);
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testPostMethodMultiPart(){
		TextHttpClient client = new TextHttpClient();
		PostMethod method = new PostMethod("www.baidu.com");
		MultipartBody body = new MultipartBody();
		body.addPart("part1", new TextBody("hello world"));
		body.addPart("part2", new ByteArrayBody("hello world".getBytes()));
		body.addPart("part3", new FileBody("D://hello.mp3"));
		method.setBody(body);
		
		try {
			String result = client.execute(method);
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
