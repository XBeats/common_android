package com.common.library.images;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;

import com.common.library.BuildConfig;
import com.common.library.io.OnProgressListener;
import com.common.library.thread.ThreadWork;

/**
 * This class wraps up completing some arbitrary long running work when loading
 * a bitmap to an ImageView. It handles things like using a memory and disk
 * cache, running the work in a background thread and setting a placeholder
 * image.
 */
public abstract class ImageWorker {
	private static final String TAG = "ImageWorker";

	// order messages
	private static final int MESSAGE_CLEAR = 1;
	private static final int MESSAGE_INIT_DISK_CACHE = 2;
	private static final int MESSAGE_FLUSH = 3;
	private static final int MESSAGE_CLOSE = 4;

	private static final int FADE_IN_TIME = 500;

	protected Context mContext;
	
	protected ImageCache mImageCache;
	protected Resources mResources;

	private boolean mFadeInBitmap = true;
	private boolean mTaskWorkPaused = false;
	private final Object mPauseWorkLock = new Object();
	
	protected ImageHunter mBitmapHunter;
	private WindowManager mWindowManager;

	protected ImageWorker(Context context, String diskCacheDirName){
		mContext = context;
		mResources = context.getResources();
		mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		mImageCache = new ImageCache(new ImageCache.ImageCacheParams(mContext, diskCacheDirName));
		mBitmapHunter = new ImageHunter();
		
		// disk cache initialization should be executed in child thread
		initDiskCache();
	}
	
	protected ImageWorker(Context context, ImageCache.ImageCacheParams cacheParams){
		mContext = context;
		mResources = context.getResources();
		mImageCache = new ImageCache(cacheParams);
		
		// disk cache initialization should be executed in child thread
		initDiskCache();
	}
	
	/**
	 * If set to true, the image will fade-in once it has been loaded in the
	 * background thread.
	 * 
	 * @return current {@link ImageWorker} object.
	 */
	public ImageWorker setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
		return this;
	}

	/**
	 * Subclasses should override this to define any processing or work that
	 * must happen to produce the final bitmap. This will be executed in a
	 * background thread and be long running. For example, you could resize a
	 * large bitmap here, or pull down an image from the network.
	 * 
	 * @param data
	 *            The data to identify which image to process, as provided by
	 *            {@link ImageWorker#loadImage(Object, android.widget.ImageView)}
	 * @param width required width
	 * @param height required height
	 * @param progressListener  download listener
	 * @return The processed bitmap
	 * @throws IOException
	 */
	protected abstract Bitmap processBitmap(Object data, int width, int height, 
			OnProgressListener progressListener) throws IOException;

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * 
	 * @param imageView
	 */
	public static void cancelWork(ImageView imageView) {
		final ImageHunter bitmapHunter = getBitmapWorkerTask(imageView);
		if (bitmapHunter != null) {
			bitmapHunter.getImageHunterJob().cancel(true);
			if (BuildConfig.DEBUG) {
				final Object bitmapData = bitmapHunter.mData;
				Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
			}
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no
	 * work in progress on this image view. Returns false if the work in
	 * progress deals with the same data. The work is not stopped in that case.
	 */
	private boolean cancelPotentialWork(Object data, ImageView imageView) {
		// BEGIN_INCLUDE(cancel_potential_work)
		final ImageHunter bitmapHunter = getBitmapWorkerTask(imageView);

		if (bitmapHunter != null) {
			final Object bitmapData = bitmapHunter.mData;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapHunter.getImageHunterJob().cancel(true);
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
				}
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
		// END_INCLUDE(cancel_potential_work)
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active work task (if any) associated with
	 *         this imageView. null if there is no such task.
	 */
	private static ImageHunter getBitmapWorkerTask(ImageView imageView) {
		final Drawable drawable = imageView.getDrawable();
		if (drawable instanceof AsyncDrawable) {
			final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
			return asyncDrawable.getBitmapWorkerTask();
		} 
		return null;
	}

	/**
	 * The actual ThreadWork that will asynchronously process the image.
	 */
	public class ImageHunter {
		private Object mData;
		private WeakReference<ImageView> mImageViewReference;
		private int mHeitht;
		private int mWidth;
		private Bitmap mPlaceHolder;
		private Bitmap mErrorImage;
		private ThreadWork<Void, Integer, String, BitmapDrawable> mHunterJob;
		private OnProgressListener mProgressListener;
		
		/**
	     * Set image download URL.
	     *  
	     * @param url download url
	     * @return current BitmapHunter object
	     */
		public ImageHunter load(Object data){
			mData = data;
			return this;
		}
		
		/**
		 * Display before image download finished.
		 * 
		 * @param image
		 *            place hold image
		 * @return current BitmapHunter object
		 */
		public ImageHunter placeHolder(Bitmap image) {
			mPlaceHolder = image;
			return this;
		}
		
		/**
		 * Display before image download finished.
		 * 
		 * @param image
		 *            place hold image resource
		 * @return current BitmapHunter object
		 */
		public ImageHunter placeHolder(int imageId){
			mPlaceHolder = BitmapFactory.decodeResource(mResources, imageId);
			return this;
		}
		
		/**
		 * Display when download image failed.
		 * 
		 * @param errorImage
		 * @return current BitmapHunter object
		 */
		public ImageHunter errorImage(Bitmap errorImage){
			mErrorImage = errorImage;
			return this;
		}
		
		/**
		 * Display when download image failed.
		 * 
		 * @param imageId
		 * @return current BitmapHunter object
		 */
		public ImageHunter errorImage(int imageId){
			mErrorImage = BitmapFactory.decodeResource(mResources, imageId);
			return this;
		}
		
		/**
		 * Set download listener, which can detect download progress, error and
		 * finish status.
		 * 
		 * @param listener
		 *            instance{@link OnProgressListener} object
		 * @return current BitmapHunter object
		 */
		public ImageHunter listener(OnProgressListener listener){
			mProgressListener = listener;
			return this;
		}
		
		/**
		 * Set the target {@link ImageView}, once download successfully it will be associates to bitmap.
		 * and once the method was called, the download job will be started
		 * at instance, so other parameters or setting must be called before
		 * this.
		 * 
		 * @param imageView
		 */
		@SuppressWarnings("deprecation")
		public void into(ImageView imageView) {
			mImageViewReference = new WeakReference<ImageView>(imageView);
			
			// height
			if (imageView.getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
				mHeitht = Integer.MAX_VALUE;
			} else if (imageView.getLayoutParams().height == LayoutParams.MATCH_PARENT) {
				mHeitht = mWindowManager.getDefaultDisplay().getHeight();
			} else {
				mHeitht = imageView.getLayoutParams().height;
			}

			// width
			if (imageView.getLayoutParams().width == LayoutParams.WRAP_CONTENT) {
				mWidth = Integer.MAX_VALUE;
			} else if (imageView.getLayoutParams().width == LayoutParams.MATCH_PARENT) {
				mWidth = mWindowManager.getDefaultDisplay().getWidth();
			} else {
				mWidth = imageView.getLayoutParams().width;
			}

			BitmapDrawable value = null;
			if (mImageCache != null) {
				// search from memory cache
				value = mImageCache.getBitmapFromMemCache(mData.toString());
				
				// search from disk cache, if found insert it into memory cache again
				if (value == null){
					Bitmap bitmap = mImageCache.getBitmapFromDiskCache(mData.toString());
					if (bitmap != null) {
						value = new BitmapDrawable(mResources, bitmap);
						mImageCache.addBitmapToCache(mData.toString(), value);
					}
				}
			} else {
				throw new RuntimeException("image cache was not initialized," +
						" please call setImageCache() to intialize image cache.");
			}

			if (value != null) {
				// Bitmap found in memory cache
				imageView.setImageDrawable(value);
			} else if (cancelPotentialWork(mData.toString(), imageView)) {
				// BEGIN_INCLUDE(execute_background_task)
				AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mPlaceHolder, mBitmapHunter);
				imageView.setImageDrawable(asyncDrawable);
				mBitmapHunter.execute(true);
				// END_INCLUDE(execute_background_task)
			}
		}

		protected ThreadWork<Void, Integer, String, BitmapDrawable> execute(boolean oneByOne) {
			mHunterJob = new ThreadWork<Void, Integer, String, BitmapDrawable>() {

				@Override
				protected BitmapDrawable doInBackground(Void... params) {
					// BEGIN_INCLUDE(load_bitmap_in_background)
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "doInBackground - starting work");
					}

					final String dataString = String.valueOf(mData);
					Bitmap bitmap = null;
					BitmapDrawable drawable = null;

					// Wait here if work is paused and the task is not cancelled
					synchronized (mPauseWorkLock) {
						while (mTaskWorkPaused && !isCancelled()) {
							try {
								mPauseWorkLock.wait();
							} catch (InterruptedException e) {
							}
						}
					}

					// If the image cache is available and this task has not been
					// cancelled by another thread and the ImageView that was originally bound to this task
					// is still bound back to this task and our "exit early" flag is not set then try and
					// fetch the bitmap from the cache
					if (mImageCache != null && !isCancelled() && getAttachedImageView() != null && !mTaskWorkPaused) {
						bitmap = mImageCache.getBitmapFromDiskCache(dataString);
					}

					// If the bitmap was not found in the cache and this task has not
					// been cancelled by another thread and the ImageView that was originally bound to
					// this task is still bound back to this task and our "exit early" flag is not set,
					// then call the main process method (as implemented by a subclass)
					if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !mTaskWorkPaused) {

						try {
							bitmap = processBitmap(mData, mHeitht, mWidth, mProgressListener);
						} catch (IOException e) {
							// onError() will be called then, and onSucces will not be called.
							publishError(e.getMessage());
							return drawable;
						}
					}

					// If the bitmap was processed and the image cache is available,
					// then add the processed bitmap to the cache for future use. Note we don't check if the
					// task was cancelled here, if it was, and the thread is still running, we may as well
					// add the processed bitmap to our cache as it might be used again in the future
					if (bitmap != null) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							// Running on Honeycomb or newer, so wrap in a standard BitmapDrawable 
							drawable = new BitmapDrawable(mResources, bitmap);
						} else {
							// Running on Gingerbread or older, so wrap in a
							// RecyclingBitmapDrawable
							// which will recycle automatically
							drawable = new RecyclingBitmapDrawable(mResources, bitmap);
						}

						if (mImageCache != null) {
							mImageCache.addBitmapToCache(dataString, drawable);
						}
					}

					if (BuildConfig.DEBUG) {
						Log.d(TAG, "doInBackground - finished work");
					}

					return drawable;
					// END_INCLUDE(load_bitmap_in_background)
				}

				@Override
				protected void onProgressUpdate(Integer value) {
					if (mProgressListener != null) {
						mProgressListener.onProgress(value, mData.toString());
					}
				}

				@Override
				protected void onError(String error) {
					// reset caches for try again next time
					closeCacheInBackground();
					initDiskCache();
					
					if (mErrorImage != null) {
						final ImageView imageView = mImageViewReference.get();
						if(imageView != null){
							imageView.setImageDrawable(new BitmapDrawable(mResources, mErrorImage));
						}
					}
					
					// notice caller that error occured in UI thread
					if (mProgressListener != null) {
						mProgressListener.onError(error, mData.toString());
					}
				}

				/**
				 * Once the image is pull down successfully, associates it to the imageView.
				 */
				@Override
				protected void onSuccess(BitmapDrawable result) {
					// BEGIN_INCLUDE(complete_background_work)
					// if cancel was called on this task or the "exit early" flag is set
					// then we're done
					if (isCancelled() || mTaskWorkPaused) {
						result = null;
					}

					final ImageView imageView = getAttachedImageView();
					if (imageView != null) {
						if (result != null) {
							if (BuildConfig.DEBUG) {
								Log.d(TAG, "onPostExecute - setting bitmap");
							}
							setImageDrawable(imageView, result);
						} else {
							imageView.setImageDrawable(new BitmapDrawable(mResources, mPlaceHolder));
						}
					}

					if (mProgressListener != null) {
						mProgressListener.onCompleted(mData.toString());
					}
					// END_INCLUDE(complete_background_work)
				}

				@Override
				protected void onCancelled(BitmapDrawable value) {
					synchronized (mPauseWorkLock) {
						mPauseWorkLock.notifyAll();
					}
				}
			};
			
			if (oneByOne) {
				return mHunterJob.executeSerial();
			} else {
				return mHunterJob.executeParallel();
			}
		}
		
		protected ThreadWork<Void, Integer, String, BitmapDrawable> getImageHunterJob(){
			return mHunterJob;
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = mImageViewReference.get();
			final ImageHunter bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<ImageHunter> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap loadingBitmap, ImageHunter bitmapWorkerTask) {
			super(res, loadingBitmap);
			bitmapWorkerTaskReference = new WeakReference<ImageHunter>(bitmapWorkerTask);
		}

		public ImageHunter getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * Called when the processing is completed and the final drawable should be
	 * set on the ImageView.
	 * 
	 * @param imageView
	 * @param drawable
	 * @return current {@link ImageWorker} object.
	 */
	private ImageWorker setImageDrawable(ImageView imageView, Drawable drawable) {
		if (mFadeInBitmap) {
			// Transition drawable with a transparent drawable and the final
			// drawable
			final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent), drawable });

			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageDrawable(drawable);
		}
		return this;
	}

	/**
	 * Pause any ongoing background work. This can be used as a temporary
	 * measure to improve performance. For example background work could be
	 * paused when a ListView or GridView is being scrolled using a
	 * {@link android.widget.AbsListView.OnScrollListener} to keep scrolling
	 * smooth.
	 * <p>
	 * If work is paused, be sure setPauseWork(false) is called again before
	 * your fragment or activity is destroyed (for example during
	 * {@link android.app.Activity#onPause()}), or there is a risk the
	 * background thread will never finish.
	 * 
	 * @return current {@link ImageWorker} object.
	 */
	public ImageWorker setImageLoadingPaused(boolean pause) {
		synchronized (mPauseWorkLock) {
			mTaskWorkPaused = pause;
			if (!mTaskWorkPaused) {
				mPauseWorkLock.notifyAll();
			}
		}

		// flush disk cache before Activity's lifecycle was paused,
		// so that even Activity was destroyed then the disk cache was saved
		// already.
		if (pause) {
			flushCacheInBackground();
		}
		return this;
	}

	public ImageCache.ImageCacheParams getImageCacheParams() {
		return mImageCache.getCacheParams();
	}

	protected class CacheAsyncTask extends ThreadWork<Object, Void, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			return null;
		}

		public ThreadWork<Object, Void, Void, Void> execute(Object... params) {
			return executeSerial(params);
		}
	}

	protected void initDiskCacheInternal() {
		if (mImageCache != null) {
			mImageCache.initDiskCache();
		}
	}

	protected void clearCacheInternal() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
	}

	protected void flushCacheInternal() {
		if (mImageCache != null) {
			mImageCache.flush();
		}
	}

	protected void closeCacheInternal() {
		if (mImageCache != null) {
			mImageCache.close();
		}
	}

	protected void clearCacheInBackground() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	protected void flushCacheInBackground() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	/**
	 * Close cache (mainly  for disk cache) while application was destroyed. 
	 */
	protected void closeCacheInBackground() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}
	
	protected void initDiskCache(){
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}
}
