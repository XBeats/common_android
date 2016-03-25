package com.common.library.io.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

import com.common.library.io.OnProgressListener;
import com.common.library.io.ProgressAwareInputStream;

/**
 * This is the base download Util, developers should never use it,
 *  if you want download file, you should use FileDownloader instead
 *  since it has UI callback.
 */
public class DownloadUtils {
	private static final int BUFFER_SIZE = 4096;
	private static final int TIMEOUT_DURATION = 3000;

	/**
	 * Download bitmap of small size, if bitmap is very big you can use 
	 * {@link DownloadUtils#downloadBitmap(String, Options, OnProgressListener)} instead.
	 * @throws IOException 
	 */
	public static Bitmap downloadBitmap(String imageUrl, OnProgressListener progressListener) throws IOException  {
		return downloadBitmap(imageUrl, null, progressListener);
	}
	
	/**
	 * Download bitmap of big size.
	 * @param imageUrl
	 * @param options
	 * @param progressListener progress update callback {@link OnProgressListener}
	 * @return bitmap downloaded from imagUrl
	 * @throws IOException 
	 */
	public static Bitmap downloadBitmap(String imageUrl, Options options, OnProgressListener progressListener) 
			throws IOException  {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boolean result = downloadToOutputStream(imageUrl, outputStream, progressListener);
		if(result){
			if(options != null){
				return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size(), options);
			}else{
				return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Download content into specified output stream.
	 * 
	 * @param downloadUrl
	 *            file to download from
	 * @param options
	 * @param progressListener
	 *            progress update callback {@link OnProgressListener}
	 * @return return true if download successfully, otherwise return false
	 * @throws IOException 
	 */
	public static boolean downloadToOutputStream(String downloadUrl, OutputStream outputStream, 
			OnProgressListener progressListener) throws IOException{
		HttpURLConnection connection = null;
		ProgressAwareInputStream inputStream = null;

		try {
			URL url = new URL(downloadUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setAllowUserInteraction(true);
			connection.setConnectTimeout(TIMEOUT_DURATION);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Android Client");
			
			// always check HTTP response code first
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String disposition = connection.getHeaderField("Content-Disposition");
				String contentType = connection.getContentType();
				int contentLength = connection.getContentLength();

				// retrieve file name from header field or URL
				String fileName = "";
				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					String stringUrl = downloadUrl.toString();
					fileName = stringUrl.substring(stringUrl.lastIndexOf("/") + 1, stringUrl.length());
				}

				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("Content-Length = " + contentLength);
				System.out.println("fileName = " + fileName);

				// opens input stream from the HTTP connection
				inputStream = new ProgressAwareInputStream(connection.getInputStream(), contentLength, 0l, downloadUrl);
				inputStream.setOnProgressListener(progressListener);

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				return true;
			} else{
				if(progressListener != null){
					progressListener.onError("Invalid http response code:" + responseCode, downloadUrl);
				}
				return false;
			}
		} finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
			IOUtils.close(connection);
		}
	}
	
	/**
	 * Download file.
	 * @param fileURL file download URL
	 * @param outputFile the output file for download
	 * @param isBreakpointMode if set true, it will download in breakpoint mode
	 * @param progressListener  progress update callback {@link OnProgressListener}
	 * @throws IOException
	 */
	public static void download(String fileURL, File outputFile, boolean isBreakpointMode, 
			OnProgressListener progressListener) {
		if(TextUtils.isEmpty(fileURL)){
			throw new RuntimeException("fileURL cannot be empty or null.");
		}
		
		if(outputFile == null){
			throw new RuntimeException("outputFile cannot be null.");
		}
		
		HttpURLConnection connection = null;
		ProgressAwareInputStream inputStream = null;
		RandomAccessFile randomAccessFile = null;
		OutputStream outputStream = null;
		
		boolean needCreateFile = false;
		if (isBreakpointMode){
			// create new file if not exist
			if (!outputFile.exists()){
				needCreateFile = true;
			}
		} else {
			// make sure that output file was new file when in
			// non-breakpoint mode
			if (outputFile.exists()) {
				FileUtils.deleteFiles(outputFile);
				needCreateFile = true;
			} else {
				needCreateFile = true;
			}
		}
		
		if(needCreateFile){
			try {
				FileUtils.createFile(outputFile);
			} catch (IOException e) {
				if(progressListener != null){
					progressListener.onError("cannot create new file caused by " + e.getMessage(), fileURL);
				}
			}
		}
		
		try {
			long localSize = outputFile.length();
			URL url = new URL(fileURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(TIMEOUT_DURATION);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Android Client");
			if(isBreakpointMode){
				connection.setRequestProperty("Range", "bytes=" + localSize + "-");
			}
			
			// always check HTTP response code first
			int responseCode = connection.getResponseCode();
			if(isBreakpointMode){
				if(responseCode == HttpURLConnection.HTTP_PARTIAL) {
					long remainSize = connection.getContentLength();
					long totalSize = localSize + remainSize;
					inputStream = new ProgressAwareInputStream(connection.getInputStream(), totalSize, localSize, fileURL);
					inputStream.setOnProgressListener(progressListener);
					
					// seek position the be the end of file
					randomAccessFile = new RandomAccessFile(outputFile.getPath(), "rw");
					randomAccessFile.seek(localSize);
					
					int bytesRead = -1;
					byte[] buffer = new byte[BUFFER_SIZE];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						randomAccessFile.write(buffer, 0, bytesRead);
					}
				} else{
					if(progressListener != null){
						progressListener.onError("invalidate http response code:" + responseCode, fileURL);
					}
				}
			} else {
				if (responseCode == HttpURLConnection.HTTP_OK) {
					String disposition = connection.getHeaderField("Content-Disposition");
					String contentType = connection.getContentType();
					int contentLength = connection.getContentLength();
					
					// retrieve file name from header field or URL
					String fileName = "";
					if (disposition != null) {
						// extracts file name from header field
						int index = disposition.indexOf("filename=");
						if (index > 0) {
							fileName = disposition.substring(index + 10, disposition.length() - 1);
						}
					} else {
						// extracts file name from URL
						String stringUrl = fileURL.toString();
						fileName = stringUrl.substring(stringUrl.lastIndexOf("/") + 1, stringUrl.length());
					}

					System.out.println("Content-Type = " + contentType);
					System.out.println("Content-Disposition = " + disposition);
					System.out.println("Content-Length = " + contentLength);
					System.out.println("fileName = " + fileName);

					// opens input stream from the HTTP connection
					inputStream = new ProgressAwareInputStream(connection.getInputStream(), contentLength, 0l, fileURL);
					inputStream.setOnProgressListener(progressListener);

					// delete old and create new file
					FileUtils.deleteFiles(outputFile);
					FileUtils.createFile(outputFile);

					// opens an output stream to save into file
					outputStream = new FileOutputStream(outputFile);

					int bytesRead = -1;
					byte[] buffer = new byte[BUFFER_SIZE];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			if(progressListener != null){
				progressListener.onError("MalformedURLException:" + e.getMessage() + " for " + fileURL, fileURL);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if(progressListener != null){
				progressListener.onError("IOException:" + e.getMessage(), fileURL);
			}
		}finally {
			IOUtils.closeQuietly(randomAccessFile);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
			IOUtils.close(connection);
		}
	}
}
