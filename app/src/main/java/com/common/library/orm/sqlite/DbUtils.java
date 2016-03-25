package com.common.library.orm.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;


/**
 * A convenient tool to do CRUD actions on SQLite database.
 * 
 * @author zhangfei
 * 
 */
public final class DbUtils {
	private SQLiteDatabase database;
	private BaseDbHelper dbHelper;

	/**
	 * Create or retrieve sqlite utils instance.
	 * 
	 * @param dbHelper
	 * @return singleton of SqliteUtils.
	 */
	public static DbUtils create(BaseDbHelper dbHelper) {
		return new DbUtils(dbHelper);
	}

	private DbUtils(BaseDbHelper dbHelper) {
		this.dbHelper = dbHelper;
		this.database = dbHelper.getWritableDatabase();
	}

	public SQLiteDatabase getDatabase() {
		if (database != null && database.isOpen()) {
			while (database.isDbLockedByCurrentThread()) {
				continue;
			}
			return database;
		} else {
			database = dbHelper.getWritableDatabase();
			while (database.isDbLockedByCurrentThread()) {
				continue;
			}
			return database;
		}
	}

	// The Content sub class must have a no-arg constructor
	protected <T extends BaseTable> T getContent(Cursor cursor, Class<T> tableClass) {
		try {
			T content = tableClass.newInstance();
			content.id = cursor.getLong(0);
			content.restore(cursor);
			return content;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Close database, should called after db operations are completed.
	 */
	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	/**
	 * Get all record count of table.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @return count result
	 */
	public <T extends BaseTable> int count(Class<T> tableClass) {
		String tableName = Tables.getTableName(tableClass);
		Cursor c = getDatabase().query(tableName, BaseTable.COUNT_COLUMNS, null, null, null, null, null);
		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}
		try {
			if (c.moveToFirst()) {
				return c.getInt(0);
			} else {
				return 0;
			}
		} finally {
			c.close();
		}
	}

	/**
	 * Get record count of table with selections.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @return count result
	 */
	public <T extends BaseTable> int count(Class<T> tableClass, String selection, String[] selectionArgs) {
		String tableName = Tables.getTableName(tableClass);
		Cursor c = getDatabase().query(tableName, BaseTable.COUNT_COLUMNS, selection, selectionArgs, null, null, null);
		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}
		try {
			if (c.moveToFirst()) {
				return c.getInt(0);
			} else {
				return 0;
			}
		} finally {
			c.close();
		}
	}

	/**
	 * Query and return all subclass of {@link BaseTable} instances as a list
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @return subclass of {@link BaseTable}'s instances list
	 */
	public <T extends BaseTable> List<T> findAll(Class<T> tableClass) {
		String orderBy = Tables.getDefaultOrderBy(tableClass);
		return find(tableClass, null, null, null, null, orderBy);
	}

	/**
	 * Like {@link #findAll(Class)} but return {@code Cursor} instead.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @return query result cursor
	 */
	public <T extends BaseTable> Cursor findAllToCursor(Class<T> tableClass) {
		String orderBy = Tables.getDefaultOrderBy(tableClass);
		return findToCursor(tableClass, null, null, null, null, orderBy);
	}

	/**
	 * Query and return subclass of {@link BaseTable} instance by table primary
	 * key id's value.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param id
	 *            primary key id's value
	 * @return subclass of {@link BaseTable}'s instance.
	 */
	public <T extends BaseTable> T findById(Class<T> tableClass, long id) {
		String tableName = Tables.getTableName(tableClass);
		Cursor c = getDatabase().query(tableName, null, BaseTable._ID + "=?", new String[] { String.valueOf(id) },
				null, null, null);
		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}
		try {
			if (c.moveToFirst()) {
				return getContent(c, tableClass);
			} else {
				return null;
			}
		} finally {
			c.close();
		}
	}

	/**
	 * Query and return subclass of {@link BaseTable} instances as a list with
	 * size limit for pagination.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limitOffset
	 * @param  limitSize
	 * @return subclass of {@link BaseTable}'s instances list.
	 */
	public <T extends BaseTable> List<T> findWithLimit(Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, int limitOffset, int limitSize) {
		Cursor c = findWithLimitToCursor(tableClass, selection, selectionArgs, groupBy, having, orderBy, limitOffset,
				limitSize);
		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}
		List<T> entities = new ArrayList<T>();
		try {
			while (c.moveToNext()) {
				entities.add(getContent(c, tableClass));
			}
		} finally {
			c.close();
		}
		return entities;
	}

	/**
	 * Be similar with {@link #findWithLimit(Class, String, String[], String, String, String, int, int)}
	 * but return {@code Cursor} instead.
	 * 
	 * @param tableClass
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limitOffset
	 * @param limitSize
	 * @return result cursor of query
	 */
	private <T extends BaseTable> Cursor findWithLimitToCursor(Class<T> tableClass, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy, int limitOffset, int limitSize) {

		String limit;
		if (limitOffset == 0 && limitSize == 0) {
			limit = null;
		} else {
			limit = limitOffset + "," + limitSize;
		}

		String tableName = Tables.getTableName(tableClass);
		if (TextUtils.isEmpty(orderBy)){
			orderBy = Tables.getDefaultOrderBy(tableClass);
		}
		Cursor c = getDatabase().query(tableName, null, selection, selectionArgs, groupBy, having, orderBy, limit);
		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}
		return c;
	}

	/**
	 * Query and return subclass of {@link BaseTable} instance with selections.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return subclass of {@link BaseTable}'s instances list
	 */
	public <T extends BaseTable> List<T> find(Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		return findWithLimit(tableClass, selection, selectionArgs, groupBy, having, orderBy, 0, 0);
	}

	/**
	 * Like {@link #find(Class, String, String[], String, String, String)} but
	 * return {@code Cursor} instead.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return result cursor of query
	 */
	public <T extends BaseTable> Cursor findToCursor(Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		return findWithLimitToCursor(tableClass, selection, selectionArgs, groupBy, having, orderBy, 0, 0);
	}

	/**
	 * Query and return the first record of subclass of {@link BaseTable}
	 * instance with selections.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return subclass of {@link BaseTable}'s instance
	 */
	public <T extends BaseTable> T findFirst(Class<T> tableClass, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		String tableName = Tables.getTableName(tableClass);
		Cursor c = getDatabase().query(tableName, null, selection, selectionArgs, groupBy, having, orderBy);

		if (c == null) {
			throw new SQLiteException("Cannot create cursor object, database or columns may have error...");
		}

		try {
			if (c.moveToFirst()) {
				return getContent(c, tableClass);
			} else {
				return null;
			}
		} finally {
			c.close();
		}
	}

	/**
	 * Insert table with one record.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param record
	 *            subclass of {@link BaseTable}'s instance
	 */
	public <T extends BaseTable> long save(Class<T> tableClass, T record) {
		String tableName = Tables.getTableName(tableClass);
		return getDatabase().insert(tableName, null, record.toContentValues());
	}

	/**
	 * Insert table with more than one records.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param tables
	 *            records to save into database.
	 * @return primary key ids of records
	 */
	public <T extends BaseTable> List<Long> saveAll(Class<T> tableClass, List<T> tables) {
		List<Long> ids = new ArrayList<Long>();
		if (tables == null || tables.size() == 0) {
			return ids;
		}

		SQLiteDatabase db = getDatabase();
		try {
			db.beginTransaction();
			for (T table : tables) {
				SQL sql = SQLBuilder.buildInsertSQL(table);
				db.execSQL(sql.getSql(), sql.getBindArgsAsArray(false));
			}
			db.setTransactionSuccessful();
			return ids;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Update recored with content values.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param id
	 *            the primary key id's value of table you want to update.
	 * @param values
	 *            table columns to be updated were defined here.
	 */
	public <T extends BaseTable> int update(Class<T> tableClass, long id, ContentValues values) {
		if (id == BaseTable.NOT_SAVED) {
			throw new UnsupportedOperationException();
		}

		String tableName = Tables.getTableName(tableClass);
		return getDatabase().update(tableName, values, BaseTable._ID + "=?", new String[] { String.valueOf(id) });
	}

	/**
	 * Delete all records in table.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @return deleted record's count
	 */
	public <T extends BaseTable> int deleteAll(Class<T> tableClass) {
		return delete(tableClass, null, null);
	}

	/**
	 * Update records with selection and values.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param where
	 * @param selectionArgs
	 * @param values
	 * @return updated records' count
	 */
	public <T extends BaseTable> int update(Class<T> tableClass, String where, String[] selectionArgs,
			ContentValues values) {
		String tableName = Tables.getTableName(tableClass);
		return getDatabase().update(tableName, values, where, selectionArgs);
	}

	/**
	 * Update record with properties if its instance.
	 * 
	 * @param table
	 * @return updated records' count
	 */
	public <T extends BaseTable> int update(T table) {
		if (table == null) {
			throw new SQLiteException("Record to be updated cannot be null");
		}

		if (table.id == BaseTable.NOT_SAVED) {
			throw new SQLiteException("Record cannot update since its primary key is -1");
		}

		return update(table.getClass(), table.id, table.toContentValues());
	}

	/**
	 * Delete record with its primary key id.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param id
	 *            value of primary key id
	 * @return deleted record count
	 */
	public <T extends BaseTable> int deleteById(Class<T> tableClass, long id) {
		if (id == BaseTable.NOT_SAVED) {
			return 0;
		}
		String tableName = Tables.getTableName(tableClass);
		return getDatabase().delete(tableName, BaseTable._ID + "=?", new String[] { String.valueOf(id) });
	}

	/**
	 * Delete record by its instance.
	 * 
	 * @param table
	 * @return deleted record count
	 */
	public <T extends BaseTable> int delete(T table) {
		if (table == null) {
			throw new SQLiteException("Record cannot be null");
		}

		if (table.id == BaseTable.NOT_SAVED) {
			throw new SQLiteException("Record do not exist since its primary key is -1");
		}

		return deleteById(table.getClass(), table.id);
	}

	/**
	 * Delete records with selections.
	 * 
	 * @param tableClass
	 *            subclass of {@link BaseTable}
	 * @param selection
	 * @param selectionArgs
	 * @return deleted records' count
	 */
	public <T extends BaseTable> int delete(Class<T> tableClass, String selection, String[] selectionArgs) {
		String tableName = Tables.getTableName(tableClass);
		return getDatabase().delete(tableName, selection, selectionArgs);
	}

	/**
	 * Execute more than one SQL job with transaction.
	 * 
	 * @param batchJobs
	 * @see {@link BatchJobs}
	 */
	public <T extends BaseTable> void executeBatchJobs(BatchJobs batchJobs) {
		SQLiteDatabase database = getDatabase();
		try {
			database.beginTransaction();
			ArrayList<SQL> bindArgs = batchJobs.getBatchJobs();
			for (SQL job : bindArgs) {
				Object[] args = job.getBindArgsAsArray(false);
				if (args != null && args.length > 0) {
					database.execSQL(job.getSql(), args);
				} else {
					database.execSQL(job.getSql());
				}
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
	}
}
