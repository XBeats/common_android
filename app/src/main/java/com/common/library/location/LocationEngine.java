package com.common.library.location;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

/**
 * A simple location tool, location tool Usage:</br>
 * 
 * 1. Retrieve exist location cache: {@link #getCachedLocation()};</br>
 * 
 * 2. Clear exist location cache: {@link #clearCache()};</br>
 * 
 * 3. Try use exist location cache, if exist and not expired:
 * {@link #startLocation(boolean)};</br>
 * 
 * 4. Start location with detail option parameters, e.g. location timeout,
 * network sensitive, and other client option parameters:
 * {@link #create(Context, LocationParams)}.
 * 
 * @author zf08526
 * 
 */
public class LocationEngine implements BDLocationListener{
	private Context mContext;
	private LocationClient mLocationClient;
	private LocationParams mLocationParams;
	
	/**
	 * Key: listener object, Value: auto recycle after location complete or error.
	 */
	private ConcurrentHashMap<LocationListener, Boolean> mListeners;
	private LocationInfo mLocationInfo;
	private String mChosenCity;
	private CountDownTimer mTimeoutSchedule;
	
	private static LocationEngine singleton;
	
	private LocationEngine(Context context){
		this(context, LocationParams.getDefault());
	}
	
	private LocationEngine(Context context, LocationParams locationParams){
		mContext = context.getApplicationContext();
		mLocationParams = locationParams;
		mLocationClient = new LocationClient(mContext);
		mLocationClient.registerLocationListener(this);
		mLocationClient.setLocOption(mLocationParams.getClientOption());
		mListeners = new ConcurrentHashMap<LocationListener, Boolean>();
	}
	
	public static LocationEngine create(Context context){
		return create(context, LocationParams.getDefault());
	}
	
	public static LocationEngine create(Context context, LocationParams locationParams){
		if (singleton == null){
			singleton = new LocationEngine(context, locationParams);
		} else if (locationParams != null){
			singleton.mLocationParams = locationParams;
			singleton.mLocationClient.setLocOption(locationParams.getClientOption());
		}
		return singleton;
	}
	
	/**
	 * Retrieve cached location information before.
	 * 
	 * @return exist location cache.
	 * @throws LocationCacheExpiredException
	 */
	public static LocationInfo getCachedLocation() throws LocationCacheExpiredException{
		if(singleton != null && singleton.mLocationInfo != null){
			try {
				LocationInfo info = (LocationInfo)singleton.mLocationInfo.clone();
				info.setCachedInfo(true);
				return info;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				// should not be happen
			}
		}
		throw new LocationCacheExpiredException();
	}
	
	public static void setChosenCity(Context context, String cityName){
		if (singleton == null){
			singleton = new LocationEngine(context, null);
		}
		
		singleton.mChosenCity = cityName;
	}
	
	public static String getChosenCity(Context context){
		if (singleton == null){
			singleton = new LocationEngine(context, null);
		}
		
		return singleton.mChosenCity;
	}
	
	/**
	 * Clear cached location information.
	 * 
	 * @return return true if have cache and cleared successfully, otherwise
	 *         return false.
	 */
	public static boolean clearCache(){
		if(singleton != null && singleton.mLocationInfo != null){
			singleton.mLocationInfo = null;
			return true;
		}
		return false;
	}
	
	/**
	 * Request to locate, location information will be responsed asynchronously
	 * via {@link LocationListener}.
	 */
	public void startLocation(){
		// since startLocation() without parameter 'tryUseCache', so cache will
		// not be used even though it exists. in other words, if network is not
		// available, error should be responsed.
		if(!isNetworkAvailable(mContext)){
			notifyError(LocationListener.NETWORK_ERROR, "network is not available.");
			cancelLocation();
			return;
		}
		
		if(mLocationClient.isStarted()){
			// clear cache and start location
			mLocationInfo = null;
			mLocationClient.start();
			mLocationClient.requestLocation();

			// schedule timeout
			mTimeoutSchedule = new CountDownTimer(mLocationParams.getTimeout(), mLocationParams.getTimeout()) {

				@Override
				public void onTick(long millisUntilFinished) {}

				@Override
				public void onFinish() {
					mTimeoutSchedule = null;
					notifyError(LocationListener.TIMEOUT_ERROR, "locate timeout.");
					cancelLocation();
				}
			}.start();
		}
	}
	
	/**
	 * Like {@link #startLocation()} but will use location cache if exist and not expired,
	 * otherwise new location action will be started. If a previous location has
	 * been completed and is still valid the result may be returned via the
	 * listener immediately.
	 * 
	 * @param tryUseCache
	 */
	public void startLocation(boolean tryUseCache){
		if(!isNetworkAvailable(mContext) && mLocationParams.isNetworkSensitive()){
			notifyError(LocationListener.NETWORK_ERROR, "network is not available.");
			cancelLocation();
			return;
		}
		
		if(tryUseCache){
			boolean useable = isCacheUsable(mLocationInfo);
			if(useable){
				// use location cache
				try {
					LocationInfo info = (LocationInfo) mLocationInfo.clone();
					info.setCachedInfo(true);
					notifySuccess(info);
					cancelLocation();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					// should not be happen
				}
			}else{
				startLocation();
			}
		}else{
			startLocation();
		}
	}
	
	/**
	 * Request to cancel locate.
	 */
	public void cancelLocation(){
		if(mLocationClient.isStarted()){
			mLocationClient.stop();
		}
		removeTimeoutSchedule();
	}
	
	/**
	 * Remove timeout schedule which means location info will update repeatedly
	 * later.
	 */
	private void removeTimeoutSchedule(){
		if(mTimeoutSchedule != null){
			mTimeoutSchedule.cancel();
		}
	}
	
	private boolean isCacheUsable(LocationInfo info){
		if(info != null){
			long lastModifyDt = mLocationInfo.getLastModifyDt();
			long nowDt = System.currentTimeMillis();
			long diffDt = (nowDt - lastModifyDt);
			return diffDt < mLocationParams.getCacheExpiration();
		}
		return false;
	}
	
	@Override
	public void onReceivePoi(BDLocation paramBDLocation) {}
	
	@Override
	public void onReceiveLocation(BDLocation location) {
		// 定为半径小于1m
		if (location == null || location.getRadius() < 1.0) {
			notifyError(LocationListener.UNKNOWN_ERRPR, "unknown error");
			return;
		}
		LocationInfo info = new LocationInfo();
		info.setLatitude(location.getLatitude());
		info.setLongitude(location.getLongitude());
		info.setCity(location.getCity());
		info.setAddrStr(location.getAddrStr());
		info.setDistrict(location.getDistrict());
		info.setStreet(location.getStreet());
		info.setLastModifyDt(System.currentTimeMillis());
		
		// replace exist location info
		mLocationInfo = info;
		mLocationInfo.setCachedInfo(false);
		notifySuccess(mLocationInfo);
		
		// stop location or remove timeout schedule
		if(mLocationParams.isStopLocationAutomaticly()){
			cancelLocation();
		}else{
			removeTimeoutSchedule();
		}
	}
	
	private void notifyError(int errorType, String errorDesc){
		Enumeration<LocationListener> listeners = mListeners.keys();
		while(listeners.hasMoreElements()){
			LocationListener listener = listeners.nextElement();
			if(listener != null){
				listener.onError(errorType, errorDesc);
				
				// remove one-off listener
				boolean removeable = mListeners.get(listener);
				if(removeable){
					mListeners.remove(listener);
				}
			}
		}
	}
	
	private void notifySuccess(LocationInfo locationInfo){
		Enumeration<LocationListener> listeners = mListeners.keys();
		while(listeners.hasMoreElements()){
			LocationListener listener = listeners.nextElement();
			if(listener != null){
				listener.onSuccess(locationInfo);
				
				// remove one-off listener
				boolean removeable = mListeners.get(listener);
				if(removeable){
					mListeners.remove(listener);
				}
			}
		}
	}
	
	/**
	 * Save location listener and also can set whether the listener can be
	 * recycled after location complete or error.<br>
	 * If the listener was set not auto recyclable, it also can be recycled
	 * manually by call {@link LocationEngine#unregisterListener(LocationListener)}
	 * 
	 * @param listener
	 * @param autoRecycle
	 *            if true, listener will be recycled later, otherwise the
	 *            listener will called whatever new location complete or error.
	 * @return LocationEngine its self
	 */
	public LocationEngine registerListener(LocationListener listener, boolean autoRecycle){
		if (singleton != null && listener != null){
			singleton.mListeners.put(listener, autoRecycle);
		} else {
			// should not be happen
		}
		return this;
	}
	
	/**
	 * Be similar with
	 * {@link LocationEngine#registerListener(LocationListener, boolean)}, but
	 * listener will be recycled as default.
	 * 
	 * @param listener
	 * @return LocationEngine its self
	 */
	public LocationEngine registerListener(LocationListener listener){
		registerListener(listener, true);
		return this;
	}
	
	/**
	 * This is only useful for listener which can not auto recycled since auto
	 * recyclable listeners can unregister itself after location complete or error automatically.
	 * 
	 * @param listener
	 * @return LocationEngine its self
	 */
	public LocationEngine unregisterListener(LocationListener listener){
		if (singleton != null && listener != null){
			singleton.mListeners.remove(listener);
		} else {
			// should not be happen
		}
		return this;
	}
	
	private boolean isNetworkAvailable(Context context){
		if (context != null) {
			ConnectivityManager mConnectivityManager = 
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isConnected();
			}
		}
		return false;
	}
}
