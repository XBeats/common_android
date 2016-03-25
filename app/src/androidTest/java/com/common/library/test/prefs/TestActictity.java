package com.common.library.test.prefs;

import android.app.Activity;
import android.os.Bundle;

public class TestActictity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SystemPrefs.setLogin(this, true);
		boolean isLogin = SystemPrefs.isLogin(this);
	}
}
