package com.common.library.utils;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * Similar to {@link android.view.View.OnClickListener}, but will ignore
 * multiple click, only first click action will be invoked.
 * 
 * @author zhangfei
 * 
 */
public abstract class IgnoreMultipleClickListener implements OnClickListener {

	/**
	 * The first time of view has been clicked.
	 * 
	 * @param view
	 *            The view that was clicked.
	 */
	public abstract void onViewClicked(View view);

	/**
	 * Your should use {@link #onViewClicked(View)} instead.
	 */
	@Override
	public void onClick(View v) {
		if (!UiUtils.isFastDoubleClick()) {
			onViewClicked(v);
		}
	}
}
