package com.common.library.io;

import java.io.File;

import android.text.TextUtils;

import com.common.library.io.utils.DownloadUtils;
import com.common.library.thread.ThreadWork;

/**
 * A file download utils which can notify download status like: progress update,
 * error and complete.
 */
public abstract class FileDownloader extends ThreadWork<Void, Integer, String, Void> {
	private String mDownloadUrl;
	private File mDestFile;
	private boolean mUseBreakpoint;

	public FileDownloader(Tracker tracker, String downloadUrl, File destFile, boolean useBreakpoint) {
		super(tracker);
		mDownloadUrl = downloadUrl;
		mDestFile = destFile;
		mUseBreakpoint = useBreakpoint;
	}

	@Override
	protected Void doInBackground(Void... params) {
		if(TextUtils.isEmpty(mDownloadUrl)){
			throw new RuntimeException("download url cannot be empty or null.");
		}
		
		if(mDestFile == null){
			throw new RuntimeException("destination file cannot be null.");
		}
		
		DownloadUtils.download(mDownloadUrl, mDestFile, mUseBreakpoint,
				new OnProgressListener() {

					@Override
					public void onProgress(int percentage, String tag) {
						publishProgress(percentage);
					}

					@Override
					public void onError(String errorMsg, String tag) {
						publishError(errorMsg);
					}

					@Override
					public void onCompleted(String tag) {
						// do nothing, onSuccess() will be
						// executed instead.
					}
				});
		return null;
	}

	/**
	 * Start download task.
	 * 
	 * @param oneByOne
	 *            if set true download task one by one polled from queue,
	 *            otherwise download tasks at the same time.
	 */
	public void startDownload(boolean oneByOne) {
		if (oneByOne) {
			executeSerial();
		} else {
			executeParallel();
		}
	}
}
