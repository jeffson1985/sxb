package org.sxb.plugin.event.utils;

/**
 * Bean工具类
 * @author Jeffson
 * email: jefson.app@gmail.com
 * site:http://www.cetvision.com
 * date 2015年4月26日下午9:58:53
 */
public class BeanUtil {

	/**
	 * 实例化对象
	 * @param clazz 类
	 * @param <T> type parameter
	 * @return 对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<?> clazz) {
		try {
			return (T) clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 实例化对象
	 * @param clazz 类名
	 * @param <T> type parameter
	 * @return 对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String clazz) {
		try {
			return (T) Class.forName(clazz).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
