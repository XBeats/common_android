package com.common.library.ui.indexlistview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.common.library.R;
import com.common.library.orm.async.SQLiteCursorLoader;
import com.common.library.ui.indexlistview.IndexBar.OnTouchingLetterChangedListener;
import com.common.library.ui.indexlistview.IndexListViewAdapter.OnLetterItemClickedListener;
import com.google.gson.Gson;


/**
 * Base activity with index slide bar.
 *
 * @author zf08526
 */
public abstract class IndexListViewActivity extends FragmentActivity {
    // loader callback ID
    private static final int LOADER_ID_LIST = 0;
    private static final int LOADER_ID_QUERY = 1;

    // used for dismiss overlay in delay
    private Handler mHandler = new Handler();

    // list view
    private ListView mListView;
    private IndexBar mIndexBarView;
    private TextView mLetterView;
    private View mOverlay;
    private IndexListViewAdapter mListViewAdapter;

    // query pop up window
    private EditText mQueryView;
    private ListPopupWindow mQueryResultPopupWindow;
    private SimpleCursorAdapter mQueryResultAdapter;

    private Arguments mArguments;
    private List<String> mPrefix = new ArrayList<String>();

    // prevent list view and overlay from initializing repeatedly
    private boolean mInitialized = false;

    private LoaderManager.LoaderCallbacks<Cursor> mListLoadCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] fakeTable = new String[] {
                    IndexPrefix._ID,
                    IndexPrefix.FIELD_PREFIX_TYPE,
                    IndexPrefix.FIELD_IS_TITLE_ITEM,
                    IndexPrefix.FIELD_EXTRA_VALUE
            };

            int index = 0;
            MatrixCursor fakeCursor = new MatrixCursor(fakeTable);

            if (mArguments.getCurrentCity() != null && mArguments.getCurrentCity().size() > 0) {
                // add current city title item
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE - index++,
                        IndexPrefix.PREFIX_TYPE_CURRENT_CITY,
                        1,
                        ""});

                // add current city value item
                String currentCity = new Gson().toJson(mArguments.getCurrentCity());
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE -index++,
                        IndexPrefix.PREFIX_TYPE_CURRENT_CITY,
                        0,
                        currentCity});

                mPrefix.add(IndexPrefix.PREFIX_TYPE_CURRENT_CITY);
            }

            if (mArguments.getHistoryCity() != null && mArguments.getHistoryCity().size() > 0) {
                // add history city title item
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE - index++,
                        IndexPrefix.PREFIX_TYPE_HISTORY_CITY,
                        1,
                        ""});

                // add history city value item
                String historyCity = new Gson().toJson(mArguments.getHistoryCity());
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE - index++,
                        IndexPrefix.PREFIX_TYPE_HISTORY_CITY,
                        0,
                        historyCity});

                mPrefix.add(IndexPrefix.PREFIX_TYPE_HISTORY_CITY);
            }

            if (mArguments.getHotCity() != null && mArguments.getHotCity().size() > 0) {
                // add hot city title item
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE - index++,
                        IndexPrefix.PREFIX_TYPE_HOT_CITY,
                        1,
                        ""});

                // add hot city value item
                String hotCity = new Gson().toJson(mArguments.getHotCity());
                fakeCursor.addRow(new Object[] {
                        Integer.MAX_VALUE - index++,
                        IndexPrefix.PREFIX_TYPE_HOT_CITY,
                        0,
                        hotCity});

                mPrefix.add(IndexPrefix.PREFIX_TYPE_HOT_CITY);
            }

            Cursor cursor = mArguments.getDbUtils().findToCursor(
                    mArguments.getTableClass(),
                    null, null, null, null,
                    mArguments.getPinyinColumnName() + " ASC");

            cursor = new MergeCursor(new Cursor[] { fakeCursor, cursor});
            return new SQLiteCursorLoader(IndexListViewActivity.this, cursor);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mListViewAdapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mListViewAdapter.changeCursor(null);
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> mQueryLoadCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String queryContent = mQueryView.getText().toString().trim();
            String whereArgs = "%" + queryContent + "%";
            Cursor cursor = mArguments.getDbUtils().findToCursor(
                    mArguments.getTableClass(),
                    mArguments.getPinyinColumnName() + " LIKE ? OR "
                            + mArguments.getDataColumnName() + " LIKE ? OR "
                            + mArguments.getPyColumnName() + " LIKE ?",
                    new String[] { whereArgs, whereArgs, whereArgs }, null, null,
                    mArguments.getPinyinColumnName() + " ASC");

            return new SQLiteCursorLoader(IndexListViewActivity.this, cursor);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mQueryResultAdapter.changeCursor(data);

            // control visibility of pop up window
            if (data.getCount() > 0) {
                mQueryResultPopupWindow.show();
            } else {
                mQueryResultPopupWindow.dismiss();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mQueryResultAdapter.changeCursor(null);
        }
    };

    private TextWatcher mQueryContentWathcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                if (mQueryResultPopupWindow.isShowing()) {
                    mQueryResultPopupWindow.dismiss();
                }
            } else {
                Loader<Cursor> loader = getSupportLoaderManager().getLoader(LOADER_ID_QUERY);
                if (loader != null) {
                    getSupportLoaderManager().restartLoader(LOADER_ID_QUERY, null,
                            mQueryLoadCallbacks);
                } else {
                    getSupportLoaderManager()
                            .initLoader(LOADER_ID_QUERY, null, mQueryLoadCallbacks);
                }
                if (!mQueryResultPopupWindow.isShowing()) {
                    mQueryResultPopupWindow.show();
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private Runnable mDimissOverlayRunnable = new Runnable() {

        @Override
        public void run() {
            mOverlay.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_listview);
        mListView = (ListView) findViewById(R.id.listview);
        mQueryView = (EditText) findViewById(R.id.et_query);
        mQueryView.addTextChangedListener(mQueryContentWathcher);
        mIndexBarView = (IndexBar) findViewById(R.id.index_bar);
        mIndexBarView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String letter) {
                Map<String, Integer> map = mIndexBarView.getLetterPositonMap();
                if (map.get(letter) != null) {
                    mListView.setSelection(map.get(letter));
                    mLetterView.setText(letter);
                    mOverlay.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLetterPressedStateChanged(boolean pressed) {
                if (pressed) {
                    mHandler.removeCallbacks(mDimissOverlayRunnable);
                } else {
                    mHandler.removeCallbacks(mDimissOverlayRunnable);
                    mHandler.postDelayed(mDimissOverlayRunnable, 1000);
                }
            }
        });

        initOverlay();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInitialized) {
            Arguments args = onLoadArguments();
            checkArguments(args);
            mArguments = args;

            initData();
            initQueryResultPopupWindow();
            mInitialized = true;
        }
    }

    private void checkArguments(Arguments args) {
        if (args == null) {
            throw new RuntimeException("Arguments was not initialized.");
        }

        if (args.getDbUtils() == null) {
            throw new RuntimeException("Arguments initialized error since DbUtils was null.");
        }

        if (args.getTableClass() == null) {
            throw new RuntimeException("Arguments initialized error since TableClass was null.");
        }

        if (TextUtils.isEmpty(args.getPinyinColumnName())) {
            throw new RuntimeException(
                    "Argument initialized error since PinyinColumnName was empty.");
        }

        if (TextUtils.isEmpty(args.getPyColumnName())) {
            throw new RuntimeException("Argument initialized error since PYColumnName was empty.");
        }

    }

    @Override
    public void onBackPressed() {
        if (dismissPopupWindow()) {
            super.onBackPressed();
        }
    }

    private boolean dismissPopupWindow() {
        if (mQueryResultPopupWindow != null && mQueryResultPopupWindow.isShowing()) {
            mQueryResultPopupWindow.dismiss();
            return false;
        } else {
            return true;
        }
    }
    
   @Override
protected void onPause() {
	super.onPause();
	if (mOverlay != null){
		 WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
	     windowManager.removeView(mOverlay);
	}
}

    /**
     * Once data item in list view or pop up window was chosen, it will be
     * called.
     *
     * @param value
     *            chosen data of item.
     */
    protected abstract void onLetterItemChosen(String value);

    /**
     * Override to support arguments used to query database.
     *
     * @return
     */
    protected abstract Arguments onLoadArguments();

    private void initData() {
        mListViewAdapter = new IndexListViewAdapter(this, mArguments.getPinyinColumnName(), mArguments.getDataColumnName());
        mListView.setAdapter(mListViewAdapter);
        mListViewAdapter.setOnLetterItemClickedListener(new OnLetterItemClickedListener() {
            @Override
            public void onClicked(String value) {
                onLetterItemChosen(value);
            }
        });
        getSupportLoaderManager().initLoader(LOADER_ID_LIST, null, mListLoadCallbacks);

        // load side bar view
        String[] prefixArray = mPrefix.toArray(new String[0]);
        mIndexBarView.loadIndexLetters(
                mArguments.getDbUtils(),
                mArguments.getTableClass(),
                mArguments.getPinyinColumnName(),
                prefixArray, null);
    }

    @SuppressLint("InflateParams")
    private void initOverlay() {
        LinearLayout overlay = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.index_letter_overlay, null);
        TextView letterView = (TextView) overlay.findViewById(R.id.tv_letter);

        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, params);

        mLetterView = letterView;
        mOverlay = overlay;
    }

    private void initQueryResultPopupWindow() {
        mQueryResultAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null, new String[] { mArguments.getDataColumnName() },
                new int[] { android.R.id.text1 }, 0);
        mQueryResultPopupWindow = new ListPopupWindow(this);
        mQueryResultPopupWindow.setAdapter(mQueryResultAdapter);
        mQueryResultPopupWindow.setAnchorView(mQueryView);
        mQueryResultPopupWindow.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView dataView = (TextView) view.findViewById(android.R.id.text1);
                onLetterItemChosen(dataView.getText().toString());
                mQueryResultPopupWindow.dismiss();
            }
        });
    }

}
