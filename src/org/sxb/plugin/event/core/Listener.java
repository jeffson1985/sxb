package org.sxb.plugin.event.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解标记需要扫描的监听器
 * @author Jeffson
 * email: jefson.app@gmail.com
 * site:http://www.cetvision.com
 * date 2015年4月26日下午9:58:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Listener {

	// The order value. Default is {@link Integer#MAX_VALUE}.
	int order() default Integer.MAX_VALUE;

	// 标记Listener是否为异步，Default is false
	boolean enableAsync() default false;

}