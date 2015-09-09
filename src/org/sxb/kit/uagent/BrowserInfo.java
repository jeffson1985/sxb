
package org.sxb.kit.uagent;

/** クライアントブラウザの情報 */
public final class BrowserInfo {
	public enum  Browser {
		IE, FIREFOX, NETSCAPE, OPERA, SAFARI, OTHER;
	}
	
	public enum Os{
		WIN, MAC, OTHER;
	}
	
	BrowserInfo(String userAgent, Browser browser, double browserVersion, Os os){
		_userAgent      = userAgent;
		_browser        = browser;
		_browserVersion = browserVersion;
		_os             = os;
	}
	
	
	
	private final String  _userAgent;
	private final Browser _browser;
	private final double  _browserVersion;
	private final Os      _os;
	
	public String getUserAgent(){
		return _userAgent;
	}
	
	public Browser getBrowser(){
		return _browser;
	}
	public double getBrowserVersion(){
		return _browserVersion;
	}
	public Os getOs(){
		return _os;
	}
	
	@Override
	public String toString(){
		return super.toString() + " browser=" + _browser + ":" + _browserVersion + " os=" + _os;
	}
}
