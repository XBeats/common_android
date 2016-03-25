package com.common.library.utils;

import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.StrictMode;
import android.os.Build.VERSION_CODES;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class AppUtils {
	public static boolean isApkInstalled(Context context, String packageName) {
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return info != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	
	public static boolean isEmulator(Context context) {
		try {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = tm.getDeviceId();
			if (imei != null && imei.equals("000000000000000")) {
				return true;
			}
			return (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
		} catch (Exception ioe) {
		}
		return false;
	}
	
	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static String getDeviceVersion(Context context){
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceSoftwareVersion();
	}
	
	public static boolean isLocalCN(Context context){
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		return language.equals("zh");
	}
	
	public static int[] getDeviceMetrics(Activity activity){
		DisplayMetrics  dm = new DisplayMetrics();  
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;  
		int screenHeight = dm.heightPixels;  
		return new int[]{screenWidth, screenHeight};
	}
	
	@TargetApi(VERSION_CODES.HONEYCOMB)
    public static void enableStrictMode() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
}
