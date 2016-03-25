package com.common.library.orm.sqlite;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.text.TextUtils;

import com.common.library.orm.DataType;
import com.common.library.orm.annotation.Column;
import com.common.library.orm.annotation.Foreign;
import com.common.library.orm.annotation.ID;
import com.common.library.orm.annotation.Table;
import com.common.library.orm.annotation.Transient;

/**
 * Factory tool used to build plain SQL script with specified table class.
 * 
 * @author zf08526
 * 
 */
public final class SQLBuilder {
	private SQLBuilder(){}
	
	public static String getTableName(Class<? extends BaseTable> tableClass){
		return Tables.getTableName(tableClass);
	}
	
	public static String getColumnName(Field field){
		Column column = field.getAnnotation(Column.class);
		Transient tr = field.getAnnotation(Transient.class);
		if (column == null && tr == null) {
			throw new SQLiteException("neight @Transient nor @Column are defined for field ["
					+ field.getName() + "]");
		}
		String columnName = column.columnName();
		if (TextUtils.isEmpty(columnName)) {
			columnName = field.getName();
		}
		return columnName;
	}
	
	public static <T extends BaseTable> Object getFieldValue(T table, Field field) {
		try {
            field.setAccessible(true);
            return field.get(table);
        } catch (Throwable e) {
			throw new SQLiteException("Field '[" + field.getName()
					+ "]' is not accessable.");
        }
    }
	
	static SQL buildTableCreateSQL(Class<? extends BaseTable> tableClass) {
		// get table name
		String tableName = SQLBuilder.getTableName(tableClass);

		// builder string to create table
		final StringBuilder buffer = new StringBuilder();
		buffer.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
		buffer.append(" (");
		Field[] fields = Tables.getAndSaveFields(tableClass);

		int index = 0;// no need ", " for last column
		for (Field field : fields) {
			index++;

			// field having @Transient will not treat as table column
			Transient tr = field.getAnnotation(Transient.class);
			if (tr != null) {
				continue;
			}

			// add column definition
			String columnName = getColumnName(field);
			Column column = field.getAnnotation(Column.class);

			validateFieldType(field, tableName);

			// add column name and type definition
			String columnType = DataType.getDataTypeByField(field);
			buffer.append(columnName).append(" ").append(columnType);

			// add id definition if it was id column
			ID id = field.getAnnotation(ID.class);
			if (id != null) {
				buffer.append(" PRIMARY KEY AUTOINCREMENT");
			}

			// add 'default value' definition
			if (!TextUtils.isEmpty(column.defaultValue())) {
				buffer.append(" DEFAULT '" + column.defaultValue() + "'");
			}
			
			// add unique definition
			boolean unique = column.unique();
			if(unique){
				buffer.append(" UNIQUE");
			}

			// add 'not null' definition
			if (column.notNull()) {
				buffer.append(" NOT NULL");
			}

			// add foreign key definition
			Foreign foreign = field.getAnnotation(Foreign.class);
			if (foreign != null) {
				Class<? extends BaseTable> refTableClass = foreign.tableClass();
				Table refTable = refTableClass.getAnnotation(Table.class);
				String refTableName = refTable.name();
				String refColumnName = BaseTable._ID;
				buffer.append(" REFERENCES " + refTableName + "(" + refColumnName + ")");
			}

			if (index != fields.length) {
				buffer.append(", ");
			}
		}
		buffer.append(");");
		return new SQL(buffer.toString());
	}
	
	private static void validateFieldType(Field field, String tableName) {
		Class<?> fieldTypeClass = field.getType();

		if (fieldTypeClass != Integer.class 
				&& fieldTypeClass != int.class 
				&& fieldTypeClass != Short.class
				&& fieldTypeClass != short.class
				&& fieldTypeClass != Double.class
				&& fieldTypeClass != double.class
				&& fieldTypeClass != Float.class
				&& fieldTypeClass != float.class 
				&& fieldTypeClass != Long.class
				&& fieldTypeClass != long.class
				&& fieldTypeClass != Boolean.class
				&& fieldTypeClass != boolean.class
				&& fieldTypeClass != String.class 
				&& fieldTypeClass != Byte[].class 
				&& fieldTypeClass != byte[].class) {
			throw new SQLiteException(
					"field type is not supported to be converted into sqlite column type for field \""
							+ field.getName() + "\" in table class \"" + tableName + "\"");
		}
	}
	
	//-- insert sql
    public static <T extends BaseTable> SQL buildInsertSQL(T table) {
        List<KeyValue> keyValueList = table2KeyValueList(table);
        if (keyValueList.size() == 0){
        	return null;
        }

        StringBuffer buffer = new StringBuffer();
        SQL sql = new SQL();
        buffer.append("INSERT INTO ");
        buffer.append(SQLBuilder.getTableName(table.getClass()));
        buffer.append(" (");
        for (KeyValue kv : keyValueList) {
        	if(BaseTable._ID.equals(kv.key)){
        		continue;
        	}
            buffer.append(kv.key).append(",");
            sql.addBindArg(kv.value);
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(") VALUES (");

        for (KeyValue kv : keyValueList) {
        	if(BaseTable._ID.equals(kv.key)){
        		continue;
        	}
        	 buffer.append("?,");
        }
        
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(")");
        sql.setSql(buffer.toString());
        return sql;
    }

    // -- delete sql
    public static <T extends BaseTable> SQL buildDeleteSQL(T table){
        if (table == null || table.id == BaseTable.NOT_SAVED) {
            throw new SQLiteException("this table[" + table.getClass().getName() + "]'s id value is not illegal.");
        }
        
        StringBuilder buffer = new StringBuilder("DELETE FROM " + SQLBuilder.getTableName(table.getClass()));
        buffer.append(" WHERE ").append(BaseTable._ID + "=" + table.id);
        return new SQL(buffer.toString());
    }

    public static <T extends BaseTable> SQL buildDeleteSQL(Class<T> tableClass, long id) {
        if (id == BaseTable.NOT_SAVED) {
            throw new SQLiteException("this table[" + SQLBuilder.getTableName(tableClass) + "]'s id value is null");
        }
        
        StringBuilder buffer = new StringBuilder("DELETE FROM " + SQLBuilder.getTableName(tableClass));
        buffer.append(" WHERE ").append(BaseTable._ID + "=" + id);
        return new SQL(buffer.toString());
    }
    
    public static <T extends BaseTable> SQL buildDeleteSQL(Class<T> tableClass, String where, 
    		String[] selectionArgs) {
    	StringBuilder buffer = new StringBuilder("DELETE FROM " + SQLBuilder.getTableName(tableClass));
    	if (where != null && where.length() > 0) {
            buffer.append(" WHERE ").append(buildWhere(where, selectionArgs));
        }
        return new SQL(buffer.toString());
    }
    
    // -- update sql
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public static <T extends BaseTable> SQL buildUpdateSQL(Class<T> tableClass, long id, ContentValues values) {
        if (values == null || values.size() == 0) {
        	return null;
        }

        String tableName = getTableName(tableClass);
        if (id == BaseTable.NOT_SAVED) {
            throw new SQLiteException("this table [" + tableName + "]'s id value is null");
        }

        SQL sql = new SQL();
        StringBuffer sqlBuffer = new StringBuffer("UPDATE ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" SET ");
        Set<String> columnNames = values.keySet();
        
        for (String columnName : columnNames) {
        	sqlBuffer.append(columnName).append("=?,");
            sql.addBindArg(values.getAsString(columnName));
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" WHERE ").append(BaseTable._ID + "=" + id);

        sql.setSql(sqlBuffer.toString());
        return sql;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public static <T extends BaseTable> SQL buildUpdateSQL(Class<T> tableClass, String where, 
    		String[] selectionArgs, ContentValues values) {
        if (values == null || values.size() == 0) {
        	return null;
        }
        
        SQL result = new SQL();
        StringBuffer buffer = new StringBuffer("UPDATE ");
        buffer.append(getTableName(tableClass));
        buffer.append(" SET ");
        Set<String> keys = values.keySet();
        for (String columnName : keys) {
        	buffer.append(columnName).append("=?,");
            result.addBindArg(values.getAsString(columnName));
        }
        buffer.deleteCharAt(buffer.length() - 1);
        if (where != null && where.length() > 0) {
            buffer.append(" WHERE ").append(buildWhere(where, selectionArgs));
        }

        result.setSql(buffer.toString());
        return result;
    }

    private static <T extends BaseTable> KeyValue column2KeyValue(T table, Field field, Column column) {
        KeyValue kv = null;
        String key = column.columnName();
        if (key != null) {
            Object value = SQLBuilder.getFieldValue(table, field);
            value = value == null ? column.defaultValue() : value;
            kv = new KeyValue(key, value);
        }
        return kv;
    }

    public static <T extends BaseTable> ArrayList<KeyValue> table2KeyValueList(T table) {
    	ArrayList<KeyValue> keyValueList = new ArrayList<KeyValue>();
        Field[] fields = Tables.getAndSaveFields(table.getClass());
        
        for (Field field : fields) {
        	Column column = field.getAnnotation(Column.class);
        	if(column != null){
        		KeyValue kv = column2KeyValue(table, field, column);
        		if (kv != null) {
        			keyValueList.add(kv);
        		}
        	}
        }
        return keyValueList;
    }
    
    private static String buildWhere(String where, String[] whereArgs){
		if(whereArgs != null && whereArgs.length > 0){
			List<String> args = Arrays.asList(whereArgs);
			for(String arg : args){
				int index = where.indexOf("?");
				if(index > 0){
					String convertedArg = SQL.convert2DBValue(arg).toString();
					where = where.replaceFirst("\\?", convertedArg);
				}
			}
		}
		return where;
	}
    
}
