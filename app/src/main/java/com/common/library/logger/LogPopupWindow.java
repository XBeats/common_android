package com.common.library.logger;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import com.common.library.R;
import com.common.library.utils.UiUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LogPopupWindow extends PopupWindow {
	private Context mContext;
	private LogView mLogView;
	
	public LogPopupWindow(Context context) {
        super(context);
        mContext = context;

		View popupView = LayoutInflater.from(mContext).inflate(R.layout.log_popup_window_layout,	null);
		initLogcatView(popupView);
		
		setContentView(popupView);
		setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		setHeight(UiUtils.dp2px(mContext, 250f));
		setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.border_transparent));
		
		CheckBox stopLogChk = (CheckBox) popupView.findViewById(R.id.chk_stop_log);
		stopLogChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mLogView.setCanPrintLog(isChecked);
			}
		});

		ImageView dismissBtn = (ImageView) popupView.findViewById(R.id.img_close);
		dismissBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		popupView.setOnTouchListener(new OnTouchListener() {
			int orgX, orgY;
			int offsetX, offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					orgX = (int) event.getX();
					orgY = (int) event.getY();
					return true;
					
				case MotionEvent.ACTION_MOVE:
					offsetX = (int) event.getRawX() - orgX;
					offsetY = (int) event.getRawY() - orgY;
					update(offsetX, offsetY, -1, -1, true);
					return true;
				}
				return false;
			}
		});
    }
	
	public void initLogcatView(View rootView) {
		final ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        mLogView = new LogView(mContext);
        ViewGroup.LayoutParams logParams = new ViewGroup.LayoutParams(
        		ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        logParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLogView.setLayoutParams(logParams);
        mLogView.setClickable(true);
        mLogView.setFocusable(true);
        mLogView.setTypeface(Typeface.MONOSPACE);

        // Want to set padding as 16 dips, setPadding takes pixels.  Hooray math!
        int paddingDips = 16;
        double scale = mContext.getResources().getDisplayMetrics().density;
        int paddingPixels = (int) ((paddingDips * (scale)) + 0.5);
        mLogView.setPadding(paddingPixels, 0, paddingPixels, paddingPixels);
        mLogView.setCompoundDrawablePadding(paddingPixels);

        mLogView.setGravity(Gravity.BOTTOM);
        mLogView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
        Log.setLogNode(mLogView);
        
        mLogView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
            	scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        scrollView.addView(mLogView);
    }
	
	public void showPopupWindow(View anchorView){
		if(isShowing()){
			dismiss();
		}
		showAsDropDown(anchorView, 50, -30);
	}
	
}
