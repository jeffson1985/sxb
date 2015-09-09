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

import java.io.File;

import org.sxb.render.ViewType;

/**
 * Global constants definition
 * 全局默认常量
 */
public interface Const {
	
	// SXB发行版本
	String SXB_VERSION = "2.0";
	
	// 路径连接字符
	public final static String CONNECTOR = "::";
	// 视图类型
	ViewType DEFAULT_VIEW_TYPE = ViewType.FREE_MARKER;
	// 全体默认编码
	String DEFAULT_ENCODING = "UTF-8";
	// 默认运行模式
	boolean DEFAULT_DEV_MODE = false;
	// 默认URL参数分割符
	String DEFAULT_URL_PARA_SEPARATOR = "-";
	// 默认jsp视图后缀
	String DEFAULT_JSP_EXTENSION = ".jsp";
	// 默认Freemarker视图后缀，原始为ftl方便Eclipse编辑改成html
	String DEFAULT_FREE_MARKER_EXTENSION = ".html";			// The original is ".ftl", Recommend ".html"
	// 默认Velocity视图后缀
	String DEFAULT_VELOCITY_EXTENSION = ".vm";
	
	// "WEB-INF/download" + File.separator maybe better otherwise it can be downloaded by browser directly
	// 默认下载文件保存路径，放在WEB-INF/download下面可能效率更高，毕竟直接由浏览器下载
	String DEFAULT_FILE_RENDER_BASE_PATH = File.separator + "download" + File.separator;
	// 默认上传文件大小
	int DEFAULT_MAX_POST_SIZE = 1024 * 1024 * 10;  			// Default max post size of multipart request: 10 Meg
	// 默认国际化资源的生存时间
	int DEFAULT_I18N_MAX_AGE_OF_COOKIE = 999999999;
	// 默认Freemarker的更新周期， 只有开发时设定，产品发布时永不过期
	int DEFAULT_FREEMARKER_TEMPLATE_UPDATE_DELAY = 3600;	// For not devMode only
	// 默认CSRF抵御用token名
	String DEFAULT_TOKEN_NAME = "sxb_token";
	// 默认Token失效时间 单位秒
	int DEFAULT_SECONDS_OF_TOKEN_TIME_OUT = 900;			// 900 seconds ---> 15 minutes
	// 最小Token失效时间
	int MIN_SECONDS_OF_TOKEN_TIME_OUT = 300;				// 300 seconds ---> 5 minutes
	// 默认sxb启动配置文件名
	public static final String DEFATLT_SXB_CONFIG_NAME="sxb_config.txt";
}

