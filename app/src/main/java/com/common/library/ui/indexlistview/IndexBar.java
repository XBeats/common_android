package com.common.library.ui.indexlistview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.common.library.orm.sqlite.BaseTable;
import com.common.library.orm.sqlite.DbUtils;
import com.common.library.utils.HanziToPinyin;

/**
 * Slide index bar of list view.
 *
 * @author zf08526
 */
public class IndexBar extends View {
    public static final String[] LETTERS = new String[] { "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    private String[] mIndexLetters;
    private OnTouchingLetterChangedListener mLetterChangedListener;
    private Paint mPaint = new Paint();
    private int mChoose = -1;
    private boolean mInitIndexLetters = false;
    private Map<String, Integer> mLetterPositionMap;

    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String letter);

        void onLetterPressedStateChanged(boolean pressed);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mInitIndexLetters) {
            return;
        }

        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / mIndexLetters.length;
        for (int i = 0; i < mIndexLetters.length; i++) {
            mPaint.setColor(Color.BLACK);
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(30);
            if (i == mChoose) {
                mPaint.setColor(Color.parseColor("#3399ff"));
                mPaint.setFakeBoldText(true);
            }
            float xPos = width / 2 - mPaint.measureText(mIndexLetters[i]) / 2;
            float yPos = singleHeight * i + singleHeight / 2;
            canvas.drawText(mIndexLetters[i], xPos, yPos, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mInitIndexLetters) {
            return false;
        }

        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mChoose;
        final int position = (int) (y / getHeight() * mIndexLetters.length);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            setBackgroundColor(Color.parseColor("#40000000"));

            if (mLetterChangedListener != null) {
                mLetterChangedListener.onLetterPressedStateChanged(true);
            }

            if (oldChoose != position && mLetterChangedListener != null) {
                if (position >= 0 && position <= mIndexLetters.length) {
                    mLetterChangedListener.onTouchingLetterChanged(mIndexLetters[position]);
                    mChoose = position;
                    invalidate();
                }
            }

            break;
        case MotionEvent.ACTION_MOVE:
            if (oldChoose != position && mLetterChangedListener != null) {
                if (position >= 0 && position < mIndexLetters.length) {
                    mLetterChangedListener.onTouchingLetterChanged(mIndexLetters[position]);
                    mChoose = position;
                    invalidate();
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            setBackgroundColor(Color.parseColor("#40FFFFFF"));

            if (mLetterChangedListener != null) {
                mLetterChangedListener.onLetterPressedStateChanged(false);
            }
            mChoose = -1;
            invalidate();
            break;
        }
        return true;
    }

    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener listener) {
        mLetterChangedListener = listener;
    }

    public String[] getIndexLetters() {
        return mIndexLetters;
    }

    public <T extends BaseTable> void loadIndexLetters(DbUtils dbUtils,
            Class<T> tableClass, String pinyinColumnName,
            String[] prefixLetters, String[] suffixLetters) {
        InitLetterTask task = new InitLetterTask(dbUtils, tableClass, pinyinColumnName);
        task.setPrefixLetters(prefixLetters);
        task.setSuffixLetters(suffixLetters);
        task.execute();
    }

    public void loadLetterPositionRelation(DbUtils dbUtils,
            Class<? extends BaseTable> tableClass, String pinyinColumnName) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        int length = mIndexLetters.length;
        int position = 0;
        for (int i = 0; i < length; i++) {
            String letter = mIndexLetters[i];
            int count = IndexBar.countLetter(dbUtils, tableClass, pinyinColumnName, letter);
            if (count > 0) {
                map.put(letter, position);
                position += count;
            }
        }
        mLetterPositionMap = map;
    }

    public static <T extends BaseTable> int countLetter(DbUtils dbUtils,
            Class<T> tableClass, String pinyinColumn, String letter) {
        if (TextUtils.isEmpty(letter)) {
            return 0;
        }

        if (HanziToPinyin.isChinese(letter)) {
            return 2;
        } else {
            letter = letter.toUpperCase(Locale.getDefault());
            pinyinColumn = "UPPER(" + pinyinColumn + ")";
            return dbUtils.count(
                    tableClass,
                    pinyinColumn + " LIKE ?",
                    new String[] { letter + "%" });
        }

    }

    public Map<String, Integer> getLetterPositonMap() {
        return mLetterPositionMap;
    }

    private class InitLetterTask extends AsyncTask<Void, Void, String[]> {
        private String[] mPrefixLetters;
        private String[] mSuffixLetters;
        private DbUtils mDbUtils;
        private Class<? extends BaseTable> mTableClass;
        private String mPinyinColumnName;

        public InitLetterTask(DbUtils dbUtils, Class<? extends BaseTable> tableClass,
                String pinyinColumnName) {
            mDbUtils = dbUtils;
            mTableClass = tableClass;
            mPinyinColumnName = pinyinColumnName;
        }

        public void setPrefixLetters(String[] prefixLetters) {
            mPrefixLetters = prefixLetters;
        }

        public void setSuffixLetters(String[] suffixLetters) {
            mSuffixLetters = suffixLetters;
        }

        @Override protected String[] doInBackground(Void... params) {

            List<String> existLetters = new ArrayList<String>();
            if (mPrefixLetters != null && mPrefixLetters.length > 0) {
                existLetters.addAll(Arrays.asList(mPrefixLetters));
            }

            for (int i = 0; i < LETTERS.length; i++) {
                String letter = LETTERS[i];
                int count = IndexBar.countLetter(mDbUtils, mTableClass, mPinyinColumnName, letter);
                if (count > 0) {
                    existLetters.add(letter);
                }
            }

            if (mSuffixLetters != null && mSuffixLetters.length > 0) {
                existLetters.addAll(Arrays.asList(mSuffixLetters));
            }
            return existLetters.toArray(new String[0]);
        }

        @Override protected void onPostExecute(String[] result) {
            mIndexLetters = result;
            loadLetterPositionRelation(mDbUtils, mTableClass, mPinyinColumnName);
            if (mIndexLetters != null && mIndexLetters.length > 0) {
                mInitIndexLetters = true;
            }
            invalidate();
        }

    }

}
