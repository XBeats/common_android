package com.common.library.test.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.widget.ImageView;

import com.common.library.R;
import com.common.library.images.ImageCache.ImageCacheParams;
import com.common.library.images.Images;
import com.common.library.io.OnProgressListener;

public class ImageDownloadTest extends AndroidTestCase{
	private static final String IMAGE_SAVE_FOLDER = "images";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// init with parameters and folder
		ImageCacheParams params = new ImageCacheParams(mContext, IMAGE_SAVE_FOLDER);
		params.setDiskCacheSize(20 * 1024); // 20M
		params.setMemCacheSize(500); // 500K
		Images.initialize(mContext, params);
		
		// init with default parameters
		//Images.initialize(mContext, IMAGE_SAVE_FOLDER);
	}
	
	public void testDownload(){
		ImageView imageView = new ImageView(mContext);
		
		// set global default loading image
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
		
		String IMAGE_URL = "https://www.baidu.com/img/muqin270x129_71d723b6eaff2a38855f61af4dd11d43.png";
		Images.build(mContext).load(IMAGE_URL).placeHolder(bitmap).errorImage(bitmap)
			.listener(new OnProgressListener() {
			
				@Override
				public void onProgress(int percentage, String tag) {
					// handle process in UI thread
				}
				
				@Override
				public void onError(String errorMsg, String tag) {
					// handle error in UI thread
				}
				
				@Override
				public void onCompleted(String tag) {
					// handle success in UI thread
				}
			}).into(imageView);
	}
	
	public void testManagerCaches(){
		Images.clearCache(mContext);
		Images.closeCache(mContext);
		Images.flushCache(mContext);
	}
}
