package com.common.library.ui.indexlistview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.common.library.R;
import com.common.library.ui.widget.NoScrollGridView;
import com.common.library.utils.HanziToPinyin;

/**
 * Cursor adapter for list view in {@link IndexListViewActivity}
 *
 * @author zf08526
 */
@SuppressLint("InflateParams") 
public class IndexListViewAdapter extends CursorAdapter {
    private String mPinyinColumnName;
    private String mDataColumnName;
    private OnLetterItemClickedListener mLetterItemClickedListener;

    public IndexListViewAdapter(Context context, String pinyinColumnName, String dataColumnName) {
        super(context, null, 0);
        mPinyinColumnName = pinyinColumnName;
        mDataColumnName = dataColumnName;
    }

    protected interface OnLetterItemClickedListener {
        void onClicked(String value);
    }

    public void setOnLetterItemClickedListener(OnLetterItemClickedListener listener) {
        mLetterItemClickedListener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new LinearLayout(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int index = cursor.getColumnIndex(IndexPrefix.FIELD_PREFIX_TYPE);
        if (index > 0){
            boolean isTitleItem = cursor.getInt(cursor.getColumnIndex(IndexPrefix.FIELD_IS_TITLE_ITEM)) == 1;
            if (isTitleItem){
                LinearLayout container = (LinearLayout) view;
                container.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER_VERTICAL);
                container.addView(LayoutInflater.from(context).inflate(R.layout.index_list_item, null), params);
                bindDataForPrefixTitleItem(view, cursor);
            } else {
                LinearLayout container = (LinearLayout) view;
                container.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER_VERTICAL);
                container.addView(LayoutInflater.from(context).inflate(R.layout.gridview_in_city_list, null), params);

                String prefixType = cursor.getString(index);
                if (IndexPrefix.PREFIX_TYPE_CURRENT_CITY.equals(prefixType)
                        || IndexPrefix.PREFIX_TYPE_HISTORY_CITY.equals(prefixType)
                        || IndexPrefix.PREFIX_TYPE_HOT_CITY.equals(prefixType)){
                    bindDataForGridView(context, view, cursor);
                }
            }
        } else {
            LinearLayout container = (LinearLayout) view;
            container.removeAllViews();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER_VERTICAL);
            container.addView(LayoutInflater.from(context).inflate(R.layout.index_list_item, null), params);
            bindDataForCityData(view, cursor);
        }
    }

    private void bindDataForPrefixTitleItem(View view, Cursor cursor){
        final LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.ll_content);
        final TextView contentView = (TextView) view.findViewById(R.id.tv_content);

        String title = cursor.getString(cursor.getColumnIndex(IndexPrefix.FIELD_PREFIX_TYPE));
        contentView.setText(title);
        contentLayout.setBackgroundColor(Color.parseColor("#DCDCDC"));
        contentLayout.setOnClickListener(null);
    }

    private void bindDataForGridView(Context context, View view, Cursor cursor){
        String json = cursor.getString(cursor.getColumnIndex(IndexPrefix.FIELD_EXTRA_VALUE));
        try {
            JSONArray jsonArray = new JSONArray(json);

            List<Map<String, String>> gridViewValues = new ArrayList<Map<String, String>>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                Map<String, String> map = new HashMap<String, String>();
                map.put("city_name", value);
                gridViewValues.add(map);
            }

            NoScrollGridView gridView = (NoScrollGridView) view.findViewById(R.id.grid_view_item);
            SimpleAdapter adapter = new SimpleAdapter(
                    context,
                    gridViewValues,
                    android.R.layout.simple_gallery_item,
                    new String[]{"city_name"},
                    new int[]{android.R.id.text1});
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mLetterItemClickedListener != null) {
                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                        mLetterItemClickedListener.onClicked(textView.getText().toString().trim());
                    }
                }
            });
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void bindDataForCityData(View view, Cursor cursor){
        final LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.ll_content);
        final TextView contentView = (TextView) view.findViewById(R.id.tv_content);

        String fullPinyin = cursor.getString(cursor.getColumnIndex(mPinyinColumnName));
        String firstLetter = getDisplayingChar(fullPinyin);

        boolean isTitleItem = false;

        // first item in list view -> always show title item
        if (cursor.getPosition() == 0) {
            boolean isPrefixItem = cursor.getColumnIndex(IndexPrefix.FIELD_PREFIX_TYPE) > 0;
            if (isPrefixItem){
                bindDataForPrefixTitleItem(view, cursor);
                return;
            }

            isTitleItem = true;
            contentView.setText(firstLetter.toUpperCase(Locale.getDefault()));
            contentLayout.setBackgroundColor(Color.parseColor("#DCDCDC"));
        } else {
            cursor.moveToPrevious();
            boolean isPrefixItem = cursor.getColumnIndex(IndexPrefix.FIELD_PREFIX_TYPE) > 0;
            if (isPrefixItem){
                contentView.setText(firstLetter.toUpperCase(Locale.getDefault()));
                contentLayout.setBackgroundColor(Color.parseColor("#DCDCDC"));
                return;
            }

            String previewFullPinyin = cursor.getString(cursor.getColumnIndex(mPinyinColumnName));
            String previewFirstLetter = getDisplayingChar(previewFullPinyin);

            if (!previewFirstLetter.equalsIgnoreCase(firstLetter)) {
                isTitleItem = true;
                contentView.setText(firstLetter.toUpperCase(Locale.getDefault()));
                contentLayout.setBackgroundColor(Color.parseColor("#DCDCDC"));
            }

            cursor.moveToNext();
        }

        // set displaying field
        if (!isTitleItem) {
            contentLayout.setBackgroundResource(android.R.drawable.list_selector_background);
            String data = cursor.getString(cursor.getColumnIndex(mDataColumnName));
            contentView.setText(data);
            contentLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mLetterItemClickedListener != null) {
                        mLetterItemClickedListener.onClicked(contentView.getText().toString().trim());
                    }
                }
            });
        } else {
            contentLayout.setOnClickListener(null);
        }
    }


    private String getDisplayingChar(String pinyin) {
        if (TextUtils.isEmpty(pinyin)) {
            return "";
        }

        if (HanziToPinyin.isChinese(pinyin)) {
            return pinyin;
        } else {
            if (pinyin.length() == 1) {
                return pinyin;
            } else {
                return pinyin.substring(0, 1);
            }
        }
    }

}
