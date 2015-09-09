
package org.sxb.kit.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.sxb.kit.HashKit;


//import java.util.regex.*;




/**
 *  日付を表現するクラスです。
 *  
 *  不変クラスです。
 *  パフォーマンスを重視した日付演算を行いたい場合、Calendarクラスを経由してください。
 */
public final class Datetime implements Comparable<Datetime>, TimePoint, java.io.Serializable {
	
	private static final long serialVersionUID = 8068329320462032593L;
	
	public static final Datetime ZERO_EPOCH = new Datetime(1970, 1, 1, 0, 0, 0);	// ゼロエポック時
	public static final Datetime MIN_VALUE = new Datetime(Long.MIN_VALUE);		// 最小値
	
	private long _millis;
	
	public Datetime(long timeInMillis){
		
		Calendar datetime = Calendar.getInstance();
		datetime.clear();
		datetime.setTimeInMillis(timeInMillis);
		
		_millis = datetime.getTimeInMillis();
	}
	
	public Datetime(int year, int month, int day, int hour, int minute){
		this(year, month, day, hour, minute, 0);
	}
	
	public Datetime(int year, int month, int day, int hour, int minute, int second){
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month-1, day, hour, minute, second);
		
		_millis = cal.getTimeInMillis();
	}
	
	
	/**
	 * 日付と時刻を合成して日付時刻を作成します
	 * @param datePart 日付部分
	 * @param timePart 時刻部分
	 */
	public Datetime(Date datePart,Time timePart){
		Calendar date = datePart.toCalendar();
		Calendar time = timePart.toCalendar();
		
		Calendar datetime = Calendar.getInstance();
		datetime.setLenient(false);
		datetime.clear();
		datetime.set(Calendar.ERA, date.get(Calendar.ERA));
		datetime.set(
			date.get(Calendar.YEAR),
			date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH),
			time.get(Calendar.HOUR_OF_DAY),
			time.get(Calendar.MINUTE),
			time.get(Calendar.SECOND)
		);
		
		_millis = datetime.getTimeInMillis();
	}
	
	/**
	 * SimpleDateFormat形式 yyyy/MM/dd HH:mm を返します
	 * @see java.text.SimpleDateFormat
	 */
	public String toString(){
		return format("yyyy/MM/dd HH:mm");
	}
	
	// ▼比較▼ -----------------------------------------------
	public boolean beforeEquals(Datetime another){
		return before(another) || equals(another);
	}
	
	public boolean before(Datetime another){
		return _millis < another.getTimeInMillis();
	}
	
	public boolean after(Datetime another){
		return _millis > another.getTimeInMillis();
	}
	
	/**
	 *  このクラスと比較対象のxb.util.Datetime が同じ日付時刻を表すときのみ
	 * trueを返します。
	 */
	public boolean equals(Object obj){
		
		if(!(obj instanceof Datetime)) return false;
		
		Datetime another = (Datetime) obj;
		return _millis == another.getTimeInMillis();
		
	}
	
	public int hashCode(){
		// equals の実装に合わせてオーバーライド
		int result = 17;
		result = 37 * result + HashKit.calc(_millis);
		return result;
	}
	
	public int compareTo(Datetime another) {
		long m1 = _millis;
		long m2 = another.getTimeInMillis();
		return (m1 < m2 ? -1 : (m2 < m1 ? 1 : 0));
	}
	
	
	// ▲比較▲ ------------------------------------------------
	
	
	// ▼演算▼ -----------------------------------------------
// 	/** 
// 	 * [試験中メソッド]
// 	 * 時間を加算した新しいインスタンスを返します。
// 	 */
//	@InProgress
//	public xb.util.Datetime plus(Interval interval){
//		return new xb.util.Datetime(this.getTimeInMillis() + interval.inMillis());
//	}
//	
// 	/** 
// 	 * [試験中メソッド]
// 	 * 時間を減算した新しいインスタンスを返します。
// 	 */
//	@InProgress
//	public xb.util.Datetime minus(Interval interval){
//		return new xb.util.Datetime(this.getTimeInMillis() - interval.inMillis());
//	}
//	
// 	/** 
// 	 * [試験中メソッド]
// 	 * 引数の時刻からこの時刻までの時間を返します
// 	 */
//	@InProgress
//	public xb.util.Interval minus(xb.util.Datetime another){
//		return Interval.millis(this.getTimeInMillis() - another.getTimeInMillis() );
//		}
	
	/** 
	 * [試験中メソッド]
	 * 時間を加算した新しいインスタンスを返します。
	 */
	public Datetime plus(long interval){
		return new Datetime(this.getTimeInMillis() + interval);
	}
	
	/** 
	 * [試験中メソッド]
	 * 時間を減算した新しいインスタンスを返します。
	 */
	
	public Datetime minus(long interval){
		return new Datetime(this.getTimeInMillis() - interval);
	}
	
	/** 
	 * [試験中メソッド]
	 * 引数の時刻からこの時刻までの時間を返します
	 */
	
	public long minus(Datetime another){
		return getTimeInMillis() - another.getTimeInMillis();
	}
	
	/** 
	 * [試験中メソッド]
	 * 	この日付時刻の0:00 から 対象の日付時刻の0:00までの日付時刻の不変リストを返します
	 * 	(例)
	 *		new Datetime(2007, 11, 7, 11, 30).daysDuring(new Datetime(2007, 11, 9, 16, 18))
	 *			contains
	 *				new Datetime(2007, 11, 7, 0, 0)
	 *				new Datetime(2007, 11, 8, 0, 0)
	 *				new Datetime(2007, 11, 9, 0, 0)
	 */
	
	public List<Datetime> daysDuring(Datetime another){
		return null;
	}
	// ▲演算▲ -------------------------------------------------
	
	
	// ▼型変換▼ -----------------------------------------------
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
	
	/** 時刻値をミリ秒で返します */
	public long getTimeInMillis(){
		return _millis;
	}
	
	/** 日付部分を返します */
	public Date datePart(){
		return new Date(_millis);
	}
	
	/** 時刻部分を返します */
	public Time timePart(){
		return new Time(_millis);
	}
	
	public final int getYear(){
		return datePart().getYear();
	}
	
	public final int getMonth(){
		return datePart().getMonth();
	}
	
	public final int getDay(){
		return datePart().getDay();
	}
		
	/** 曜日を返します */
	public final WeekDay getWeekDay(){
		return datePart().getWeekDay();
	}
	
	/** 曜日の日本語名を返します */
	public final String getWeekDayJp(){
		return datePart().getWeekDayJp();
	}
	
	public final int getHour(){
		return timePart().getHour();
	}
	
	public final int getMinute(){
		return timePart().getMinute();
	}
	
	public final int getSecond(){
		return timePart().getSecond();
	}
	
	public java.sql.Timestamp toSqlTimestamp(){
		return new java.sql.Timestamp(_millis);
	}
	// ▲型変換▲ -----------------------------------------------
	
	
	/**
	 * SimpleDateFormatを使用して整形された文字列を返します
	 * @param pattern パターン文字列
	 * @see java.text.SimpleDateFormat
	 */
	public String format(String pattern){
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(toSqlTimestamp());
	}
	
	public String format(String pattern, Locale locale){
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		return formatter.format(toSqlTimestamp());
	}
	
	// --------------------------------------------------------------------
	// creation method
	// --------------------------------------------------------------------
	// 現在の日付時刻を返す
	public static Datetime currentDatetime(){
		return new Datetime(System.currentTimeMillis());
	}
	
	/**<pre>
	 * currentDateのシノニム
	 * 今日の日付を返します。
	 *</pre>
	 */
	public static Datetime now(){
		return currentDatetime();
	}
	
	/**<pre>
	 * 文字列からテキストを解析して Datetime を生成します。
	 * @param pattern - 日付と時刻のフォーマットを記述するパターン
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 *</pre>
	 */
	public static Datetime parse(String pattern, String text) /*throws ParseException*/ {
		try{
			SimpleDateFormat parser = new SimpleDateFormat(pattern);
			java.util.Date javaDate = parser.parse(text);
			return new Datetime(javaDate.getTime());
		}catch(ParseException e){
			return null;
		}
	}
	
	
	/**<pre>
	 * デフォルトのパターンを使用して文字列からDatetime を生成します。
	 * 解析に失敗した場合はnullを返します。
	 *
	 * 現在は下記のフォーマットに対応しています。
	 * 	"yyyy-MM-ddTHH:mm:ss"
	 * 	"yyyy-MM-dd HH:mm:ss"
	 * 	"yyyy-MM-dd HH:mm"
	 * 	"yyyy/MM/ddTHH:mm:ss"
	 * 	"yyyy/MM/dd HH:mm:ss"
	 * 	"yyyyMMddHHmmss"
	 * 	"yyyy/MM/dd HH:mm"
	 *
	 * 解析に厳密な方法を用いています。存在しない日付の解析は失敗します。
	 *</pre>
	 *
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 */
	public static Datetime parse(String text){
		
		if(text == null) return null;
		
		String[] patterns = {
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm",
			"yyyy/MM/dd'T'HH:mm:ss",
			"yyyy/MM/dd HH:mm:ss",
			"yyyyMMddHHmmss",
			"yyyy/MM/dd HH:mm"
		};
		
		// 全パターンを試行する
		for(String pattern: patterns){
			
			//try{
				Datetime datetime = parse(pattern, text);
				if(datetime != null){
					return datetime;
				}
				
			//}catch(ParseException e){
				// 次のパターンを試す
				// nop
			//}
		}
		
		// 解析に失敗した場合はnullを返す。
		return null;
	}
	
	// --------------------------------------------------------------------
	//  static method
	// --------------------------------------------------------------------
	/**
	 * このメソッドは今のところフレームワーク内からのみ呼び出されることを前提としています.
	 * その日付が本当に存在するときtrueを返します
	 */
	public static boolean isDatetimeValid(int year, int month, int day, int hour, int minute, int second){
		
		Calendar cal = Calendar.getInstance();
		
		cal.setLenient(false);	// 厳密なチェックを行うように設定
		cal.clear();
		cal.set(year, month-1, day, hour, minute, second);
		
		try {
			// 設定した日付が存在しない場合
			//getTimeはIllegalArgumentExceptionをthrowする
			cal.getTime();
			return true;
			
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	
	// --------------------------------------------------------------------
	//  演算
	// --------------------------------------------------------------------
	
	public Datetime addYear(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.YEAR, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	public Datetime addMonth(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.MONTH, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	/**<pre>
	 * 日を加算 （マイナスで減算）
	 * 加算後の日付を表す新しいインスタンスを返します。(元のインスタンスは変化しない)
	 *
	 *</pre>
	 */
	public Datetime addDay(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.DATE, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	public Datetime addHour(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.HOUR, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	public Datetime addMinute(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.MINUTE, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	public Datetime addSecond(int number){
		Calendar cal = toCalendar();
		cal.add(Calendar.SECOND, number);
		return new Datetime(cal.getTimeInMillis());
	}
	
	public Datetime firstDatetimeOfMonth(){
		return new Datetime(getYear(), getMonth(), 1, getHour(), getMinute(), getSecond());
	}
	
	public Datetime lastDatetimeOfMonth(){
		return new Datetime(getYear(), getMonth(), lastDayOfMonth(), getHour(), getMinute(), getSecond());
	}
	
	private int lastDayOfMonth(){
		return toCalendar().getActualMaximum(Calendar.DATE);
	}
	
}
