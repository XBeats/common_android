package com.common.library.prefs;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.text.TextUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SharedPrefefs {
	private Context mContext;
	private String mPrefsFileName;
	private final Map<String, String> prefsCaches = new HashMap<String, String>();
	private final Map<String, String> toAddOrUpdate = new HashMap<String, String>();
	private final Map<String, String> toRemove = new HashMap<String, String>();
	
	private static SharedPrefefs singleton = null;
	private static Object lock = new Object();
	
	/**
	 * Entrance for retrieve instance of PrefsUnity
	 * @param context
	 * @param prefsFileName
	 * @return Object of PrefsUnity
	 */
	public static SharedPrefefs getPrefs(Context context, String prefsFileName) {
		synchronized (lock) {
			if (singleton == null) {
				singleton = new SharedPrefefs(context, prefsFileName);
			}
		}
		return singleton;
	}
	
	private SharedPrefefs(Context context, String prefsFileName){
		mContext = context;
		mPrefsFileName = prefsFileName;
		prefsCaches.putAll(getAll());
	}
	
	private SharedPreferences getSharedPreference(){
		return mContext.getSharedPreferences(mPrefsFileName, 
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Context.MODE_MULTI_PROCESS : 0);
	}

	private Editor getEditor(){
		return getSharedPreference().edit();
	}
	
	/**
	 * Encode single string.
	 * @param clearValue not encoded string.
	 * @return encoded string.
	 */
	private String encode(String clearValue){
		return clearValue;
	}
	
	/**
	 * Decode single string.
	 * @param encodedValue encoded string.
	 * @return decoded string.
	 */
	private String decode(String encodedValue){
		return encodedValue;
	}
	
	/**
	 * Decode all values retrieved from shared preferences.
	 * @param encodedValues encoded values for decode.
	 * @return decoded values.
	 */
	private Map<String, String> decodeAll(Map<String, String> encodedValues){
		return encodedValues;
	}
	
	/**
	 * Read String type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @param defVal Value to return if this preference does not exist.
	 * @param encrypt whether encrypt or no.
	 * @return String Returns the preference value if it exists or return null.
	 */
	public String getString(String key, String defVal){
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, defVal);
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return prefsCaches.get(key);
	}
	
	/**
	 * Read String type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return String Returns the preference value if it exists or return null.
	 */
	public String getString(String key) {
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, "");
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return prefsCaches.get(key);
	}
	
	/**
	 * Read Integer type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @param defVal Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue(0) .
	 */
	public Integer getInt(String key, Integer defVal){
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, defVal.toString());
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Integer.valueOf(prefsCaches.get(key));
	}
	
	/**
	 * Read Integer type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return Returns the preference value if it exists, or defValue(0) .
	 */
	public Integer getInt(String key) {
		if(TextUtils.isEmpty(key)){
			String value = getSharedPreference().getString(key, "0");
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Integer.valueOf(prefsCaches.get(key));
	}
	
	/**
	 * Read Float type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @param defVal Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue(0f) .
	 */
	public Float getFloat(String key, Float defVal){
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, defVal.toString());
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Float.valueOf(prefsCaches.get(key));
	}
	
	/**
	 * Read Float type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return Returns the preference value if it exists, or defValue(0f) .
	 */
	public Float getFloat(String key) {
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, "0f");
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Float.valueOf(prefsCaches.get(key));
	}

	/**
	 * Read Long type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return Returns the preference value if it exists, or defValue(0l) .
	 */
	public Long getLong(String key, Long defVal){
		if(TextUtils.isEmpty(prefsCaches.get(key))){
			String value = getSharedPreference().getString(key, defVal.toString());
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Long.valueOf(prefsCaches.get(key));
	}
	
	/**
	 * Read Long type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return Returns the preference value if it exists, or defValue(0l) .
	 */
	public Long getLong(String key) {
		if(TextUtils.isEmpty(prefsCaches.get(key))) {
			String value = getSharedPreference().getString(key, "0l");
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Long.valueOf(prefsCaches.get(key));
	}

	/**
	 * Read Boolean type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @param Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue(false) .
	 */
	public Boolean getBoolean(String key, Boolean defVal){
		if (TextUtils.isEmpty(prefsCaches.get(key))) {
			String value = getSharedPreference().getString(key, defVal.toString());
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Boolean.valueOf(prefsCaches.get(key));
	}

	/**
	 * Read Boolean type value from shared preference with its key.
	 * @param key The name of the preference to retrieve.
	 * @return Returns the preference value if it exists, or defValue(false).
	 * @return class object itself.
	 */
	public Boolean getBoolean(String key) {
		if (TextUtils.isEmpty(prefsCaches.get(key))) {
			String value = getSharedPreference().getString(key, "false");
			value = decode(value);
			prefsCaches.put(key, value);
		}
		return Boolean.valueOf(prefsCaches.get(key));
	}
	
	/**
	 * Set a boolean value in the preferences editor, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to put.
	 * @param value Value to save.
	 * @return class object itself.
	 */
	public SharedPrefefs putString(String key, String value) {
		toAddOrUpdate.put(key, value);
		return this;
	}
	
	/**
	 * Set a Long type value in the preferences editor, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to put.
	 * @param value Value to save.
	 * @return class object itself.
	 */
	public SharedPrefefs putLong(String key, Long value) {
		toAddOrUpdate.put(key, value.toString());
		return this;
	}
	
	/**
	 * Set a Integer type value in the preferences editor, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to put.
	 * @param value Value to save.
	 * @return class object itself.
	 */
	public SharedPrefefs putInt(String key, Integer value) {
		toAddOrUpdate.put(key, value.toString());
		return this;
	}

	/**
	 * Set a Float type value in the preferences editor, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to put.
	 * @param value Value to save.
	 * @return class object itself.
	 */
	public SharedPrefefs putFloat(String key, Float value) {
		toAddOrUpdate.put(key, value.toString());
		return this;
	}
	
	/**
	 * Set a Boolean type value in the preferences editor, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to put.
	 * @param value Value to save.
	 * @return class object itself.
	 */
	public SharedPrefefs putBoolean(String key, Boolean value) {
		toAddOrUpdate.put(key, value.toString());
		return this;
	}
	
	/**
	 * Remove one value saved in shared preference with its key, to be written back
	 * once {@link #commit} are called.
	 * @param key The name of the preference to remove.
	 * @return class object itself.
	 */
	public SharedPrefefs remove(String key) {
		toRemove.remove(key);
		return this;
	}

	/**
	 * Remove all value saved in shared preference, to be written back
	 * once {@link #commit} are called.
	 * @return class object itself
	 */
	public SharedPrefefs removeAll() {
		toRemove.putAll(prefsCaches);
		return this;
	}
	
	/**
	 * Get all key-value saved in shared preference.
	 * @return all value saved in shared preferences
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> getAll(){
		Map<String, String> values = (Map<String, String>) getSharedPreference().getAll();
		return decodeAll(values);
	}

	/**
	 * Commit shared preference from caches.
	 */
	public void commit(){
		boolean needCommit = toAddOrUpdate.size() > 0 || toRemove.size() > 0;
		if(needCommit){
			// save cached actions into android shared preferences
			Editor editor = getEditor();
			for(String key : toAddOrUpdate.keySet()){
				String value = toAddOrUpdate.get(key);
				value = encode(value);
				editor.putString(key, value);
			}
			
			for(String key : toRemove.keySet()){
				editor.remove(key);
			}
			boolean commited = editor.commit();
			
			// refresh cached preferences values
			if(commited){
				for(String key : toAddOrUpdate.keySet()){
					prefsCaches.put(key, toAddOrUpdate.get(key));
				}
				
				for(String key : toRemove.keySet()){
					prefsCaches.remove(key);
				}
				
				toAddOrUpdate.clear();
				toRemove.clear();
			}
		}
	}
}
