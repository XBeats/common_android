package com.common.library.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import com.common.library.BuildConfig;
import com.common.library.utils.StyleStringBuilder;

/**
 *
 * Simple date time tools.
 *
 * @author  zhangfei
 */
public class DateTimeUtils {
	private static final String TAG = "DateTimeUtils";
	/**
	 * "yyyy-MM-dd HH:mm:sss"（2014-0607 11:15:083）
	 */
	public static final String FORMAT_STAND = "yyyy-MM-dd HH:mm:ss";

	/**
	 * "yyyy-MM-dd" (2013-04-23)
	 */
	private static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

	/**
	 * "EEEE"(星期一)
	 */
	private static final String FORMAT_WEEK = "EEEE";
	/**
	 * "EE"(周一)
	 */
	private static final String FORMAT_SHORT_WEEK = "EE";
	
	// date time units
	public static final long DAY_UNIT = 1000 * 60 * 60 * 24;
	public static final long HOUR_UNIT = 1000 * 60 * 60;
	public static final long MINUTE_UNIT = 1000 * 60;
	public static final long SECOND_UNIT = 1000;

	/**
	 * Convert datetime passed from stand datetime string into java.util.Date.
	 * 
	 * @param datetime
	 *            the datetime must use format {@link DateTimeUtils#FORMAT_STAND}
	 * @return java.util.Date parsed from String datetime
	 */
	public static Date parseFromStandDatetime(String datetime) {
		try {
			return new SimpleDateFormat(FORMAT_STAND, Locale.getDefault()).parse(datetime);
		} catch (ParseException e) {
			if (BuildConfig.DEBUG) {
				throw new RuntimeException("Cannot parse date from " + datetime + " with " + FORMAT_STAND);
			} else {
				Log.e(TAG, "Cannot parse date from " + datetime + " with " + FORMAT_STAND);
				return new Date();
			}
		}
	}

	/**
	 * Get week from stand datetime string
	 * 
	 * @param datetime
	 *            it must use format of {@link DateTimeUtils#FORMAT_WEEK}
	 * @return week of date
	 */
	public static String getWeekFromStandDatetime(String datetime) {
		Date date = parseFromStandDatetime(datetime);
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_WEEK, Locale.getDefault());
		return format.format(date);
	}

	/**
	 * Get week from stand datetime string
	 * 
	 * @param datetime
	 *            it must use format of {@link DateTimeUtils#FORMAT_SHORT_WEEK}
	 * @return week of date
	 */
	public static String getShortWeekFromStandDatetime(String datetime) {
		Date date = parseFromStandDatetime(datetime);
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_SHORT_WEEK, Locale.getDefault());
		return format.format(date);
	}
	
	/**
	 * Get week from {@link java.util.Date}, like '星期二'
	 * @param date {@link java.util.Date}
	 * @return week of date
	 */
	public static String getWeekFromDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_WEEK, Locale.getDefault());
		return format.format(date);
	}
	
	/**
	 * Get short week from {@link java.util.Date}, like '周二'
	 * @param date {@link java.util.Date}
	 * @return week of date
	 */
	public static String getShortWeekFromDate(Date date){
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_SHORT_WEEK, Locale.getDefault());
		return format.format(date);
	}
	
	/**
	 *  Format {@link java.util.Date} as stand datetime string.
	 * @param date {@link java.util.Date}
	 * @return datetime in format "yyyy-MM-dd HH:mm:sss"
	 */
	public static String formatDatetime(Date date){
		if(date == null){
			if(BuildConfig.DEBUG){
				throw new RuntimeException("Date to format is null");
			} else {
				date = new Date();
			}
		}
		return new SimpleDateFormat(FORMAT_STAND, Locale.getDefault()).format(date);
	}

	/**
	 * Format datetime to different kind style accrording to specified datetime
	 * unit. Formated datetime can be "2012年10月20日", "2012年10月20日 12点30分",
	 * "2012-09-14" and so on.
	 * 
	 * @param date
	 *            java.utils.Date
	 * @param unit
	 *            {@link DateSuffix}
	 * @return formated datetime
	 */
	public static String formatDatetime(Date date, DateSuffix unit) {
		if (unit == null) {
			throw new RuntimeException("No datetime unit supplied.");
		}
		return unit.toDateFormat().format(date);
	}

	/**
	 * Get date time difference between two time as String, like 2小时34分钟
	 */
	public static String getDateTimeBetweenDesc(Calendar cal1, Calendar cal2) {
		long gap = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 60);// 间隔分钟
		if (gap < 0) {
			gap = gap + 1000 * 60 * 60 * 24;
		}
		String str = "";
		if (gap >= 60) {
			long time1 = gap / 60;
			long time2 = gap % 60;
			if (time2 > 0) {
				str = time2 + "分钟";
			}
			str = time1 + "小时" + str;
		} else if (gap > 0) {
			str = gap + "分钟";
		}
		return str;
	}

	public static int getDateTimeBetweenDays(Calendar cal1, Calendar cal2) {
		long gap = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / DAY_UNIT;
		return (int) gap;
	}

	public static int getDateTimeBetweenDays2(Calendar cal1, Calendar cal2) {
		long cal2time = formatCalendarWithoutTime((Calendar) cal2.clone()).getTimeInMillis();
		long cal1time = formatCalendarWithoutTime((Calendar) cal1).getTimeInMillis();
		long gap = (cal2time - cal1time) / DAY_UNIT;// 从间隔毫秒变成间隔天数
		return (int) gap;
	}

	/**
	 * Get system current date as stand date format string.
	 */
	public static String getNowDateAsString() {
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_STAND, Locale.getDefault());
		Calendar c = Calendar.getInstance();
		String date = format.format(c.getTime());
		return date;
	}

	/**
	 * Reset calendar's time as zero, from 2014-03-08 22:12:34 to 2014-03-08 00:00:00
	 */
	public static Calendar formatCalendarWithoutTime(Calendar calendar){
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_YYYY_MM_DD, Locale.getDefault());
		Calendar c = (Calendar) calendar.clone();
		String dateString = format.format(c.getTime());
		try {
			c.setTime(format.parse(dateString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}
	

	/**
	 * Be familiar with {@link #formatTimeDuration(Context, String, String, long, int, int)},
	 * but without prefix and suffix and style.
	 */
	public static Spanned formatTimeDuration(Context context, long millisecond) {
		return formatTimeDuration(context, null, null, millisecond, -1, -1);
	}
	
	/**
	 * Be familiar with {@link #formatTimeDuration(Context, String, String, long, int, int)},
	 * but without prefix and suffix.
	 */
	public static Spanned formatTimeDuration(Context context, long millisecond, int numColor, int textColor) {
		return formatTimeDuration(context, null, null, millisecond, numColor, textColor);
	}

	/**
	 * Format time in millisecond as "01天01小时01分钟01秒", "01天00小时00分钟01秒",
	 * "01分钟00秒" and so on.
	 * 
	 * @param millisecond
	 * @return formated time 
	 */
	public static Spanned formatTimeDuration(Context context, String prefix, String suffix, long millisecond, int numColor, int textColor) {
		long days = millisecond / DAY_UNIT;
		long hours = millisecond / HOUR_UNIT - days * 24;
		long minutes = millisecond / MINUTE_UNIT - days * 24 * 60 - hours * 60;
		long second = millisecond / SECOND_UNIT - days * 24 * 60 * 60 - hours * 60 * 60 - minutes * 60;
		
		StyleStringBuilder build = new StyleStringBuilder(context);
		if(!TextUtils.isEmpty(prefix)){
			build.appendForegroundColorString(prefix, textColor);
		}
		
		boolean hasDays = false;
		if(days > 0){
			build.appendForegroundColorString(String.format(Locale.getDefault(), "%02d", days), numColor);
			build.appendForegroundColorString("天", textColor);
			hasDays = true;
		}
		
		boolean hasHours = false;
		if(hours > 0){
			build.appendForegroundColorString(String.format(Locale.getDefault(), "%02d", hours), numColor);
			build.appendForegroundColorString("小时", textColor);
			hasHours = true;
		}else if(hasDays){
			build.appendForegroundColorString("00", numColor);
			build.appendForegroundColorString("小时", textColor);
			hasHours = true;
		}
		
		boolean hasMinutes = false;
		if(minutes > 0){
			build.appendForegroundColorString(String.format(Locale.getDefault(), "%02d", minutes), numColor);
			build.appendForegroundColorString("分钟", textColor);
			hasMinutes = true;
		}else if(hasHours){
			build.appendForegroundColorString("00", numColor);
			build.appendForegroundColorString("分钟", numColor);
			hasMinutes = true;
		}
		
		if(second > 0){
			build.appendForegroundColorString(String.format("%02d", second), numColor);
			build.appendForegroundColorString("秒", numColor);
		}else if(hasMinutes){
			build.appendForegroundColorString("00", numColor);
			build.appendForegroundColorString("秒", numColor);
		}
		
		if(!TextUtils.isEmpty(suffix)){
			build.appendForegroundColorString(suffix, textColor);
		}
		return build.build();
	}
	
	public static Calendar clearTime(Calendar datetime){
		if (datetime == null){
			return null;
		}
		
		datetime.set(Calendar.HOUR_OF_DAY, 0);
        datetime.set(Calendar.MINUTE, 0);
        datetime.set(Calendar.SECOND, 0);
        datetime.set(Calendar.MILLISECOND, 0);
        return datetime;
	}
	
	public static Date clearTime(Date datetime){
		if (datetime == null){
			return  null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetime);
		cal = clearTime(cal);
		return cal.getTime();
	}
	
	public static String clearTime(String datetime){
		if (TextUtils.isEmpty(datetime)){
			return null;
		}
		
		Date date = parseFromStandDatetime(datetime);
		date = clearTime(date);
		return formatDatetime(date);
	}
	
	public static Calendar clearDate(Calendar datetime){
		if(datetime == null){
			return null;
		}
		
		datetime.set(Calendar.YEAR, 0);
		datetime.set(Calendar.MONTH, 0);
		datetime.set(Calendar.DAY_OF_MONTH, 0);
		return datetime;
	}
	
	public static Date clearDate(Date datetime){
		if(datetime == null){
			return null;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(datetime);
		calendar = clearDate(calendar);
		return calendar.getTime();
	}

	public static String clearDate(String dateTime){
		Date date = parseFromStandDatetime(dateTime);
		date = clearDate(date);
		return formatDatetime(date);
	}
}
