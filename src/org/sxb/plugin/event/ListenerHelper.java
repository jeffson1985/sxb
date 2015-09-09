package org.sxb.plugin.event;

import org.sxb.plugin.event.core.ApplicationListener;

/**
 * 事件监听帮助类
 * @author Jeffson
 * email: jefson.app@gmail.com
 * site:http://www.cetvision.com
 * date 2015年4月26日下午9:58:53
 */
@SuppressWarnings("rawtypes")
class ListenerHelper {

	public final ApplicationListener listener;

	public final boolean enableAsync;

	public ListenerHelper(ApplicationListener listener, boolean enableAsync) {
		this.listener = listener;
		this.enableAsync = enableAsync;
	}

}
