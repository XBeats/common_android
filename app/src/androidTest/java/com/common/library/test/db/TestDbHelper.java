package com.common.library.test.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.common.library.orm.sqlite.BaseDbHelper;

public class TestDbHelper extends BaseDbHelper {
	private static final String DATABASE_NAME = "mict_partient.db";
	private static final int VERSION = 1;
	private static TestDbHelper instance;
	
	public static TestDbHelper getDbHelper(Context context){
		if(instance == null){
			synchronized (mLocker) {
				instance = new TestDbHelper(context);
			}
		}
		return instance;
	}
	
	protected TestDbHelper(Context context) {
		super(context, DATABASE_NAME, VERSION);
	}
	
	@Override
	protected void onSQLiteOpen() {
		addTableClass(Account.class);
		addTableClass(Permission.class);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
