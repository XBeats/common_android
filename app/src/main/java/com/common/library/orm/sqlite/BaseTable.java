package com.common.library.orm.sqlite;

import java.io.Serializable;
import java.lang.reflect.Field;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.common.library.orm.annotation.Column;
import com.common.library.orm.annotation.ID;
import com.common.library.orm.annotation.Table;
import com.common.library.orm.annotation.Transient;

/**
 * Base class of all table classes.
 * 
 * @author zhangfei
 *
 */
public abstract class BaseTable implements Serializable {
	private static final long serialVersionUID = -6833637753877258272L;

	// Newly created objects get this id
	public static final long NOT_SAVED = 0;

	// The id of the Content
	@ID
	@Column(columnName = _ID)
	public long id = NOT_SAVED;

	// All classes share this
	public static final String _ID = "_id";
	public static final String[] COUNT_COLUMNS = new String[] { "count(*)" };
	public static final String[] ID_PROJECTION = new String[] { _ID };
	public static final int ID_PROJECTION_COLUMN = 0;
	public static final String ID_SELECTION = _ID + " =?";

	/**
	 * Write the Content into a ContentValues container
	 */
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		Field[] fields = Tables.getAndSaveFields(getClass());

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Transient trans = field.getAnnotation(Transient.class);
			if (trans != null) {
				continue;
			} else {
				Column column = field.getAnnotation(Column.class);
				if (column == null) {
					Table table = this.getClass().getAnnotation(Table.class);
					throw new SQLiteException("No @Column or @Transient defined for field \"" + field.getName()
							+ "\" in table \"" + table.name() + "\"");
				}

				String columnName = column.columnName();
				if (TextUtils.isEmpty(columnName)) {
					columnName = field.getName();
				}

				// record which not saved into database yet, its id should not be put into content values,
				// since its id will be generated automatically by auto increment
				try {
					if (columnName.equals(_ID) && field.getLong(this) == NOT_SAVED) {
						continue;
					}
				} catch (IllegalAccessException e) {
					throw new SQLiteException("IllegalAccessException: " + e.getMessage());
				} catch (IllegalArgumentException e) {
					throw new SQLiteException("IllegalArgumentException: " + e.getMessage());
				}

				try {
					// put not null field value into ContentValues
					if (field.get(this) != null) {
						values.put(columnName, field.get(this).toString());
					}
				} catch (IllegalAccessException e) {
					throw new SQLiteException("IllegalAccessException:" + e.getMessage());
				} catch (IllegalArgumentException e) {
					throw new SQLiteException("IllegalArgumentException:" + e.getMessage());
				}
			}
		}
		return values;
	}

	/**
	 * Read the Content from a ContentCursor.
	 */
	public void restore(Cursor cursor) {
		Field[] fields = Tables.getAndSaveFields(getClass());
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Transient trans = field.getAnnotation(Transient.class);
			if (trans != null) {
				continue;
			} else {
				Column column = field.getAnnotation(Column.class);
				if (column == null) {
					Table table = this.getClass().getAnnotation(Table.class);
					throw new SQLiteException("No @Column or @Transient defined for field \"" + field.getName()
							+ "\" in table \"" + table.name() + "\"");
				}

				String columnName = column.columnName();
				if (TextUtils.isEmpty(columnName)) {
					columnName = field.getName();
				}

				setField(field, this, column, cursor, columnName);
			}
		}
	}

	private <T extends BaseTable> void setField(Field field, T tableObj, Column column, Cursor cursor, String columnName) {
		try {
			int columnIndex = cursor.getColumnIndex(columnName);
			Class<?> dataTypeClass = field.getType();
			
			if ((dataTypeClass == Integer.class || dataTypeClass == int.class)) {
				field.set(tableObj, cursor.getInt(columnIndex));
			} else if (dataTypeClass == Long.class || dataTypeClass == long.class) {
				field.set(tableObj, cursor.getLong(columnIndex));
			} else if (dataTypeClass == String.class) {
				field.set(tableObj, cursor.getString(columnIndex));
			} else if (dataTypeClass == Short.class || dataTypeClass == short.class) {
				field.set(tableObj, cursor.getShort(columnIndex));
			} else if (dataTypeClass == Double.class || dataTypeClass == double.class) {
				field.set(tableObj, cursor.getDouble(columnIndex));
			} else if (dataTypeClass == Float.class || dataTypeClass == float.class) {
				field.set(tableObj, cursor.getFloat(columnIndex));
			} else if (dataTypeClass == Boolean.class || dataTypeClass == boolean.class) {
				field.set(tableObj, cursor.getInt(columnIndex) == 1);
			} else if (dataTypeClass == Byte[].class || dataTypeClass == byte[].class) {
				field.set(tableObj, cursor.getBlob(columnIndex));
			} else {
				throw new SQLiteException("field \"" + field.getName() + "\" is not primitive data type.");
			}
		} catch (IllegalAccessException e) {
			throw new SQLiteException("IllegalAccessException:" + e.getMessage());
		} catch (IllegalArgumentException e) {
			Table table = this.getClass().getAnnotation(Table.class);
			throw new SQLiteException("ursor value cannot be converted to field's value for field \"" + field.getName()
					+ "\" in table \"" + table.name() + "\"");
		}
	}

	public boolean isSaved() {
		return id != NOT_SAVED;
	}
}
