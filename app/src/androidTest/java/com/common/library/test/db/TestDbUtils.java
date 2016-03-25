package com.common.library.test.db;

import android.content.Context;

import com.common.library.orm.sqlite.DbUtils;

public class TestDbUtils {
	public static DbUtils getDBUtils(Context context) {
		return DbUtils.create(TestDbHelper.getDbHelper(context));
	}
}
