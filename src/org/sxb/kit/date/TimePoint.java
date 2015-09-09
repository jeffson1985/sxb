
package org.sxb.kit.date;

/**
 * 時刻を表します。
 * 正確に表現すると、時間軸上のある一点です。
 * 
 * [演算ルール]
 * 	時間 + 時間 = 時間	# Interval.plus(Interval)   => Interval
 * 	時刻 + 時間 = 時刻	# TimePoint.plus(Interval)  => Interval
 * 	時刻 + 時刻 = [不可能]	# TimePoint.plus(TimePoint) => (Compile Error)
 */
public interface TimePoint/*<T extends TimePoint>*/ {
	
	/** 時刻値をミリ秒で返します */
	long getTimeInMillis();
	
	/** interval後の時刻を返します */
	//<T> T plus(Interval interval);
}
