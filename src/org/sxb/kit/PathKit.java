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

package org.sxb.kit;

import java.io.File;

/**
 * new File("..\path\abc.txt") 中的三个方法获取路径的方法
 * 1： getPath() 获取相对路径，例如   ..\path\abc.txt
 * 2： getAbslutlyPath() 获取绝对路径，但可能包含 ".." 或 "." 字符，例如  D:\otherPath\..\path\abc.txt
 * 3： getCanonicalPath() 获取绝对路径，但不包含 ".." 或 "." 字符，例如  D:\path\abc.txt
 */
public class PathKit {
	
	private static String webRootPath;
	private static String rootClassPath;
	
	/**
	 * 获取指定类的绝对路径
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getPath(Class clazz) {
		String path = clazz.getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	/**
	 * 根据对象获取绝对路径
	 * @param object
	 * @return
	 */
	public static String getPath(Object object) {
		String path = object.getClass().getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	/**
	 * 获取类根路径
	 * @return
	 */
	public static String getRootClassPath() {
		if (rootClassPath == null) {
			try {
				String path = PathKit.class.getClassLoader().getResource("").toURI().getPath();
				rootClassPath = new File(path).getAbsolutePath();
			}
			catch (Exception e) {
				String path = PathKit.class.getClassLoader().getResource("").getPath();
				rootClassPath = new File(path).getAbsolutePath();
			}
		}
		return rootClassPath;
	}
	/**
	 * 设定类根路径
	 * @param rootClassPath
	 */
	public void setRootClassPath(String rootClassPath) {
		PathKit.rootClassPath = rootClassPath;
	}
	
	/**
	 * 获取包路径
	 * @param object
	 * @return
	 */
	public static String getPackagePath(Object object) {
		Package p = object.getClass().getPackage();
		return p != null ? p.getName().replaceAll("\\.", "/") : "";
	}
	
	/**
	 * 从jar包中获取文件
	 * @param file
	 * @return
	 */
	public static File getFileFromJar(String file) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}
	
	/**
	 * 获取网站根目录
	 * @return
	 */
	public static String getWebRootPath() {
		if (webRootPath == null)
			webRootPath = detectWebRootPath();
		return webRootPath;
	}
	
	/**
	 * 设定网站根目录
	 * @param webRootPath
	 */
	public static void setWebRootPath(String webRootPath) {
		if (webRootPath == null)
			return ;
		
		if (webRootPath.endsWith(File.separator))
			webRootPath = webRootPath.substring(0, webRootPath.length() - 1);
		PathKit.webRootPath = webRootPath;
	}
	
	/**
	 * 探测网站跟目录
	 * @return 网站根目录
	 */
	private static String detectWebRootPath() {
		try {
			String path = PathKit.class.getResource("/").toURI().getPath();
			return new File(path).getParentFile().getParentFile().getCanonicalPath();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	private static String detectWebRootPath() {
		try {
			String path = PathKit.class.getResource("/").getFile();
			return new File(path).getParentFile().getParentFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	*/
}


