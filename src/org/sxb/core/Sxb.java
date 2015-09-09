/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 * 
 * -----------------------------------------
 * ----  Surpass Across Border Framework ---
 * ----  S-XB  Framework                 ---
 * -----------------------------------------
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

import java.util.List;

import javax.servlet.ServletContext;

import org.sxb.config.Constants;
import org.sxb.config.SxbConfig;
import org.sxb.handler.Handler;
import org.sxb.handler.HandlerFactory;
import org.sxb.kit.PathKit;
import org.sxb.plugin.IPlugin;
import org.sxb.render.RenderFactory;
import org.sxb.server.IServer;
import org.sxb.server.ServerFactory;
import org.sxb.token.ITokenCache;
import org.sxb.token.TokenManager;
import org.sxb.upload.OreillyCos;

/**
 * Sxb 启动类 
 * 此类为框架的中心类，框架的初始化工作大都在此完成
 * 当利用sxb内部服务器插件jetty作为服务器时，可用此类来启动调试sxb
 */
public final class Sxb {

	private Constants constants;
	private ActionMapping actionMapping;
	private Handler handler;
	private ServletContext servletContext;
	private static IServer server;
	private String contextPath = "";

	Handler getHandler() {
		return handler;
	}

	private static final Sxb me = new Sxb();

	private Sxb() {
	}

	public static Sxb me() {
		return me;
	}

	/**
	 * 初始化sxb
	 * 
	 * @param sxbConfig
	 * @param servletContext
	 * @return
	 */
	boolean init(SxbConfig sxbConfig, ServletContext servletContext) {
		this.servletContext = servletContext;
		this.contextPath = servletContext.getContextPath();

		initPathUtil();

		Config.configSxb(sxbConfig); // start plugin and init logger factory in
										// this method
		constants = Config.getConstants();

		initActionMapping();
		initHandler();
		initRender();
		initOreillyCos();
		initTokenManager();

		return true;
	}

	/**
	 * 初始化CSRF 堤防
	 */
	private void initTokenManager() {
		ITokenCache tokenCache = constants.getTokenCache();
		if (tokenCache != null)
			TokenManager.init(tokenCache);
	}

	/**
	 * 初始化控制器
	 * 路由等
	 */
	private void initHandler() {
		Handler actionHandler = new ActionHandler(actionMapping, constants);
		handler = HandlerFactory.getHandler(Config.getHandlers()
				.getHandlerList(), actionHandler);
	}

	/**
	 * 初始化ORillyCos上传组建
	 */
	private void initOreillyCos() {
		OreillyCos.init(constants.getUploadedFileSaveDirectory(),
				constants.getMaxPostSize(), constants.getEncoding());
	}

	/**
	 * 初始化路径工具
	 */
	private void initPathUtil() {
		String path = servletContext.getRealPath("/");
		PathKit.setWebRootPath(path);
	}

	/**
	 * 初始化视图渲染器
	 */
	private void initRender() {
		RenderFactory renderFactory = RenderFactory.me();
		renderFactory.init(constants, servletContext);
	}

	/**
	 * 初始化路由
	 */
	private void initActionMapping() {
		actionMapping = new ActionMapping(Config.getRoutes(),
				Config.getInterceptors());
		actionMapping.buildActionMapping();
	}

	/**
	 * 关闭所有插件
	 */
	void stopPlugins() {
		List<IPlugin> plugins = Config.getPlugins().getPluginList();
		if (plugins != null) {
			for (int i = plugins.size() - 1; i >= 0; i--) { // stop plugins
				boolean success = false;
				try {
					success = plugins.get(i).stop();
				} catch (Exception e) {
					success = false;
					e.printStackTrace();
				}
				if (!success) {
					System.err.println("Plugin stop error: "
							+ plugins.get(i).getClass().getName());
				}
			}
		}
	}

	/**
	 * 获取Servlet上下文
	 * @return
	 */
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public static void start() {
		server = ServerFactory.getServer();
		server.start();
	}

	/**
	 * 启动服务器
	 * @param webAppDir
	 * @param port
	 * @param context
	 * @param scanIntervalSeconds
	 */
	public static void start(String webAppDir, int port, String context,
			int scanIntervalSeconds) {
		server = ServerFactory.getServer(webAppDir, port, context,
				scanIntervalSeconds);
		server.start();
	}

	/**
	 * 停止服务器
	 */
	public static void stop() {
		server.stop();
	}

	/**
	 * Run Sxb Server with Debug Configurations or Run Configurations in Eclipse
	 * JavaEE args example: WebRoot 8089 / 5
	 */
	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			server = ServerFactory.getServer();
			//server = ServerFactory.getServer(8089, "/sxb");
			server.start();
		} else {
			String webAppDir = args[0];
			int port = Integer.parseInt(args[1]);
			String context = args[2];
			int scanIntervalSeconds = Integer.parseInt(args[3]);
			server = ServerFactory.getServer(webAppDir, port, context,
					scanIntervalSeconds);
			server.start();
		}
	}

	/**
	 * 获取所有路由
	 * @return
	 */
	public List<String> getAllActionKeys() {
		return actionMapping.getAllActionKeys();
	}

	/**
	 * 获取框架常量
	 * @return
	 */
	public Constants getConstants() {
		return Config.getConstants();
	}

	/**
	 * 获取指定路由
	 * @param url
	 * @param urlPara
	 * @return
	 */
	public Action getAction(String url, String[] urlPara) {
		return actionMapping.getAction(url, urlPara);
	}

	/**
	 * 获取项目根路径
	 * @return
	 */
	public String getContextPath() {
		return contextPath;
	}
}
