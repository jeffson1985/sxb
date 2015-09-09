
package org.sxb.kit.uagent;

import javax.servlet.http.*;

import java.util.*;
import java.util.regex.*;


// ブラウザおよびOSの判定クラス
// (futomiのCGIからロジックを拝借しています)
public final class BrowserDiscriminator {
	
	public BrowserDiscriminator(){}
	
	/** クライアントのブラウザ情報を返します */
	public static BrowserInfo getInfo(HttpServletRequest request){
		String userAgent = request.getHeader("user-agent");
		return analyze(userAgent);
	}
	
	
	private static BrowserInfo analyze(String userAgent){
		
		SimpleRegex reg = new SimpleRegex();
		String $browser    = null;
		String $browser_v  = null;
		String $platform   = null;
		
		
		if(reg.match(userAgent, "^Mozilla\\/[^\\(]+\\(compatible\\; MSIE .+\\)")) {
			
			// -- IE系 -- //
			
			if(reg.match(userAgent, "NetCaptor ([0-9\\.]+)")) {
				$browser = "NetCaptor";
				$browser_v = reg.$1;
			} else {
				$browser = "InternetExplorer";
				String $user_agent2 = userAgent;
				$user_agent2 = userAgent.replaceAll(" ", "");
				String[] version_buff = $user_agent2.split("\\;");
				version_buff = grep("MSIE", version_buff);
				$browser_v = version_buff[0];
				$browser_v = $browser_v.replaceAll("MSIE", "");
				if(reg.match($browser_v, "^([0-9]+)\\.([0-9]+)")){
					$browser_v = reg.$1 + "." + reg.$2;;
				}
			}
			
			if(reg.match(userAgent, "(?i)Win")){
				$platform = "Windows";
			}else if(reg.match(userAgent, "(?i)Mac")) {
				$platform = "MacOS";
			} else {
				$platform = "";
			}
			
		}else if(reg.match(userAgent, "Safari\\/([0-9]+)")) {
			
			// -- サファリ -- //
			$browser = "Safari";
			$browser_v = reg.$1;
			$platform = "MacOS";
			
			if(reg.match(userAgent, " PPC ")) {
			}
			
			
			
		}else if(reg.match(userAgent, "Opera")) {
			$browser = "Opera";
			
			if(reg.match(userAgent, "^Opera\\/([0-9\\.]+)")) {
				$browser_v = reg.$1;
			}else if(reg.match(userAgent, "Opera\\s+([0-9\\.]+)")) {
				$browser_v = reg.$1;
			} else {
				$browser_v = "";
			}
			
			if(reg.match(userAgent, "(?i)Windows\\s+([^\\;]+)(\\;|\\))")) {
			
				$platform = "Windows";
				
			}else if(reg.match(userAgent, "Macintosh\\;[^\\;]+\\;([^\\)]+)\\)")) {
				$platform = "MacOS";
			}else if(reg.match(userAgent, "(?i)Mac_PowerPC")) {
				$platform = "MacOS";
			} else {
				$platform = "";
			}
			
			
		
		}else if(reg.match(userAgent, "^Mozilla\\/([0-9\\.]+)")) {
			$browser = "NetscapeNavigator";
			$browser_v = reg.$1;
			if(reg.match(userAgent, "Gecko\\/")) {
				if(reg.match(userAgent, "Netscape[0-9]*\\/([0-9a-zA-Z\\.]+)")) {
					$browser_v = reg.$1;
				}else if(reg.match(userAgent, "(Phoenix|Chimera|Firefox|Camino)\\/([0-9a-zA-Z\\.]+)")) {
					$browser = reg.$1;
					$browser_v = reg.$2;
				} else {
					$browser = "Mozilla";
					if(reg.match(userAgent, "rv:([0-9\\.]+)/")) {
						$browser_v = reg.$1;
					} else {
						$browser_v = "";
					}
				}
			}
			
			if(reg.match(userAgent, "(?i)Win")){
				$platform = "Windows";
			}else if(reg.match(userAgent, "(?i)Macintosh")){
				$platform = "MacOS";
			} else {
				$platform = "";
			}
		/*
		}else if(reg.match(userAgent, "Googlebot")){
			
			// -- ロボット（独自実装） -- //
			$platform = "Robot";
			$browser = "";
			$browser_v = "";
		*/
		} else {
			$platform = "";
			$browser = "";
			$browser_v = "";
		}

		
		
		// ブラウザ情報の作成 -----------------------------------------
		//BrowserInfo report = new BrowserInfo();
		//report.userAgent = userAgent;
		
		// ブラウザ
		BrowserInfo.Browser browser;
		if($browser.equals("InternetExplorer")){
			
			browser = BrowserInfo.Browser.IE;
			
		}else if($browser.equals("Firefox")){
			
			browser = BrowserInfo.Browser.FIREFOX;
			
		}else if($browser.equals("NetscapeNavigator")){
			
			browser = BrowserInfo.Browser.NETSCAPE;
			
		}else if($browser.equals("Opera")){
			
			browser = BrowserInfo.Browser.OPERA;
			
		}else if($browser.equals("Safari")){
			
			browser = BrowserInfo.Browser.SAFARI;
		
		/*
		}else if($browser.equals("Robot")){
			
			report.browser = BrowserInfo.Browser.ROBOT;
		*/

		}else {
			
			browser = BrowserInfo.Browser.OTHER;
			
		}
		
		// OS
		BrowserInfo.Os os;
		if($platform.equals("Windows")){
			
			os = BrowserInfo.Os.WIN;
			
		}else if($platform.equals("MacOS")){
			
			os = BrowserInfo.Os.MAC;
			
		}else {
			
			os = BrowserInfo.Os.OTHER;
			
		}
		
		// バージョン
		double browserVersion;
		if($browser_v ==null || $browser_v.equals("")){
			
			browserVersion = -1;	// unknown
			
		}else {
			// ブラウザのバージョンの「2.0.0.1」なども処理→2.001にする
			//String intPart = $browser_v.split("\\.")[0];
			String numbers[] = $browser_v.split("\\.");
			StringBuffer buf_version = new StringBuffer();
			for(int i=0; i<numbers.length; i++){
				String number = numbers[i];
				
				buf_version.append(number);
				if(i==0){
					buf_version.append(".");
				}
			}
			
			
			try{
				browserVersion = Double.parseDouble(buf_version.toString());
				
			}catch(NumberFormatException e){
				
				browserVersion = -1;	// unknown
			}
			
		}
		
		
		BrowserInfo report = new BrowserInfo(
			userAgent,
			browser,
			browserVersion,
			os
		);
		
		return report;
	}
	
	private static String[] grep(String str, String[] array){
		
		if(str == null) return new String[0];
		
		List<String> lines = new ArrayList<String>(Arrays.asList(array));
		for(Iterator<String> it=lines.iterator(); it.hasNext();){
			
			String line = (String)it.next();
			if(!line.contains(str)){
				it.remove();
			}
		}
		return lines.toArray(new String[0]);
	}
	
	
	
	// 正規表現を手軽に使えるように暫定的に作成
	@SuppressWarnings("unused")
	private static class SimpleRegex {
		
		
		public String $0;
		public String $1;
		public String $2;
		public String $3;
		public String $4;
		public String $5;
		public String $6;
		public String $7;
		public String $8;
		public String $9;
		
		/**
		 * パターンをマッチさせます
		 * このとき、$0～$9に適切な値がセットされます。
		 * 一回限りのマッチに使用します
		 */
		public boolean match(String input, String regex){
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(input);
			
			boolean found = matcher.find();
			
			String[] groups = new String[10];
			if(found){
				for(int i=0, n=matcher.groupCount(); i<=n; i++){
					groups[i] = matcher.group(i);
				}
			}


			
			$0 = groups[0];
			$1 = groups[1];
			$2 = groups[2];
			$3 = groups[3];
			$4 = groups[4];
			$5 = groups[5];
			$6 = groups[6];
			$7 = groups[7];
			$8 = groups[8];
			$9 = groups[9];
			

			
			return found;
		}
	}

	
}