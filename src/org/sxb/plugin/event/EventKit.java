package org.sxb.plugin.event;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.sxb.plugin.event.core.ApplicationEvent;
import org.sxb.plugin.event.utils.ArrayListMultimap;


/**
 * 事件工具类
 * @author Jeffson
 * email: jefson.app@gmail.com
 * site:http://www.cetvision.com
 * date 2015年4月26日下午9:58:53
 */
public class EventKit {

	private static ArrayListMultimap<Type, ListenerHelper> map;
	private static ExecutorService pool;

	static void init(ArrayListMultimap<Type, ListenerHelper> map, ExecutorService pool) {
		EventKit.map = map;
		EventKit.pool = pool;
	}

	/**
	 * 发布事件
	 * 执行发送消息
	 * @param event ApplicationEvent
	 */
	@SuppressWarnings("unchecked")
	public static void postEvent(final ApplicationEvent event) {
		Collection<ListenerHelper> listenerList = map.get(event.getClass());
		for (final ListenerHelper helper : listenerList) {
			if (null != pool && helper.enableAsync) {
				pool.execute(new Runnable() {

					@Override
					public void run() {
						helper.listener.onApplicationEvent(event);
					}
				});
			} else {
				helper.listener.onApplicationEvent(event);
			}
		}
	}

}
