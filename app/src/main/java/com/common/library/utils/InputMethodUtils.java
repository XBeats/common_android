package com.common.library.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputMethodUtils {

	/**
	 * Show input method when specified view can be focused.
	 * 
	 * @param context
	 *            application context
	 * @param view
	 *            android view widget
	 */
	public static void showSoftKeyboard(Context context, View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}
}
