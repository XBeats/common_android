package com.common.library.images;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.common.library.BuildConfig;
import com.common.library.io.OnProgressListener;
import com.common.library.io.utils.DownloadUtils;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.<br>
 */
public class Images extends ImageResizer {
    private static final String TAG = "ImageFetcher";

    private static final int DISK_CACHE_INDEX = 0;
    
    private static Images mInstance;
    private static Object mLocker = new Object();
    
    private Images(Context context) {
        super(context, ImageCache.ImageCacheParams.DEFAULT_DISK_DIR_NAME);
    }
    
    private Images(Context context, String diskCacheDirName){
    	super(context, diskCacheDirName);
    }
    
    private Images(Context context, ImageCache.ImageCacheParams cacheParams){
    	super(context, cacheParams);
    }
    
	/**
	 * Create and return a instance of {@link Images} if not created before.
	 * 
	 * @param context
	 * @return Initialized {@link Images} instance.
	 */
    public static Images build(Context context){
    	if(mInstance == null){
    		synchronized (mLocker) {
    			mInstance = new Images(context);
			}
    	}
    	return mInstance;
    }
    
    /**
     * Set image download URL.
     *  
     * @param url download url
     * @return current BitmapHunter object
     */
    public ImageHunter load(String url){
    	return mBitmapHunter.load(url);
    }
    
	/**
	 * About initialization images engine, it was suggested to be done in
	 * Application.
	 * 
	 * @param context
	 * @param diskCacheDir
	 * @return Initialized {@link Images} instance.
	 */
    public static Images initialize(Context context, String diskCacheDir){
    	if(mInstance == null){
    		synchronized (mLocker) {
    			mInstance = new Images(context, diskCacheDir);
			}
    	}
    	return mInstance;
    }
    
    /**
	 * About initialization images engine, it was suggested to be done in
	 * Application.
	 * 
	 * @param context
	 * @param cacheParams
	 * @return Initialized {@link Images} instance.
	 */
    public static Images initialize(Context context, ImageCache.ImageCacheParams cacheParams){
    	if(mInstance == null){
    		synchronized (mLocker) {
    			mInstance = new Images(context, cacheParams);
			}
    	}
    	return mInstance;
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     */
    private Bitmap processBitmap(String data, int width, int height, OnProgressListener progressListener) 
    		throws IOException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }

        final String key = ImageCache.hashKeyForDisk(data);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot;
        
        synchronized (mImageCache.mDiskCacheLock) {
            // Wait for disk cache to initialize
            while (mImageCache.mDiskCacheStarting) {
                try {
                	mImageCache.mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }

            DiskLruCache diskLruCache = mImageCache.getDiskLruCache();
            if (diskLruCache != null) {
                try {
                    snapshot = diskLruCache.get(key);
                    if (snapshot == null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "processBitmap, not found in image cache, downloading...");
                        }
                        DiskLruCache.Editor editor = diskLruCache.edit(key);
                        if (editor != null) {
                            if (downloadUrlToStream(data, editor.newOutputStream(DISK_CACHE_INDEX), progressListener)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        snapshot = diskLruCache.get(key);
                    }
                    if (snapshot != null) {
                        fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileDescriptor = fileInputStream.getFD();
                    }
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        Bitmap bitmap = null;
        if (fileDescriptor != null) {
            bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, width, height, mImageCache);
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {}
        }
        return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data, int width, int height, OnProgressListener progressListener) throws IOException {
        return processBitmap(String.valueOf(data), width, height, progressListener);
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     * @throws IOException 
     */
    protected boolean downloadUrlToStream(String urlString, OutputStream outputStream, 
    		OnProgressListener progressListener) throws IOException {
        disableConnectionReuseIfNecessary();
        return DownloadUtils.downloadToOutputStream(urlString, outputStream, progressListener);
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    private static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
    
    // global cache operations
    public static void clearCache(Context context) {
    	Images.build(context).clearCacheInBackground();
    }
    
    public static void closeCache(Context context) {
    	Images.build(context).closeCacheInBackground();
    }
    
    public static void flushCache(Context context) {
    	Images.build(context).flushCacheInBackground();
    }
}
