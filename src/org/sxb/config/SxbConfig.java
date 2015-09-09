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

package org.sxb.config;

import java.io.File;
import java.util.Properties;

import org.sxb.core.Const;
import org.sxb.kit.Prop;
import org.sxb.kit.PropKit;

/**
 * 框架配置抽象类<br>
 * 
 * SxbConfig.
 * <p>
 * Config order: configConstant(), configRoute(), configPlugin(), configInterceptor(), configHandler()
 */
public abstract class SxbConfig {
	
	/**
	 * Config constant<br>
	 * 基本常量配置<br>
	 */
	public abstract void configConstant(Constants me);
	
	/**
	 * Config route<br>
	 * 路由配置<br>
	 * 
	 */
	public abstract void configRoute(Routes me);
	
	/**
	 * Config plugin<br>
	 * 插件配置<br>
	 * 
	 */
	public abstract void configPlugin(Plugins me);
	
	/**
	 * Config interceptor applied to all actions.<br>
	 * 拦截器配置<br>
	 * 
	 */
	public abstract void configInterceptor(Interceptors me);
	
	/**
	 * Config handler<br>
	 * 控制器配置<br>
	 * 
	 */
	public abstract void configHandler(Handlers me);
	
	/**
	 * Call back after Sxb start<br>
	 * 服务器激动后执行内容<br>
	 * 
	 */
	public void afterSxbStart(){};
	
	/**
	 * Call back before Sxb stop<br>
	 * 服务器停止后执行内容<br>
	 * 
	 */
	public void beforeSxbStop(){};
	
	protected Prop prop = null;
	
	/**
	 * Load property file.<br>
	 * 加载配置文件<br>
	 * 
	 * @see #loadPropertyFile(String, String)
	 */
	public Properties loadPropertyFile(String fileName) {
		return loadPropertyFile(fileName, Const.DEFAULT_ENCODING);
	}
	
	/**
	 * Load property file with given encoding.<br>
	 * 加载配置文件并指定编码格式<br>
	 * 
	 * Example:<br>
	 * loadPropertyFile("db_username_pass.txt", "UTF-8");
	 * 
	 * @param fileName the file in CLASSPATH or the sub directory of the CLASSPATH
	 * @param encoding the encoding
	 */
	public Properties loadPropertyFile(String fileName, String encoding) {
		prop = PropKit.use(fileName, encoding);
		return prop.getProperties();
	}
	
	/**
	 * Load property file.<br>
	 * 加载指定配置文件
	 * @see #loadPropertyFile(File, String)
	 */
	public Properties loadPropertyFile(File file) {
		return loadPropertyFile(file, Const.DEFAULT_ENCODING);
	}
	
	/**
	 * Load property file<br>
	 * 加载配置文件并指定编码格式<br>
	 * Example:<br>
	 * loadPropertyFile(new File("/var/config/my_config.txt"), "UTF-8");
	 * 
	 * @param file the properties File object
	 * @param encoding the encoding
	 */
	public Properties loadPropertyFile(File file, String encoding) {
		prop = PropKit.use(file, encoding);
		return prop.getProperties();
	}
	
	/**
	 * erase the property value with the given file name<br>
	 * 清除指定配置文件<br>
	 * 
	 * @param fileName
	 */
	public void unloadPropertyFile(String fileName) {
		Prop uselessProp = PropKit.useless(fileName);
		if (this.prop == uselessProp)
			this.prop = null;
	}
	/**
	 * erase all property value <br>
	 * 清除所有配置文件<br>
	 */
	public void unloadAllPropertyFiles() {
		PropKit.clear();
		prop = null;
	}
	
	/**
	 * get property value<br>
	 * 获取配置<br>
	 * @return (String) prop
	 */
	private Prop getProp() {
		if (prop == null)
			throw new IllegalStateException("Load propties file by invoking loadPropertyFile(String fileName) method first.");
		return prop;
	}
	
	/**
	 * get property value by given key<br>
	 * 取得指定键的值<br>
	 * @param key
	 * @return (String) prop   String
	 */
	public String getProperty(String key) {
		return getProp().get(key);
	}
	
	/**
	 * get property value by given key ,if null then set it to {@code defaultValue}<br>
	 * 获取指定键的值，如果没有赋值{@code defaultValue}<br>
	 * @param key
	 * @param defaultValue
	 * @return (String) prop
	 */
	public String getProperty(String key, String defaultValue) {
		return getProp().get(key, defaultValue);
	}
	
	/**
	 * get property value and parse into int<br>
	 * 获取指定键的值并转换成整数<br>
	 * @param key
	 * @return (Ingeger) prop
	 */
	public Integer getPropertyToInt(String key) {
		return getProp().getInt(key);
	}
	
	/**
	 * get property value by given key ,if null then set it to {@code defaultValue}<br>
	 * 获取指定键的值，如果没有赋值{@code defaultValue}<br>
	 * @param key
	 * @param defaultValue
	 * @return (Integer) prop
	 */
	public Integer getPropertyToInt(String key, Integer defaultValue) {
		return getProp().getInt(key, defaultValue);
	}
	
	/**
	 * get property value and parse into int<br>
	 * 获取指定键的值并转换成长整数<br>
	 * @param key
	 * @return (Long) prop
	 */
	public Long getPropertyToLong(String key) {
		return getProp().getLong(key);
	}
	
	/**
	 * get property value by given key ,if null then set it to {@code defaultValue}<br>
	 * 获取指定键的值，如果没有赋值{@code defaultValue}<br>
	 * @param key
	 * @param defaultValue
	 * @return (Long) prop
	 */
	public Long getPropertyToLong(String key, Long defaultValue) {
		return getProp().getLong(key, defaultValue);
	}
	

	/**
	 * get property value and parse into int<br>
	 * 获取指定键的值并转换成布尔值<br>
	 * @param key
	 * @return (Boolean) prop
	 */
	public Boolean getPropertyToBoolean(String key) {
		return getProp().getBoolean(key);
	}
	
	/**
	 * get property value by given key ,if null then set it to {@code defaultValue}<br>
	 * 获取指定键的值，如果没有赋值{@code defaultValue}<br>
	 * @param key
	 * @param defaultValue
	 * @return (Boolean) prop
	 */
	public Boolean getPropertyToBoolean(String key, Boolean defaultValue) {
		return getProp().getBoolean(key, defaultValue);
	}
}