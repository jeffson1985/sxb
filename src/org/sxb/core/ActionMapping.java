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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sxb.aop.Interceptor;
import org.sxb.config.Interceptors;
import org.sxb.config.Routes;

/**
 * 行为［操作］映射类
 * 用户的所有操作将包装近此类，并进行分析
 * ActionMapping
 */
final class ActionMapping {
	
	private static final String SLASH = "/";
	private Routes routes;
	private Interceptors interceptors;
	
	private final Map<String, Action> mapping = new HashMap<String, Action>();
	
	ActionMapping(Routes routes, Interceptors interceptors) {
		this.routes = routes;
		this.interceptors = interceptors;
	}
	
	private Set<String> buildExcludedMethodName() {
		Set<String> excludedMethodName = new HashSet<String>();
		Method[] methods = Controller.class.getMethods();
		for (Method m : methods) {
			if (m.getParameterTypes().length == 0)
				excludedMethodName.add(m.getName());
		}
		return excludedMethodName;
	}
	/**
	 * 创建映射
	 */
	void buildActionMapping() {
		mapping.clear();
		Set<String> excludedMethodName = buildExcludedMethodName();
		ActionInterceptorBuilder interceptorBuilder = new ActionInterceptorBuilder();
		Interceptor[] globalInters = interceptors.getGlobalActionInterceptor();
		interceptorBuilder.addToInterceptorsMap(globalInters);
		for (Entry<String, Class<? extends Controller>> entry : routes.getEntrySet()) {
			Class<? extends Controller> controllerClass = entry.getValue();
			Interceptor[] controllerInters = interceptorBuilder.buildControllerInterceptors(controllerClass);
			
			boolean sonOfController = (controllerClass.getSuperclass() == Controller.class);
			// getDeclaredMethods只返回子类定义的方法名 getMethods将返回所有public的方法，包括父类
			Method[] methods = (sonOfController ? controllerClass.getDeclaredMethods() : controllerClass.getMethods());
			for (Method method : methods) {
				String methodName = method.getName();
				// 如果是Controller类本身的方法或者方法本身带有参数则放行
				// if the method belongs to super controller class or it has parameters then let it go
				if (excludedMethodName.contains(methodName) || method.getParameterTypes().length != 0)
					continue ;
				// 如果是子类的私有方法那么也放行
				// if the method belongs to son class but no public modifier then let it go
				if (sonOfController && !Modifier.isPublic(method.getModifiers()))
					continue ;
				// 方法级别拦截器
				Interceptor[] methodInters = interceptorBuilder.buildMethodInterceptors(method);
				// 类级别拦截器
				Interceptor[] actionInters = interceptorBuilder.buildActionInterceptors(globalInters, controllerInters, methodInters, method);
				String controllerKey = entry.getKey();
				
				ActionKey ak = method.getAnnotation(ActionKey.class);
				String actionKey;
				if (ak != null) {
					actionKey = ak.value().trim();
					if ("".equals(actionKey))
						throw new IllegalArgumentException(controllerClass.getName() + "." + methodName + "(): The argument of ActionKey can not be blank.");
					
					if (!actionKey.startsWith(SLASH))
						actionKey = SLASH + actionKey;
				}
				else if (methodName.equals("index")) {
					actionKey = controllerKey;
				}
				else {
					actionKey = controllerKey.equals(SLASH) ? SLASH + methodName : controllerKey + SLASH + methodName;
				}
				// 新规行为
				Action action = new Action(controllerKey, actionKey, controllerClass, method, methodName, actionInters, routes.getViewPath(controllerKey));
				// 如果有重复，则停止服务器
				if (mapping.put(actionKey, action) != null)
					throw new RuntimeException(buildMsg(actionKey, controllerClass, method));
			}
		}
		
		// support url = controllerKey + urlParas with "/" of controllerKey
		Action action = mapping.get("/");
		if (action != null)
			mapping.put("", action);
	}
	
	/**
	 * 提示信息拼装
	 * @param actionKey
	 * @param controllerClass
	 * @param method
	 * @return
	 */
	private static final String buildMsg(String actionKey, Class<? extends Controller> controllerClass, Method method) {
		StringBuilder sb = new StringBuilder("The action \"")
			.append(controllerClass.getName()).append(".")
			.append(method.getName()).append("()\" can not be mapped, ")
			.append("actionKey \"").append(actionKey).append("\" is already in use.");
		
		String msg = sb.toString();
		System.err.println("\nException: " + msg);
		return msg;
	}
	
	/**
	 * Support four types of url
	 * 1: http://abc.com/controllerKey                 ---> 00
	 * 2: http://abc.com/controllerKey/para            ---> 01
	 * 3: http://abc.com/controllerKey/method          ---> 10
	 * 4: http://abc.com/controllerKey/method/para     ---> 11
	 * The controllerKey can also contains "/"
	 * Example: http://abc.com/uvw/xyz/method/para
	 */
	Action getAction(String url, String[] urlPara) {
		Action action = mapping.get(url);
		if (action != null) {
			return action;
		}
		
		// --------
		// 最后一个斜线后面的字符串，均当作url参数来处理，
		// 在controller中可以用{@code getPara(0)}来取得
		int i = url.lastIndexOf(SLASH);
		if (i != -1) {
			action = mapping.get(url.substring(0, i));
			urlPara[0] = url.substring(i + 1);
		}
		
		return action;
	}
	/**
	 * 获取所有操作
	 * @return
	 */
	List<String> getAllActionKeys() {
		List<String> allActionKeys = new ArrayList<String>(mapping.keySet());
		Collections.sort(allActionKeys);
		return allActionKeys;
	}
}













