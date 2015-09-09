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

package org.sxb.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler.
 * <p>
 * You can config Handler in SxbConfig.configHandler() method,
 * Handler can do anything under the sxb action.
 * 可以在SxbConfig.configHandler()中配置控制器
 * 控制器可以对对任何的SXB路由进行操作
 */
public abstract class Handler {
	
	protected Handler nextHandler;
	
	/**
	 * Handle target
	 * @param target url target of this web http request
	 * @param request HttpServletRequest of this http request
	 * @param response HttpServletRequest of this http request
	 * @param isHandled SxbFilter will invoke doFilter() method if isHandled[0] == false,
	 * 			it is usually to tell Filter should handle the static resource.
	 */
	public abstract void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled);
}




