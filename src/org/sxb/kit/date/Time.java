
package org.sxb.kit.date;

import java.util.*;
import java.text.*;

import org.sxb.kit.HashKit;


/**<pre>
 *  時刻を表現するクラスです。
 *  (時・分・秒)
 *  
 *  不変クラスです。
 *  パフォーマンスを重視した時刻演算を行いたい場合、Calendarクラスを経由してください。
 *</pre>
 */
public final class Time implements Comparable<Time>, java.io.Serializable {
	
	private static final long serialVersionUID = -2238942452283076870L;
	
	// フィールドをクラスの外部へ露出させないこと!!
	private long _millis;
	
	
	public Time(int hour, int minute){
		this(hour, minute, 0);
	}
	
	public Time(int minute){
		
		// 時間を算出
		int hour = (int)(minute / 60);
		
		// 残り分を算出
		if(hour > 0){
			minute = minute - (hour * 60);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE,      minute);
		cal.set(Calendar.SECOND,      0);
		
		_millis = cal.getTimeInMillis();
		
	}
	
	public Time(int hour, int minute, int second){
		
		if(hour < 0 || 23 < hour
				|| minute < 0 || 59 < minute
				|| second < 0 || 59 < second){
			throw new IllegalArgumentException();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE,      minute);
		cal.set(Calendar.SECOND,      second);
		
		_millis = cal.getTimeInMillis();
	}
	
	public Time(long timeInMillis){
		
		// 日付 + 時間から時間情報のみを取り出す
		Calendar datetime = Calendar.getInstance();
		datetime.clear();
		datetime.setTimeInMillis(timeInMillis);
		
		Calendar time = Calendar.getInstance();
		time.clear();
		time.set(Calendar.HOUR_OF_DAY, datetime.get(Calendar.HOUR_OF_DAY));
		time.set(Calendar.MINUTE,       datetime.get(Calendar.MINUTE));
		time.set(Calendar.SECOND ,      datetime.get(Calendar.SECOND ));
		
		_millis = time.getTimeInMillis();
	}
	
	// ▼比較▼ -----------------------------------------------
	public boolean beforeEquals(Time another){
		return before(another) || equals(another);
	}
	
	public boolean before(Time another){
		return _millis < another.getTimeInMillis();
	}
	
	/**
	 *  このクラスと比較対象のxb.util.Date が同じ日を表すときのみ
	 * trueを返します。
	 */
	public boolean equals(Object obj){
		
		if(!(obj instanceof Time)) return false;
		
		Time another = (Time) obj;
		return _millis == another.getTimeInMillis();
	}
	
	public int hashCode(){
		// equals の実装に合わせてオーバーライド
		int result = 17;
		result = 37 * result + HashKit.calc(_millis);
		return result;
	}
	
	public int compareTo(Time another) {
		long m1 = _millis;
		long m2 = another.getTimeInMillis();
		return (m1 < m2 ? -1 : (m2 < m1 ? 1 : 0));
	}
	
	// ▲比較▲ -----------------------------------------------
	
	
	
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
	
	// DBConnector からの呼び出し専用
	public java.sql.Time toSqlTime(){
		return new java.sql.Time(_millis);
	}
	
	public long getTimeInMillis(){
		return _millis;
	}
	
	public final int getHour(){
		return toCalendar().get(Calendar.HOUR_OF_DAY);
	}
	
	public final int getMinute(){
		return toCalendar().get(Calendar.MINUTE);
	}
	
	public final int getMinutes(){
		return (toCalendar().get(Calendar.HOUR_OF_DAY) * 60) + toCalendar().get(Calendar.MINUTE);
	}
	
	public final int getSecond(){
		return toCalendar().get(Calendar.SECOND);
	}
	
	// ▲型変換▲ -----------------------------------------------
	
	
	// ▼印字形式▼ -----------------------------------------------
	public String toString(){
		return format("HH:mm");
	}
	
	public String format(String pattern){
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(toSqlTime());
	}
	
	// ▲印字形式▲ -----------------------------------------------
	
	/**<pre>
	 * 現在の時刻を返します。
	 *</pre>
	 */
	public static Time currentTime(){
		return new Time(System.currentTimeMillis());
	}
	
	/**<pre>
	 * currentTimeのシノニム
	 * 現在の時刻を返します。
	 *</pre>
	 */
	public static Time now(){
		return currentTime();
	}
	
	
	public static boolean canParse(String text) {
		return parse(text) != null;
	}
	
	/**<pre>
	 * 文字列からテキストを解析して Date を生成します。
	 * 解析失敗時にはnullを返します
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 *</pre>
	 */
	 public static Time parse(String text) {
		
		if(text == null) return null;
		
		String[] patterns = {
			"HH:mm:ss",
			"HH:mm",
			"HHmmss",
			"HHmm",
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
	
	/**<pre>
	 * 文字列からテキストを解析して Date を生成します。
	 * @param pattern - 日付と時刻のフォーマットを記述するパターン
	 * @param text    - 解析する日付/時刻文字列
	 * @see java.text.SimpleDateFormat
	 *</pre>
	 */
	 public static Time parse(String pattern, String text) throws ParseException {
		SimpleDateFormat parser = new SimpleDateFormat(pattern);
		java.util.Date javaDate = parser.parse(text);
		return new Time(javaDate.getTime());
	}
	
	/**<pre>
	 *  時を加算 （マイナスで減算）
	 *</pre>
	 */
	public Time addHour(int hour){
		Calendar cal = toCalendar();
		cal.add(Calendar.HOUR_OF_DAY, hour);
		return new Time(cal.getTimeInMillis());
	}
	
	/**<pre>
	 *  分を加算 （マイナスで減算）
	 *</pre>
	 */
	public Time addMinute(int minute){
		Calendar cal = toCalendar();
		cal.add(Calendar.MINUTE, minute);
		return new Time(cal.getTimeInMillis());
	}
}