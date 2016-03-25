package com.common.library.logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.common.library.R;
import com.common.library.utils.UiUtils;

public class FloatLogViewLauncher extends Activity {
	private Thread thread;
	private LogPopupWindow popup;
	private TextView labelView;
	private FloatLogView mFloatLogView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_test_activity);

		final Button btnOpenPopup = (Button) findViewById(R.id.btn_open_logcat_dialog);
		labelView = (TextView) findViewById(R.id.label);
		
		btnOpenPopup.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FloatLogView window = new FloatLogView(FloatLogViewLauncher.this);
				window.popupLogView();
			}
		});

		thread = new Thread() {
			public void run() {
				while (true) {
					Log.e("MainActivity", "Hello my smart girl, come on baby...###@!QPIP$@");
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		thread.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		thread.interrupt();
	}
}
