package com.common.library.orm.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.common.library.orm.annotation.DefaultOrderBy;
import com.common.library.orm.annotation.Table;

/**
 * A singleton container to save the relation between table class and the cache
 * content, about the cache content @see {@link TableCache}.
 * 
 * @author zf08526
 * 
 */
final class Tables {
	private Map<Class<? extends BaseTable>, TableCache> tableCaches =
		new ConcurrentHashMap<Class<? extends BaseTable>, TableCache>();

	private static Tables singleton = null;
	private static final Object lockObj = new Object();

	/**
	 * The table cache content.
	 */
	public static final class TableCache {
		private String tableName;
		private Field[] fields;
		private String orderBy;

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public Field[] getFields() {
			return fields;
		}

		public void setFields(Field[] fields) {
			this.fields = fields;
		}

		public String getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(String orderBy) {
			this.orderBy = orderBy;
		}

	}

	private Tables() {
	}

	// singleton
	private static Tables getSingleton() {
		if (singleton == null) {
			synchronized (lockObj) {
				singleton = new Tables();
			}
		}
		return singleton;
	}

	/**
	 * Get cached table classes.
	 * 
	 * @return table classes.
	 */
	public static List<Class<? extends BaseTable>> getTableClasses() {
		List<Class<? extends BaseTable>> tableClasses = new ArrayList<Class<? extends BaseTable>>();
		Iterator<Class<? extends BaseTable>> iterator = 
				getSingleton().tableCaches.keySet().iterator();
		while (iterator.hasNext()) {
			tableClasses.add(iterator.next());
		}
		return tableClasses;
	}

	/**
	 * Bind and save relations between table class and its fields.
	 * 
	 * @param tableClass
	 * @param fields
	 */
	public static void putFields(Class<? extends BaseTable> tableClass,
			Field[] fields) {
		Tables mappingCache = getSingleton();
		TableCache content = mappingCache.tableCaches.get(tableClass);
		if (content == null) {
			content = new TableCache();
			content.setFields(fields);
			mappingCache.tableCaches.put(tableClass, content);
		} else {
			content.setFields(fields);
		}
	}

	/**
	 * Get all fields of table class.
	 * 
	 * @param tableClass
	 * @return all fields of table class.
	 */
	private static Field[] getFields(Class<? extends BaseTable> tableClass) {
		Tables mappingCache = getSingleton();
		TableCache content = mappingCache.tableCaches.get(tableClass);
		if (content == null || content.getFields() == null
				|| content.getFields().length == 0) {
			return null;
		}
		return content.getFields();
	}

	/**
	 * Try Get cached fields of table class, if not exist bind and save first.
	 * 
	 * @param tableClass
	 * @return all fields of table class.
	 */
	public static Field[] getAndSaveFields(Class<? extends BaseTable> tableClass) {
		Field[] fields = getFields(tableClass);
		if (fields == null || fields.length == 0) {
			Class<?> superClass = tableClass.getSuperclass();
			Field[] superClassFields = superClass.getDeclaredFields();
			Field[] tableClassFields = tableClass.getDeclaredFields();

			List<Field> totalFields = new ArrayList<Field>();
			totalFields.addAll(Arrays.asList(superClassFields));
			totalFields.addAll(Arrays.asList(tableClassFields));

			// filter out static fields which are not table field
			List<Field> fieldsToRemove = new ArrayList<Field>();
			for (Field field : totalFields) {
				if (Modifier.isStatic(field.getModifiers())) {
					fieldsToRemove.add(field);
				}
			}
			totalFields.removeAll(fieldsToRemove);

			fields = totalFields.toArray(new Field[totalFields.size()]);

			// save into cache
			TableCache content = getSingleton().tableCaches.get(tableClass);
			if (content == null) {
				content = new TableCache();
				content.setFields(fields);
				getSingleton().tableCaches.put(tableClass, content);
			} else {
				content.setFields(fields);
			}
		}
		return fields;
	}

	/**
	 * Add relation between table class and table into cache.
	 * 
	 * @param tableClass
	 */
	public static void addMapping(Class<? extends BaseTable> tableClass) {
		Table table = tableClass.getAnnotation(Table.class);
		if (table == null) {
			throw new SQLiteException(
					"table annotation is not defined for table ["
							+ tableClass.getSimpleName() + "]");
		}

		if (TextUtils.isEmpty(table.name())) {
			throw new SQLiteException("table name is not definied for table ["
					+ tableClass.getSimpleName() + "]");
		}

		TableCache cachedObject = getSingleton().tableCaches.get(tableClass);

		// save mapping if not saved before
		if (cachedObject == null) {
			cachedObject = new TableCache();
			cachedObject.setTableName(table.name());
			getSingleton().tableCaches.put(tableClass, cachedObject);
		} /*
		 * else { cachedObject.setTableName(table.name()); }
		 */

		// cache default order by for tables
		Field[] fields = Tables.getAndSaveFields(tableClass);
		String orderByStr = null;

		for (Field field : fields) {
			DefaultOrderBy orderBy = field.getAnnotation(DefaultOrderBy.class);
			if (orderBy != null) {
				String columnName = SQLBuilder.getColumnName(field);
				orderByStr = columnName + " " + orderBy.sortType();
				Tables.saveDefaultOrderBy(tableClass, orderByStr);
				break;
			}
		}
	}

	/**
	 * Get table name with table class.
	 * 
	 * @param tableClass
	 * @return table name
	 */
	public static String getTableName(Class<? extends BaseTable> tableClass) {
		Tables mappingCache = getSingleton();
		TableCache content = mappingCache.tableCaches.get(tableClass);
		if (content == null || TextUtils.isEmpty(content.getTableName())) {
			throw new SQLiteException(
					"The Table mapping of table: "
							+ tableClass.getSimpleName()
							+ " not exists. Please add mapping in child class of BaseDBHelper");
		}
		return content.getTableName();
	}

	/**
	 * Get default order by of table.
	 * 
	 * @param tableClass
	 * @return default order by
	 */
	public static String getDefaultOrderBy(Class<? extends BaseTable> tableClass) {
		TableCache cache = getSingleton().tableCaches.get(tableClass);
		if (cache == null) {
			return null;
		} else {
			return cache.getOrderBy();
		}
	}

	private static void saveDefaultOrderBy(
			Class<? extends BaseTable> tableClass, String defaultOrderBy) {
		Tables mappingCache = getSingleton();
		TableCache content = mappingCache.tableCaches.get(tableClass);
		if (content == null) {
			content = new TableCache();
			content.setOrderBy(defaultOrderBy);
			mappingCache.tableCaches.put(tableClass, content);
		} else {
			content.setOrderBy(defaultOrderBy);
		}
	}
}
