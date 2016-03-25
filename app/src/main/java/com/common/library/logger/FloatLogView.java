package com.common.library.logger;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.common.library.R;
import com.common.library.utils.UiUtils;

public class FloatLogView extends LinearLayout {
	private LogView logView;

	private WindowManager windowManager;
	public WindowManager.LayoutParams params;
	private float x;
	private float y;
	
	private float xOfView;
	private float yOfView;
	
	public FloatLogView(Context context) {
		super(context);
		params = new WindowManager.LayoutParams();
		windowManager = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		setBackgroundColor(getResources().getColor(android.R.color.transparent));
		View rootView = LayoutInflater.from(context).inflate(R.layout.log_popup_window_layout, null);
		initLogcatView(rootView);

		CheckBox stopLogChk = (CheckBox) rootView.findViewById(R.id.chk_stop_log);
		stopLogChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				logView.setCanPrintLog(isChecked);
			}
		});

		ImageView dismissBtn = (ImageView) rootView.findViewById(R.id.img_close);
		dismissBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				windowManager.removeView(FloatLogView.this);
			}
		});

		addView(rootView, new LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.MATCH_PARENT));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = event.getRawX();
		y = event.getRawY() - 25;// 25 is the status bar height
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xOfView = event.getX();
			yOfView = event.getY();		
			break;

		case MotionEvent.ACTION_MOVE:
			updatePosition();
			break;
			
		case MotionEvent.ACTION_UP:
			xOfView = yOfView = 0;
			break;
		}
		return true;
	}
	
	public void initLogcatView(final View rootView) {
		final ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
		logView = new LogView(getContext());
		ViewGroup.LayoutParams logParams = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		logParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		logView.setLayoutParams(logParams);
		logView.setClickable(true);
		logView.setFocusable(true);
		logView.setTypeface(Typeface.MONOSPACE);

		// Want to set padding as 16 dips, setPadding takes pixels. Hooray math!
		int paddingDips = 16;
		double scale = getContext().getResources().getDisplayMetrics().density;
		int paddingPixels = (int) ((paddingDips * (scale)) + 0.5);
		logView.setPadding(paddingPixels, 0, paddingPixels, paddingPixels);
		logView.setCompoundDrawablePadding(paddingPixels);

		logView.setGravity(Gravity.BOTTOM);
		logView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
		Log.setLogNode(logView);

		logView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		scrollView.addView(logView);
	}

	private void updatePosition() {
		params.x = (int) (x - xOfView);
		params.y = (int) (y - yOfView);
		windowManager.updateViewLayout(this, params);
	}

	public void popupLogView(){
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		params.format = PixelFormat.RGBA_8888;  
		params.width = UiUtils.dp2px(getContext(), 250);
		params.height = UiUtils.dp2px(getContext(), 200);

		params.gravity = Gravity.LEFT | Gravity.TOP;
	
		// set (0, 0) as the default start coordinate
		params.x = 0;
		params.y = 0;
		
		windowManager.addView(this, params);
	}
}
