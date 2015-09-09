/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sxb.core;

import java.lang.reflect.Method;

import org.sxb.aop.Interceptor;

/**
 *［action］＝行为，也即是用户的一个操作
 * Action
 */
public class Action {
	
	private final Class<? extends Controller> controllerClass;
	/** 用户自定义的url段 也就是控制器类的访问路径*/
	private final String controllerKey;
	/** 控制器类内的方法url键值  actionKey = controllerKey + 方法名称或者方法的注解名称*/
	private final String actionKey;
	private final Method method;
	private final String methodName;
	private final Interceptor[] interceptors;
	private final String viewPath;
	
	public Action(String controllerKey, String actionKey, Class<? extends Controller> controllerClass, Method method, String methodName, Interceptor[] interceptors, String viewPath) {
		this.controllerKey = controllerKey;
		this.actionKey = actionKey;
		this.controllerClass = controllerClass;
		this.method = method;
		this.methodName = methodName;
		this.interceptors = interceptors;
		this.viewPath = viewPath;
	}
	
	public Class<? extends Controller> getControllerClass() {
		return controllerClass;
	}
	
	public String getControllerKey() {
		return controllerKey;
	}
	
	public String getActionKey() {
		return actionKey;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public Interceptor[] getInterceptors() {
		return interceptors;
	}
	
	public String getViewPath() {
		return viewPath;
	}
	
	public String getMethodName() {
		return methodName;
	}
}









