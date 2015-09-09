
package org.sxb.kit.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sxb.kit.HashKit;


/**
 * <pre>
 *   日付を表現するクラスです。
 *  
 *   不変クラスです。
 *   どのような操作を行ってもオブジェクトの状態が変化しないため、
 *  エイリアシングなどの問題に煩わされることなく使用できます。
 * 
 *  また下記のように反復を簡潔に行うためのAPIをサポートしています。
 * 
 * (例)
 *	// 今月のすべての日に対して処理を行う例
 *	Date currentDate = Date.now();
 *	Date startDate = currentDate.firstDateOfMonth();
 *	Date endDate = currentDate.lastDateOfMonth();
 * 	for(Date today: startDate.upto(endDate)){
 * 		// -- todayを使った処理 -- //
 * 	}
 *
 * 
 *</pre>
 */
public final class Date implements Comparable<Date>, java.io.Serializable{

	private static final long serialVersionUID = 7851540751942071031L;
	/*
	public static final Date MIN_VALUE;
	public static final Date MAX_VALUE;
	
	static {
		GregorianCalendar  min = new GregorianCalendar();
		min.clear();
		min.setLenient(false);
		min.set(GregorianCalendar.ERA, 		GregorianCalendar.BC);
		min.set(GregorianCalendar.YEAR, 	min.getActualMaximum(GregorianCalendar.YEAR));
		min.set(GregorianCalendar.MONTH, 	min.getActualMinimum(GregorianCalendar.MONTH));
		min.set(GregorianCalendar.DATE, 	min.getActualMinimum(GregorianCalendar.DATE));
		MIN_VALUE = new Date(min.getTimeInMillis());
		
		GregorianCalendar  max = new GregorianCalendar();
		max.clear();
		max.setLenient(false);
		max.set(GregorianCalendar.ERA, 		GregorianCalendar.AD);
		max.set(GregorianCalendar.YEAR, 	max.getActualMaximum(GregorianCalendar.YEAR));
		MAX_VALUE = new Date(max.getTimeInMillis());
	}*/
	
	// 曜日の変換テーブル(int -> WeekDay)
	private static final Map<Integer, WeekDay> WEEK_DAY_TABLE;
	static {
		Map<Integer, WeekDay> m = new HashMap<Integer, WeekDay>();
		m.put(Calendar.SUNDAY,    WeekDay.SUNDAY);
		m.put(Calendar.MONDAY,    WeekDay.MONDAY);
		m.put(Calendar.TUESDAY,   WeekDay.TUESDAY);
		m.put(Calendar.WEDNESDAY, WeekDay.WEDNESDAY);
		m.put(Calendar.THURSDAY,  WeekDay.THURSDAY);
		m.put(Calendar.FRIDAY,    WeekDay.FRIDAY);
		m.put(Calendar.SATURDAY,  WeekDay.SATURDAY);
		WEEK_DAY_TABLE = Collections.unmodifiableMap(m);
	}
	
	
	private final long _millis;
	
	public Date(int year, int month, int day){
		
		try{
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.setLenient(false);
			cal.set(year, month-1, day);
			
			_millis = cal.getTimeInMillis();
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(
				String.format("year=%s, month=%s, day=%s", year, month, day)
				,e
			);
		}
	}
	
	
	public Date(long timeInMillis){
		
		// 日付 + 時刻から日付情報のみを取り出す
		Calendar datetime = Calendar.getInstance();
		datetime.clear();
		datetime.setLenient(false);
		datetime.setTimeInMillis(timeInMillis);
		
		Calendar date = Calendar.getInstance();
		date.clear();
		date.setLenient(false);
		date.set(Calendar.ERA, datetime.get(Calendar.ERA));
		date.set(datetime.get(Calendar.YEAR),
			 datetime.get(Calendar.MONTH),
			 datetime.get(Calendar.DAY_OF_MONTH)
		);
		
		_millis = date.getTimeInMillis();
		
	}
	
	// このオブジェクトの翌日にあたる日付を返します
	public Date nextDay(){
		return addDay(1);
	}
	
	public Date previousDay(){
		return addDay(-1);
	}
	
	public String toString(){
		return format("yyyy/MM/dd");
	}
	
	// ▼比較▼ -----------------------------------------------
	
	public boolean before(Date another){
		return _millis < another.getTimeInMillis();
	}
	
	/**
	 *  このクラスと比較対象のDate が同じ日を表すときのみ
	 * trueを返します。
	 */
	public boolean equals(Object obj){
		
		if(!(obj instanceof Date)) return false;
		
		Date another = (Date) obj;
		return _millis == another.getTimeInMillis();
	}
	
	public boolean beforeEquals(Date another){
		return before(another) || equals(another);
	}
	
	public boolean after(Date another){
		return !beforeEquals(another);
	}
	
	public boolean afterEquals(Date another){
		return !before(another);
	}
	
	public int hashCode(){
		// equals の実装に合わせてオーバーライド
		int result = 17;
		result = 37 * result + HashKit.calc(_millis);
		return result;
	}
	
	public int compareTo(Date another) {
		long m1 = _millis;
		long m2 = another.getTimeInMillis();
		return (m1 < m2 ? -1 : (m2 < m1 ? 1 : 0));
	}
	
	// ▲比較▲ -----------------------------------------------
	
	
	// ▼差分▼ -----------------------------------------------
	/**
	 *　日付の差分（日数）を返します。比較するオブジェクトより過去ならマイナス。
	 */
	public int diffDate(Date another){
		long m1 = _millis;
		long m2 = another.getTimeInMillis();
		long d = m1 - m2;
		return (int)(d/1000/60/60/24);
	}
	
	/**<pre>
	 * このメソッドは未テストです。
	 *　月の差分（月数）を返します。比較するオブジェクトより過去ならマイナス。
	 *　(例)
	 *　	new Date(2008, 9,  1).diffMonth(new Date(2008, 8, 31) ->   1
	 *　	new Date(2008, 8, 31).diffMonth(new Date(2008, 9,  1) ->  -1
	 *　	new Date(2008, 8,  1).diffMonth(new Date(2008, 8, 31) ->   0
	 *　	new Date(2009, 8,  1).diffMonth(new Date(2008, 8,  1) ->  12
	 *
	 *</pre>
	 */
	public int diffMonth(Date another){
		
		// 1日同士にして比較
		Date start = firstDateOfMonth();
		Date end   = another.firstDateOfMonth();
		
		// 結果が-になる場合は処理をわける
		if(end.before(start)){
			return - another.diffMonth(this);
		}
		
		int count = 0;
		Date cntDate = start;
		while(cntDate.before(end)){
			count++;
			cntDate = cntDate.addMonth(1);
		}
		return -count;
	}
	
	// ▲差分▲ -----------------------------------------------
	
	
	
	//▼型変換▼ -----------------------------------------------
	/**
	 *   カレンダー型に変換します。
	 *   取得したカレンダーを変更しても、
	 *  このクラスは変化しません。
	 */
	public Calendar toCalendar(){
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTimeInMillis(_millis);
		return cal;
	}
	
	// DBConnector からの呼び出し専用
	public java.sql.Date toSqlDate(){
		return new java.sql.Date(_millis);
	}
	
	public long getTimeInMillis(){
		return _millis;
	}
	
	public final int getYear(){
		return toCalendar().get(Calendar.YEAR);
	}
	
	public final int getMonth(){
		return toCalendar().get(Calendar.MONTH) + 1;
	}
	
	public final int getDay(){
		return toCalendar().get(Calendar.DAY_OF_MONTH);
	}
		
	/** 曜日を返します */
	public final WeekDay getWeekDay(){
		return WEEK_DAY_TABLE.get(toCalendar().get(Calendar.DAY_OF_WEEK));
	}
	
	/** 曜日の日本語名を返します */
	public final String getWeekDayJp(){
		return getWeekDay().toJapanese();
	}
	
	// ▲型変換▲ -----------------------------------------------
	
	public String format(String pattern){
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(toSqlDate());
	}
	
	public String format(String pattern, Locale locale){
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		return formatter.format(toSqlDate());
	}
	
	// --------------------------------------------------------------------
	//  static method
	// --------------------------------------------------------------------
	
	/**<pre>
	 * 今日の日付を返します。
	 *</pre>
	 */
	public static Date currentDate(){
		return new Date(System.currentTimeMillis());
	}
	
	/**<pre>
	 * currentDateのシノニム
	 * 今日の日付を返します。
	 *</pre>
	 */
	public static Date now(){
		return currentDate();
	}
	
	/**<pre>
	 * 文字列からテキストを解析して Date を生成します。
	 * 解析に失敗した場合はnullを返します。
	 * @param pattern - 日付と時刻のフォーマットを記述するパターン
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 *</pre>
	 */
	 /*
	 public static Date parse(String pattern, String text) {
		if(text == null) return null;
		try{
			SimpleDateFormat parser = new SimpleDateFormat(pattern);
			parser.setLenient(false);
			java.util.Date javaDate = parser.parse(text);
			return new Date(javaDate.getTime());
		}catch(ParseException e){
			return null;
		}
	}
	*/
	
	/**<pre>
	 * 文字列からテキストを解析して Date を生成します。
	 * 解析に失敗した場合はParseExceptionを投げます。
	 * @param pattern - 日付と時刻のフォーマットを記述するパターン
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 *</pre>
	 */
	public static Date parse(String pattern, String text) throws ParseException {
		SimpleDateFormat parser = new SimpleDateFormat(pattern);
		parser.setLenient(false);
		java.util.Date javaDate = parser.parse(text);
		return new Date(javaDate.getTime());
	}
	
	/**<pre>
	 * デフォルトのパターンを使用して文字列からDate を生成します。
	 * 解析に失敗した場合はnullを返します。
	 *
	 * 現在は下記のフォーマットに対応しています。
	 * 	"yyyy/MM/dd", "yyyy.MM.dd", "yyyy-MM-dd", "yyyyMMdd",
	 * 	"yyyy年MM月dd日", "yyyy/M/d", "yyyy.M.d", "yyyy年M月d日"
	 *
	 * 解析に厳密な方法を用いています。例えば下記のような存在しない日付の解析は失敗します。
	 * 	"2008/08/50"
	 *</pre>
	 *
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 */
	public static Date parse(String text){
		
		if(text == null) return null;
		
		String[] patterns = {
			"yyyy/MM/dd",
			"yyyy.MM.dd",
			"yyyy-MM-dd",
			"yyyyMMdd",
			"yyyy年MM月dd日",
			"yyyy/M/d",
			"yyyy.M.d",
			"yyyy-M-d",
			"yyyy年M月d日",
		};
		
		// 全パターンを試行する
		for(String pattern: patterns){
			
			try{
				return parse(pattern, text);
			}catch(ParseException e){
				// 次のパターンを試す
				// nop
			}
		}
		
		// 解析に失敗した場合はnullを返す。
		return null;
	}
	
	public static boolean canParse(String text){
		return parse(text) != null;
	}
	
	
	public static boolean canParse(String pattern, String text){
		if(text == null) return false;
		try{
			parse(pattern, text);
			return true;
		}catch(ParseException e){
			return false;
		}
	}
	
	/**<pre>
	 * このメソッドは今のところフレームワーク内からのみ呼び出されることを前提としています.
	 * その日付が本当に存在するときtrueを返します
	 *</pre>
	 */
	public static boolean isDateValid(int year, int month, int day){
		
		Calendar cal = Calendar.getInstance();
		
		cal.setLenient(false);	// 厳密なチェックを行うように設定
		cal.clear();
		cal.set(year, month - 1, day);
		
		try {
			// 設定した日付が存在しない場合
			//getTimeはIllegalArgumentExceptionをthrowする
			cal.getTime();
			return true;
			
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	/**<pre>
	 * この月の最初の日を返します。
	 * 例：2008.8.10 →　2008.8.1
	 *</pre>
	 */
	public Date firstDateOfMonth(){
		return new Date(getYear(), getMonth(), 1);
	}
	
	/**<pre>
	 * この月の最後の日を返します。
	 * 例：2008.8.10 →　2008.8.31
	 *</pre>
	 */
	public Date lastDateOfMonth(){
		return new Date(getYear(), getMonth(), lastDayOfMonth());
	}
	
	private int lastDayOfMonth(){
		return toCalendar().getActualMaximum(Calendar.DATE);
	}
	
	/**<pre>
	 * この年の最初の日を返します。
	 * 例：2008.8.10 →　2008.1.1
	 *</pre>
	 */
	public Date firstDateOfYear(){
		return new Date(getYear(), 1, 1);
	}
	 
	/**<pre>
	 * この年の最後の日を返します。
	 * 例：2008.8.10 →　2008.12.31
	 *</pre>
	 */
	public Date lastDateOfYear(){
		return new Date(getYear(), 12, 31);
	}
	
	/**<pre>
	 * 日を加算 （マイナスで減算）
	 * 加算後の日付を表す新しいインスタンスを返します。(元のインスタンスは変化しない)
	 *
	 *</pre>
	 */
	public Date addDay(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.DATE, number);
		return new Date(cal.getTimeInMillis());
	}
	
	/**<pre>
	 *  月を加算 （マイナスで減算）
	 *  加算後の日付を表す新しいインスタンスを返します。(元のインスタンスは変化しない)
	 *  単純に月を加算した日付が暦上存在しない場合、日を小さくします。
	 *
	 *	(例)
	 * 		new Date(2007, 3, 31).addMonth( 1).equals(new Date(2007, 4, 30)) -> true
	 * 		new Date(2007, 5, 31).addMonth(-1).equals(new Date(2007, 4, 30)) -> true
	 *
	 * [使用上の注意]
	 * 	この演算では結合法則などは<b>満たされません</b>
	 * 	すなわち、下記のとおりです。
	 * 		date.addMonth(a).addMonth(b) !≡ date.addMonth(b).addMonth(a)
	 * 		date.addMonth(a).addMonth(b) !≡ date.addMonth(a + b)
	 * 		
	 * 		(例)
	 * 			Date date = new Date(2007, 12,31);
	 * 			date.addMonth(12))		-> 2008/12/31
	 * 			date.addMonth(2).addMonth(10)	-> 2008/12/29
	 * 			date.addMonth(10).addMonth(2)	-> 2008/12/31
	 *</pre>
	 */
	public Date addMonth(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.MONTH, number);
		return new Date(cal.getTimeInMillis());
	}
	
	/**<pre>
	 *  年を加算 （マイナスで減算）
	 *  加算後の日付を表す新しいインスタンスを返します。(元のインスタンスは変化しない)
	 *  単純に年フィールドを加算した日付が暦上存在しない場合、日フィールドを小さくします。
	 *	(例)
	 *		new Date(2008, 2, 29).addYear( 1).equals(new Date(2009, 2, 28)) -> true
	 *		new Date(2008, 2, 29).addYear(-1).equals(new Date(2007, 2, 28)) -> true
	 *
	 * [使用上の注意]
	 * 	この演算では結合法則などは<b>満たされません</b>
	 * 	すなわち、下記のとおりです。
	 * 		date.addYear(a).addYear(b) !≡ date.addYear(b).addYear(a)
	 * 		date.addYear(a).addYear(b) !≡ date.addYear(a + b)
	 * 		
	 * 		(例)
	 * 			// 1896(閏年), 1900(平年), 1904(閏年), 1908(閏年)
	 * 			Date date = new Date(1896, 2, 29);
	 * 			date.addYear(12)		-> 1908/02/29
	 * 			date.addYear(4).addYear(8)	-> 1908/02/28
	 * 			date.addYear(8).addYear(4)	-> 1908/02/29
	 *
	 *</pre>
	 */
	public Date addYear(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.YEAR, number);
		return new Date(cal.getTimeInMillis());
	}
	
// 	/**<pre>
//  	 * @deprecated
//	 * <b>このメソッドを使用する必要はありません。</b>
// 	 *
// 	 * 　このメソッドはこのオブジェクトのコピーを生成しますが、
// 	 * Dateクラスはバリューオブジェクトとして設計されているので、
// 	 * この操作が必要になるケースは存在しません。
// 	 * 
// 	 * 
// 	 * 参考として以前のバージョンのコメントを引用します。 (恐らく誤りです。)
// 	 * <BLOCKQUOTE>
// 	 * <s>この年の最初の日を返します。
// 	 * 例：2008.8.10 →　2008.1.1</s>
// 	 * </BLOCKQUOTE>
// 	 *
// 	 *</pre>
// 	 */
// 	 @Deprecated
//	 public Date copy(){
// 	 	Date d = new Date(_millis);
// 	 	return d;
// 	}
	
	// ▼範囲▼ -----------------------------------------------
	/**<pre>
	 * 
	 * この日から与えられた日までの範囲を返します
	 * 
	 * (例)
	 * 	// 今月の日について反復処理を行う
	 * 	Date currentDate = Date.now();
	 * 	Date start = currentDate.firstDateOfMonth();
	 * 	Date end   = currentDate.lastDateOfMonth();
	 * 
	 * 	for(Date today: start.upto(end)){
	 * 		// -- todayを使った処理 -- //
	 * 	}
	 * </pre>
	 */
	public Iterable<Date> upto(Date toDate){
		
		// memo
		// 　現在の実装では、初めに範囲内のリストを生成しているが、
		// 大きな範囲の使用が想定される場合に備え、
		// 遅延評価の使用を検討すること。
		final List<Date> days = new ArrayList<Date>();
		for(Date today = this;
				today.beforeEquals(toDate);
				today = today.nextDay()){
			days.add(today);
		}
		
		return new Iterable<Date>(){
			public Iterator<Date> iterator(){
				return days.iterator();
			}
		};
	}
	// ▲範囲▲ -----------------------------------------------
	
}
