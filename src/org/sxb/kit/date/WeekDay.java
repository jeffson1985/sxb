
package org.sxb.kit.date;

/** 曜日をあらわす定数 */
public enum WeekDay {
	
	/** 日曜日 */
	SUNDAY,
	
	/** 月曜日 */
	MONDAY,
	
	/** 火曜日 */
	TUESDAY,
	
	/** 水曜日 */
	WEDNESDAY,
	
	/** 木曜日 */
	THURSDAY,
	
	/** 金曜日 */
	FRIDAY,
	 
	/** 土曜日 */
	SATURDAY;
	
	// 日本語名を返します。
	public String toJapanese(){
		String result;
		switch(this){
			case SUNDAY:
				result = "日";
				break;
			case MONDAY:
				result = "月";
				break;
			case TUESDAY:
				result = "火";
				break;
			case WEDNESDAY:
				result = "水";
				break;
			case THURSDAY:
				result = "木";
				break;
			case FRIDAY:
				result = "金";
				break;
			case SATURDAY:
				result = "土";
				break;
			default:
				throw new RuntimeException("");
		}
		return result;
	}
	
	/** 英略称を返します
	 * (例) Sun, Mon, _Tue, Wed, Thu, Fri, Sat
	 */
	public String getAbbreviation(){
		String result;
		switch(this){
			case SUNDAY:
				result = "Sun";
				break;
			case MONDAY:
				result = "Mon";
				break;
			case TUESDAY:
				result = "Tue";
				break;
			case WEDNESDAY:
				result = "Wed";
				break;
			case THURSDAY:
				result = "Thu";
				break;
			case FRIDAY:
				result = "Fri";
				break;
			case SATURDAY:
				result = "Sat";
				break;
			default:
				throw new RuntimeException("");
		}
		return result;
	}
}

