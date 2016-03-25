package com.common.library.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * Recommend usage:
 * 
 * <pre>
 * public View getView(int position, View convertView, ViewGroup parent) {
 * 
 * 	if (convertView == null) {
 * 		convertView = LayoutInflater.from(context).inflate(R.layout.banana_phone, parent, false);
 * 	}
 * 
 * 	ImageView bananaView = ViewHolder.get(convertView, R.id.banana);
 * 	TextView phoneView = ViewHolder.get(convertView, R.id.phone);
 * 
 * 	BananaPhone bananaPhone = getItem(position);
 * 	phoneView.setText(bananaPhone.getPhone());
 * 	bananaView.setImageResource(bananaPhone.getBanana());
 * 
 * 	return convertView;
 * }
 * </pre>
 * 
 * @author zf08526
 * 
 */
public final class ViewHolder {

	@SuppressWarnings("unchecked")
	public static <T extends View> T get(View rootView, int viewId) {
		SparseArray<View> viewHolder = (SparseArray<View>) rootView.getTag();

		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			rootView.setTag(viewHolder);
		}

		View childView = viewHolder.get(viewId);
		if (childView == null) {
			childView = rootView.findViewById(viewId);
			viewHolder.put(viewId, childView);
		}

		return (T) childView;
	}
}
