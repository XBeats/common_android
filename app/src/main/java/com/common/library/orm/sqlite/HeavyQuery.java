package com.common.library.orm.sqlite;

import java.util.List;

import android.os.AsyncTask;

/**
 * For some special SQL query which need more time to get result, it should be
 * queried in background thread, but you can use this for convenience.
 * 
 * @author zhangfei
 * 
 */
public abstract class HeavyQuery<T extends BaseTable> extends AsyncTask<Void, Void, List<T>> {
	// query types
	private static final int QUERY_TYPE_FIND = 0;
	private static final int QUERY_TYPE_FIND_ALL = 1;
	private static final int QUERY_TYPE_FIND_LIMIT = 2;

	private DbUtils mDbUtils;
	private Class<T> mTableClass;
	private String mSelection;
	private String[] mSelectionArgs;
	private String mGroupBy;
	private String mHaving;
	private String mOrderBy;
	private int mLimitOffset;
	private int mLimitSize;
	private int mQueryType;

	/**
	 * Query result will return via this callback.
	 * 
	 * @param result
	 *            A List contains search result.
	 */
	public abstract  void onQueryComplete(List<T> result);

	/**
	 * Be similar with {@link DbUtils#findAll(Class)}
	 */
	public HeavyQuery(DbUtils dbUtils, Class<T> tableClass) {
		mQueryType = QUERY_TYPE_FIND_ALL;
		mDbUtils = dbUtils;
		mTableClass = tableClass;
	}

	/**
	 * Be similar with
	 * {@link DbUtils#find(Class, String, String[], String, String, String)}
	 */
	public HeavyQuery(DbUtils dbUtils, Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		mQueryType = QUERY_TYPE_FIND;
		mTableClass = tableClass;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mGroupBy = groupBy;
		mHaving = having;
		mOrderBy = orderBy;
	}

	/**
	 * Be similar with
	 * {@link DbUtils#findWithLimit(Class, String, String[], String, String, String, int, int)}
	 */
	public HeavyQuery(DbUtils dbUtils, Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, int limitOffset, int limitSize) {
		mQueryType = QUERY_TYPE_FIND_LIMIT;
		mTableClass = tableClass;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mGroupBy = groupBy;
		mHaving = having;
		mOrderBy = orderBy;
		mLimitOffset = limitOffset;
		mLimitSize = limitSize;
	}

	@Override
	protected List<T> doInBackground(Void... params) {
		if (mDbUtils == null) {
			throw new IllegalArgumentException("DbUtils cannot be null.");
		}

		if (mQueryType == QUERY_TYPE_FIND) {
			return mDbUtils.find(mTableClass, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy);
		} else if (mQueryType == QUERY_TYPE_FIND_ALL) {
			return mDbUtils.findAll(mTableClass);
		} else if (mQueryType == QUERY_TYPE_FIND_LIMIT) {
			return mDbUtils.findWithLimit(mTableClass, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy,
					mLimitOffset, mLimitSize);
		} else {
			throw new IllegalArgumentException("query type was not specified");
		}
	}

	@Override
	protected void onPostExecute(List<T> result) {
		onQueryComplete(result);
	}

	/**
	 * Start heavy SQL query, the query result will be responsed in
	 * {@link HeavyQuery#onQueryComplete(List)}
	 */
	public void startQuery() {
		execute();
	}

}
