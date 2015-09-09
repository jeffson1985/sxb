package org.sxb.kit.date;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 日付演算を定義します. インターフェースが固まるまではフレームワーク内のみでの使用となります。
 */
final class DateUtil {

	/** 日付の表示フォーマット */
	protected static final String DEFAULT_DATE_FORMAT = "yyyy'/'M'/'d";

	// close constructor
	private DateUtil() {
	}

	// --------------------------------------------------------------------
	// 日付演算
	// --------------------------------------------------------------------

	public static final int MONTH_PER_YEAR = 12;

	/**
	 * <pre>
	 * 日付に年を加算します.
	 * ex)
	 * 	incYear(new Date(2007, 11,  3),  1) => new Date(2008, 11,  3)
	 * 	incYear(new Date(2007, 11,  3),  2) => new Date(2009, 11,  3)
	 * 	incYear(new Date(2007, 11,  3),  3) => new Date(2010, 11,  3)
	 * 	incYear(new Date(2007, 12, 25), 10) => new Date(2017, 12, 25)
	 * 	incYear(NULL_DATE, 20)                      => NULL_DATE
	 * </pre>
	 */
	public static final Date incYear(Date date, int deltaYear) {

		if (eqv(date, NULL_DATE)) {
			return NULL_DATE;
		}

		Calendar cal = date.toCalendar();
		cal.setLenient(false); // 存在しない日付はエラーにする
		cal.add(Calendar.YEAR, deltaYear);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * <pre>
	 * 日付に月を加算します.
	 * ex)
	 * 	incMonth(new Date(2007, 11,  3), 1) => new Date(2007, 12,  3)
	 * 	incMonth(new Date(2007, 11,  3), 2) => new Date(2008,  1,  3)
	 * 	incMonth(new Date(2007, 11,  3), 3) => new Date(2008,  2,  3)
	 * 	incMonth(new Date(2007, 12, 25), 1) => new Date(2008,  1, 25)
	 * 	incMonth(new Date(2007, 12, 25), 2) => new Date(2008,  2, 25)
	 * incMonth((NULL_DATE, 20))                   => NULL_DATE
	 * </pre>
	 */
	public static final Date incMonth(Date base, int deltaMonth) {

		if (eqv(base, NULL_DATE)) {
			return NULL_DATE;
		}

		Calendar cal = base.toCalendar();
		cal.setLenient(false); // 存在しない日付はエラーにする
		cal.add(Calendar.MONTH, deltaMonth);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * <pre>
	 * 日付に日を加算します.
	 * ex)
	 * incDay((NULL_DATE, 20))                   => NULL_DATE
	 * </pre>
	 */
	public static final Date incDay(Date base, int deltaDay) {

		if (eqv(base, NULL_DATE)) {
			return NULL_DATE;
		}

		Calendar cal = base.toCalendar();
		cal.setLenient(false); // 存在しない日付はエラーにする
		cal.add(Calendar.DAY_OF_MONTH, deltaDay);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * <pre>
	 * ある日付の日のみ変更したものを返します.
	 * ex)
	 * 	sameMonthOf(new Date(2007, 8, 25), 10)
	 * 		=> new Date(2007, 8, 10)
	 * </pre>
	 */
	public static final Date sameMonthOf(Date base, int day) {
		Calendar baseCal = base.toCalendar();

		Calendar cal = Calendar.getInstance();
		cal.setLenient(false); // 存在しない日付はエラーにする
		cal.clear();
		cal.set(baseCal.get(Calendar.YEAR), baseCal.get(Calendar.MONTH), day);
		return new Date(cal.getTimeInMillis());
	}

	// 無効な日
	private static final Date NULL_DATE = new Date(9999, 1, 1);

	static boolean eqv(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		return o1.equals(o2);
	}

	/**
	 * <pre>
	 * その月の末日を返します.
	 * 
	 * lastDayOfMonth(new Date(2007, 2, 1)) => new Date(2007,  2, 28)
	 * </pre>
	 */
	public static Date lastDayOfMonth(Date base) {
		Calendar cal = base.toCalendar();
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return sameMonthOf(base, lastDay);
	}

	// --------------------------------------------------------------------
	// 仕様テスト
	// --------------------------------------------------------------------
	// テスト実行
	// 仕様通りに動いていない項目に対してログが出力されます
	// tail -f /work/app/tomcat/logs/catalina.out.yyMMdd | grep mtk
	public static void test() throws Exception {
		log(" --- Calc test start ---");

		// 日付
		assertEquals("eq   month", new Date(2000, 9, 6), new Date(2000, 9, 6));

		assertEquals("incYear1", incYear(new Date(2007, 11, 3), 1), new Date(
				2008, 11, 3));
		assertEquals("incYear2", incYear(new Date(2007, 11, 3), 2), new Date(
				2009, 11, 3));
		assertEquals("incYear3", incYear(new Date(2007, 11, 3), 3), new Date(
				2010, 11, 3));
		assertEquals("incYear4", incYear(new Date(2007, 12, 25), 10), new Date(
				2017, 12, 25));
		assertEquals("incYear5", incYear(new Date(2007, 12, 25), 20), new Date(
				2027, 12, 25));
		assertEquals("incYear6", incYear(NULL_DATE, 20), NULL_DATE);

		assertEquals("incMonth1", incMonth(new Date(2007, 11, 3), 1), new Date(
				2007, 12, 3));
		assertEquals("incMonth2", incMonth(new Date(2007, 11, 3), 2), new Date(
				2008, 1, 3));
		assertEquals("incMonth3", incMonth(new Date(2007, 11, 3), 3), new Date(
				2008, 2, 3));
		assertEquals("incMonth4", incMonth(new Date(2007, 12, 25), 1),
				new Date(2008, 1, 25));
		assertEquals("incMonth5", incMonth(new Date(2007, 12, 25), 2),
				new Date(2008, 2, 25));
		assertEquals("incMonth6", incMonth(NULL_DATE, 2), NULL_DATE);

		assertEquals("sameMonthOf1", sameMonthOf(new Date(2007, 8, 25), 10),
				new Date(2007, 8, 10));

		assertEquals("lastDayOfMonth1", lastDayOfMonth(new Date(2007, 2, 1)),
				new Date(2007, 2, 28));

		log(" --- Calc test end   ---");
	}

	public static Date max(Date... dates) {
		Date result = dates[0];
		for (Date date : dates) {
			if (result.before(date)) {
				result = date;
			}
		}
		return result;
	}

	public static Date min(Date... dates) {
		Date result = dates[0];
		for (Date date : dates) {
			if (date.before(result)) {
				result = date;
			}
		}
		return result;
	}

	private static void assertEquals(String message, Object actual,
			Object expected) {
		if (eqv(actual, expected)) {
			return;
		}

		// log("" + x.equals(y));
		log(message + " not Eq actual: " + actual + " expected: " + expected);
	}

	private static void log(String str) {
		BufferedReader lineReader = new BufferedReader(new StringReader(str));
		try {
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				System.out.println("mtk: " + line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				lineReader.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * 日付の文字列をもとに、Calendar型に変換します。
	 * 
	 * @param strDate
	 * @return
	 */
	public static Calendar dateToCalendar(String strDate) {
		return dateToCalendar(strDate.substring(0, 4), strDate.substring(4, 6),
				strDate.substring(6, 8), false);
	}

	/**
	 * 年月日の文字列をもとに、Calendar型に変換します。
	 * 
	 * @param strYear
	 * @param strMonth
	 * @param strDay
	 * @param b
	 * @return
	 */
	public static Calendar dateToCalendar(String strYear, String strMonth,
			String strDay, boolean b) {
		int year, month, day;
		try {
			year = Integer.parseInt(strYear);
			month = Integer.parseInt(strMonth) - 1;
			day = Integer.parseInt(strDay);
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		}
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.setLenient(b);
		cal.set(year, month, day, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * 年月日の文字列表現を取得します。
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static String formatDate(int year, int month, int day) {
		return formatDate(String.valueOf(year), String.valueOf(month),
				String.valueOf(day));
	}

	/**
	 * 年月日の文字列表現を取得します。
	 * 
	 * @param strYear
	 * @param strMonth
	 * @param strDay
	 * @return
	 */
	public static String formatDate(String strYear, String strMonth,
			String strDay) {
		Calendar cal = dateToCalendar(strYear, strMonth, strDay, false);
		if (cal == null) {
			return null;
		}
		try {
			cal.getTime();
		} catch (IllegalArgumentException e) {
			return null;
		}
		return formatCalendarDate(cal);
	}

	/**
	 * Calendar型を文字列の日付に変換します。
	 * 
	 * @param cal
	 * @return
	 */
	public static String formatCalendarDate(Calendar cal) {
		return getStringYear(cal) + getStringMonth(cal) + getStringDay(cal);
	}

	/**
	 * 年の文字列表現を取得します。
	 * 
	 * @param cal
	 * @return
	 */
	public static String getStringYear(Calendar cal) {
		Object[] args = new Object[1];
		args[0] = Integer.valueOf(cal.get(Calendar.YEAR));
		return MessageFormat.format("{0,number,0000}", args);
	}

	/**
	 * 月の文字列表現を取得します。
	 * 
	 * @param cal
	 * @return
	 */
	public static String getStringMonth(Calendar cal) {
		return getStringMonth(cal.get(Calendar.MONTH));
	}

	/**
	 * 月の文字列表現を取得します。
	 * 
	 * @param month
	 * @return
	 */
	public static String getStringMonth(int month) {
		switch (month) {
		case Calendar.JANUARY:
			return "01";
		case Calendar.FEBRUARY:
			return "02";
		case Calendar.MARCH:
			return "03";
		case Calendar.APRIL:
			return "04";
		case Calendar.MAY:
			return "05";
		case Calendar.JUNE:
			return "06";
		case Calendar.JULY:
			return "07";
		case Calendar.AUGUST:
			return "08";
		case Calendar.SEPTEMBER:
			return "09";
		case Calendar.OCTOBER:
			return "10";
		case Calendar.NOVEMBER:
			return "11";
		case Calendar.DECEMBER:
			return "12";
		default:
			return null;
		}
	}

	/**
	 * 日の文字列表現を取得します。
	 * 
	 * @param cal
	 * @return
	 */
	public static String getStringDay(Calendar cal) {
		Object[] args = new Object[1];
		args[0] = Integer.valueOf(cal.get(Calendar.DATE));
		return MessageFormat.format("{0,number,00}", args);
	}

	/**
	 * 曜日の文字列表現を取得します。
	 * 
	 * @param cal
	 * @return
	 */
	public static String getDayOfWeek(Calendar cal) {
		String res = "";

		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			res = "日曜日";
			break;
		case Calendar.MONDAY:
			res = "月曜日";
			break;
		case Calendar.TUESDAY:
			res = "火曜日";
			break;
		case Calendar.WEDNESDAY:
			res = "水曜日";
			break;
		case Calendar.THURSDAY:
			res = "木曜日";
			break;
		case Calendar.FRIDAY:
			res = "金曜日";
			break;
		case Calendar.SATURDAY:
			res = "土曜日";
			break;
		default:
			return null;
		}
		return res;
	}

	/**
	 * 日付の文字列表現を取得します。
	 * 
	 * @param date
	 * @return
	 */
	public static String format(java.util.Date date) {
		return format(date, null);
	}

	/**
	 * 日付の文字列表現を取得します。
	 * 
	 * @param date
	 * @param strFormat
	 * @return
	 */
	public static String format(java.util.Date date, String strFormat) {
		String formatStr = null;
		int year;
		int month;
		int day;

		if (date == null) {
			return "";
		}
		if (strFormat == null) {
			strFormat = DEFAULT_DATE_FORMAT;
		}

		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH) + 1;
			day = calendar.get(Calendar.DATE);
		} catch (Throwable ex) {
			year = 0;
			month = 0;
			day = 0;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day);

		try {
			calendar.setLenient(false);
			formatStr = formatter.format(calendar.getTime());
		} catch (Exception ex) {
			formatStr = "";
		}
		return formatStr;
	}

	/**
	 * Generates a human representation of distance in time between two time
	 * stamps. This could take a form: "less than a minute", or "about a year".
	 *
	 * @param fromTime
	 *            start timestamp. This is a representation of time in
	 *            milliseconds from January 1 1970.
	 * @param toTime
	 *            end timestamp. This is a representation of time in
	 *            milliseconds from January 1 1970.
	 * @return human representation if distance in time between
	 *         <code>fromTime</code> and <code>toTime</code>.
	 *
	 */
	public static String toHumanFormat(long fromTime, long toTime) {
		if (!(toTime >= fromTime)) {
			throw new IllegalArgumentException("toTime must be >= fromTime");
		}

		long distanceInSeconds = Math.round((toTime - fromTime) / 1000);
		long distanceInMinutes = Math.round(distanceInSeconds / 60);

		if (distanceInMinutes == 0) {
			return "less than a minute";
		}
		if (distanceInMinutes == 1) {
			return "a minute";
		}
		if (inRange(2, 44, distanceInMinutes)) {
			return distanceInMinutes + " minutes";
		}
		if (inRange(45, 89, distanceInMinutes)) {
			return "about 1 hour";
		}
		if (inRange(90, 1439, distanceInMinutes)) {
			return "about " + Math.round(distanceInMinutes / 60f) + " hours";
		}
		if (inRange(1440, 2879, distanceInMinutes)) {
			return "1 day";
		}
		if (inRange(2880, 43199, distanceInMinutes)) {
			return Math.round(distanceInMinutes / 1440f) + " days";
		}
		if (inRange(43200, 86399, distanceInMinutes)) {
			return "about 1 month";
		}
		if (inRange(86400, 525599, distanceInMinutes)) {
			return Math.round(distanceInMinutes / 43200f) + " months";
		}
		if (inRange(525600, 1051199, distanceInMinutes)) {
			return "about 1 year";
		}

		return "about " + Math.round(distanceInMinutes / 525600f) + " years";
	}

	/**
	 * This is a convenience method in addition to
	 * {@link #toHumanFormat(long, long)}, except the second parameter is always
	 * now.
	 *
	 * @param fromTime
	 *            start date. This is a representation of time in milliseconds
	 *            from January 1 1970.
	 * @return human imprecise representation of time difference between the
	 *         <code>fromTime</code> and now.
	 */
	public static String toHumanFormat(long fromTime) {
		return toHumanFormat(fromTime, new java.util.Date().getTime());
	}

	/**
	 * Returns <code>true</code> if the <code>val</code> is between
	 * <code>min</code> and <code>max</code>, inclusively. Otherwise returns
	 * <code>false</code>. This is implemented because Java does not have native
	 * support for ranges.
	 *
	 * @param min
	 *            minimum range boundary
	 * @param max
	 *            maximum range boundary
	 * @param val
	 *            value in question.
	 * @return <code>true</code> if the <code>val</code> is between
	 *         <code>min</code> and <code>max</code>, inclusively. Otherwise
	 *         returns <code>false</code>.
	 */
	private static boolean inRange(long min, long max, long val) {
		return val >= min && val <= max;
	}

}
