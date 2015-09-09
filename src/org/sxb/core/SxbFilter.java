/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 * -----------------------------------------
 * ----  Surpass Across Border Framework ---
 * ----  S-XB  Framework                 ---
 * -----------------------------------------
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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sxb.config.Constants;
import org.sxb.config.SxbConfig;
import org.sxb.handler.Handler;
import org.sxb.log.Logger;

/**
 * Sxb framework filter
 */
public final class SxbFilter implements Filter {
	
	private Handler handler; // 控制器
	private String encoding; // 编码
	private SxbConfig SxbConfig; // SXB配置
	private Constants constants; // 框架常量
	private static final Sxb sxb = Sxb.me(); // Sxb启动
	private static Logger log; // 日志记录
	private int contextPathLength; // 项目路径长度
	
	public void init(FilterConfig filterConfig) throws ServletException {
		// 根据web.xml配置来创建sxb配置对象
		createSxbConfig(filterConfig.getInitParameter("configClass"));
		
		if (sxb.init(SxbConfig, filterConfig.getServletContext()) == false)
			throw new RuntimeException("Sxb init error!");
		
		handler = sxb.getHandler();
		constants = Config.getConstants();
		encoding = constants.getEncoding();
		SxbConfig.afterSxbStart();
		
		String contextPath = filterConfig.getServletContext().getContextPath();
		contextPathLength = (contextPath == null || "/".equals(contextPath) ? 0 : contextPath.length());
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		// 设定统一编码
		request.setCharacterEncoding(encoding);
		
		// 截取访问路径
		String target = request.getRequestURI();
		if (contextPathLength != 0)
			target = target.substring(contextPathLength);
		
		boolean[] isHandled = {false};
		// handler在interceptor之前执行
		// interceptor在controller前执行  actionMapping类来管理
		try {
			handler.handle(target, request, response, isHandled);
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		
		if (isHandled[0] == false)
			chain.doFilter(request, response);
	}
	
	public void destroy() {
		// 框架停止前，进行的处理
		SxbConfig.beforeSxbStop();
		sxb.stopPlugins();
	}
	/**
	 * 创建sxb配置对象
	 * @param configClass
	 */
	private void createSxbConfig(String configClass) {
		if (configClass == null)
			throw new RuntimeException("Please set configClass parameter of SxbFilter in web.xml");
		
		Object temp = null;
		try {
			temp = Class.forName(configClass).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Can not create instance of class: " + configClass, e);
		}
		
		if (temp instanceof SxbConfig)
			SxbConfig = (SxbConfig)temp;
		else
			throw new RuntimeException("Can not create instance of class: " + configClass + ". Please check the config in web.xml");
	}
	
	static void initLogger() {
		log = Logger.getLogger(SxbFilter.class);
	}
}
