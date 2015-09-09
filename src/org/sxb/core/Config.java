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

import java.util.List;

import org.sxb.config.Constants;
import org.sxb.config.Handlers;
import org.sxb.config.Interceptors;
import org.sxb.config.SxbConfig;
import org.sxb.config.Plugins;
import org.sxb.config.Routes;
import org.sxb.log.Logger;
import org.sxb.plugin.IPlugin;
/**
 * sxb 配置类
 * @author Sun
 *
 */
class Config {
	
	private static final Constants constants = new Constants();
	private static final Routes routes = new Routes(){public void config() {}};
	private static final Plugins plugins = new Plugins();
	private static final Interceptors interceptors = new Interceptors();
	private static final Handlers handlers = new Handlers();
	private static Logger log;
	
	// prevent new Config();
	private Config() {
	}
	
	/**
	 * 启动插件并初始化日志器工厂
	 * Config order: constant, route, plugin, interceptor, handler
	 * @param sxbConfig  自定义配置类，需要在web.xml中配置
	 */
	static void configSxb(SxbConfig sxbConfig) {
		sxbConfig.configConstant(constants);				initLoggerFactory();
		sxbConfig.configRoute(routes);
		sxbConfig.configPlugin(plugins);					startPlugins();	// very important!!!
		sxbConfig.configInterceptor(interceptors);
		sxbConfig.configHandler(handlers);
	}
	
	/**
	 * 提取常量配置
	 * @return
	 */
	public static final Constants getConstants() {
		return constants;
	}
	/**
	 * 提取路由
	 * @return
	 */
	public static final Routes getRoutes() {
		return routes;
	}
	
	/**
	 * 提取插件
	 * @return
	 */
	public static final Plugins getPlugins() {
		return plugins;
	}
	
	/**
	 * 提取拦截器
	 * @return
	 */
	public static final Interceptors getInterceptors() {
		return interceptors;
	}
	
	/**
	 * 提取处理器
	 * @return
	 */
	public static Handlers getHandlers() {
		return handlers;
	}
	
	/**
	 * 启动插件
	 */
	private static void startPlugins() {
		List<IPlugin> pluginList = plugins.getPluginList();
		if (pluginList == null)
			return ;
		
		for (IPlugin plugin : pluginList) {
			try {
				// process ActiveRecordPlugin devMode
				if (plugin instanceof org.sxb.plugin.activerecord.ActiveRecordPlugin) {
					org.sxb.plugin.activerecord.ActiveRecordPlugin arp = (org.sxb.plugin.activerecord.ActiveRecordPlugin)plugin;
					if (arp.getDevMode() == null)
						arp.setDevMode(constants.getDevMode());
				}
				
				if (plugin.start() == false) {
					String message = "Plugin start error: " + plugin.getClass().getName();
					log.error(message);
					throw new RuntimeException(message);
				}
			}
			catch (Exception e) {
				String message = "Plugin start error: " + plugin.getClass().getName() + ". \n" + e.getMessage();
				log.error(message, e);
				throw new RuntimeException(message, e);
			}
		}
	}
	/**
	 * 初始化日志工厂
	 */
	private static void initLoggerFactory() {
		Logger.init();
		log = Logger.getLogger(Config.class);
		SxbFilter.initLogger();
	}
}
