package com.common.library.test.prefs;

import com.common.library.prefs.SharedPrefefs;

import android.content.Context;

public class SystemPrefs {
	private static String PREFS_FILE_NAME = "com.tongcheng.android.xml";
	
	// preference keys
	private static final String KEY_IS_LOGIN = "is_login";
	private static final String KEY_MEMBER_ID = "member_id";

	public static SharedPrefefs getPrefs(Context context) {
		return SharedPrefefs.getPrefs(context, PREFS_FILE_NAME);
	}
	
	// preference API for login
	public static boolean isLogin(Context context){
		return getPrefs(context).getBoolean(KEY_IS_LOGIN, false);
	}
	
	public static void setLogin(Context context, boolean isLogin){
		getPrefs(context).putBoolean(KEY_IS_LOGIN, isLogin).commit();
	}
	
	// preference API for memberId
	public static String getMemberId(Context context){
		return getPrefs(context).getString(KEY_MEMBER_ID);
	}
	
	public static void setMemberId(Context context, String memberId){
		getPrefs(context).putString(KEY_MEMBER_ID, memberId).commit();;
	}
	
	public static void removeMemberId(Context context, String key){
		getPrefs(context).remove(key).commit();
	}
}