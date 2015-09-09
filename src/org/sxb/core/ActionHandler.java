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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sxb.aop.Invocation;
import org.sxb.config.Constants;
import org.sxb.handler.Handler;
import org.sxb.http.server.ServletServerHttpRequest;
import org.sxb.http.server.ServletServerHttpResponse;
import org.sxb.log.Logger;
import org.sxb.render.Render;
import org.sxb.render.RenderException;
import org.sxb.render.RenderFactory;
import org.sxb.upload.ext.multipart.MaxUploadSizeExceededException;

/**
 * ActionHandler
 * sxb最终必须执行的操作类
 * 行为操作类，即路由
 */
final class ActionHandler extends Handler {
	
	private final boolean devMode;
	private final ActionMapping actionMapping;
	private static final RenderFactory renderFactory = RenderFactory.me();
	private static final Logger log = Logger.getLogger(ActionHandler.class);
	/**
	 * Constructor
	 * @param actionMapping
	 * 			action map
	 * @param constants
	 * 			constants
	 */
	public ActionHandler(ActionMapping actionMapping, Constants constants) {
		this.actionMapping = actionMapping;
		this.devMode = constants.getDevMode();
	}
	
	/**
	 * handle
	 * @param target 
	 * 			uri
	 * @param request
	 * 			request
	 * @param response
	 * 			response
	 * @param isHandled
	 * 			is handled?
	 * 1: Action action = actionMapping.getAction(target)
	 * 2: new Invocation(...).invoke()
	 * 3: render(...)
	 */
	public final void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		// 放弃静态资源处理 exp: css,js,jpeg,jpg,gif,png
		if (target.indexOf('.') != -1) {
			return ;
		}
		
		isHandled[0] = true;
		String[] urlPara = {null};
		Action action = actionMapping.getAction(target, urlPara);
		
		// 找不到路由则返回404错误页
		if (action == null) {
			if (log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("404 Action Not Found: " + (qs == null ? target : target + "?" + qs));
			}
			renderFactory.getErrorRender(404).setContext(request, response).render();
			return ;
		}
		
		try {
			// 找到路由，根据路由取得控制器类，并初始化
			Controller controller = action.getControllerClass().newInstance();
			// Common fileupload 加载  如果想让Sxb启用common fileupload模式时，打开此处的注释，一旦开启RelliyCos将无法使用请注意
			//if(WebKits.isMultipart(request))
				//request = MultiPartUtil.wrapMultiPartRequest(request, response);
			ServletServerHttpRequest  rs = new ServletServerHttpRequest(request);
			ServletServerHttpResponse  rp = new ServletServerHttpResponse(response);
			//controller.init(request, response, urlPara[0]);
			controller.init(rs, rp, urlPara[0]);
			// 开发模式，则输出路由日志
			// 否则直接调用相应的控制器方法，也就是路由到用户访问的方法
			if (devMode) {
				boolean isMultipartRequest = ActionReporter.reportCommonRequest(controller, action);
				new Invocation(action, controller).invoke();
				if (isMultipartRequest) ActionReporter.reportMultipartRequest(controller, action);
			}
			else {
				new Invocation(action, controller).invoke();
			}
			// 获取渲染器，如果是路由操作，那么判断是否是同一个路由，如是则抛出异常，不是则循环处理
			Render render = controller.getRender();
			if (render instanceof ActionRender) {
				String actionUrl = ((ActionRender)render).getActionUrl();
				if (target.equals(actionUrl))
					throw new RuntimeException("The forward action url is the same as before.");
				else
					handle(actionUrl, request, response, isHandled);
				return ;
			}
			// 如果渲染器为空， 则调用默认渲染器来处理
			if (render == null)
				render = renderFactory.getDefaultRender(action.getViewPath() + action.getMethodName());
			// 执行对应视图渲染器，输出内容
			render.setContext(request, response, action.getViewPath()).render();
		}
		catch(MaxUploadSizeExceededException e){
			int errorCode = e.getErrorCode();
			if (errorCode == 301 && log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("uplpad Forbidden: " + (qs == null ? target : target + "?" + qs));
			}
			else if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
			// 返回错误页
			e.getErrorRender().setContext(request, response, action.getViewPath()).render();
		}
		catch (RenderException e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		catch (ActionException e) {
			int errorCode = e.getErrorCode();
			if (errorCode == 404 && log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("404 Not Found: " + (qs == null ? target : target + "?" + qs));
			}
			else if (errorCode == 401 && log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("401 Unauthorized: " + (qs == null ? target : target + "?" + qs));
			}
			else if (errorCode == 403 && log.isWarnEnabled()) {
				String qs = request.getQueryString();
				log.warn("403 Forbidden: " + (qs == null ? target : target + "?" + qs));
			}
			else if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
			// 返回错误页
			e.getErrorRender().setContext(request, response, action.getViewPath()).render();
		}
		catch (Throwable t) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, t);
			}
			renderFactory.getErrorRender(500).setContext(request, response, action.getViewPath()).render();
		}
	}
}





