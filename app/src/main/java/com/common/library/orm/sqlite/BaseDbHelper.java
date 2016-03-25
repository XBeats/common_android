package com.common.library.orm.sqlite;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A help class based on {@link SQLiteOpenHelper} to create database automatically
 * and get created database, but need to override
 * {@link BaseDbHelper#onSQLiteOpen()} instead of
 * {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)}.
 * 
 * @author zhangfei
 * 
 */
public class BaseDbHelper extends SQLiteOpenHelper {
	private Context mContext;
	protected static Object mLocker = new Object();

	/**
	 * Override this method instead of
	 * {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)}, call
	 * {@link BaseDbHelper#addTableClass(Class)} to save table classes, with the
	 * table classes tables will be created automatically and relations between
	 * table class and table will also be saved, finally with the table class
	 * and {@link DbUtils} you can do many SQL jobs with inner API.
	 */
	protected void onSQLiteOpen() {
	}

	protected BaseDbHelper(Context context, String databaseName, int version) {
		super(context, databaseName, null, version);
		mContext = context;
		onSQLiteOpen();
	}

	public Context getContext() {
		return mContext;
	}

	protected void addTableClass(Class<? extends BaseTable> tableClass) {
		Tables.addMapping(tableClass);
	}

	/**
	 * Table creation will work automatically, so you should not override this
	 * method by yourself.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		List<Class<? extends BaseTable>> tableClasses = Tables.getTableClasses();
		for (Class<? extends BaseTable> clazz : tableClasses) {
			createTable(db, clazz);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Create table with specified table class
	 * 
	 * @param db
	 *            SQLiteDatabase
	 * @param tableClass
	 *            table class
	 */
	protected void createTable(SQLiteDatabase db,
			Class<? extends BaseTable> tableClass) {
		Tables.addMapping(tableClass);
		db.execSQL(SQLBuilder.buildTableCreateSQL(tableClass).getSql());
	}

}
