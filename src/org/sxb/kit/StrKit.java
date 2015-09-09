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

/**
 * StrKit.
 */
public class StrKit extends StringKits{
	 private static final char SEPARATOR = '_';
	/**
	 * 首字母变小写
	 */
	public static String firstCharToLowerCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'A' && firstChar <= 'Z') {
			char[] arr = str.toCharArray();
			arr[0] += ('a' - 'A');
			return new String(arr);
		}
		return str;
	}
	
	/**
	 * 首字母变大写
	 */
	public static String firstCharToUpperCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'a' && firstChar <= 'z') {
			char[] arr = str.toCharArray();
			arr[0] -= ('a' - 'A');
			return new String(arr);
		}
		return str;
	}
	
	/**
	 * 字符串为 null 或者为  "" 时返回 true
	 */
	public static boolean isBlank(String str) {
		return str == null || "".equals(str.trim()) ? true : false;
	}
	
	/**
	 * 字符串不为 null 而且不为  "" 时返回 true
	 */
	public static boolean notBlank(String str) {
		return str == null || "".equals(str.trim()) ? false : true;
	}
	
	public static boolean notBlank(String... strings) {
		if (strings == null)
			return false;
		for (String str : strings)
			if (str == null || "".equals(str.trim()))
				return false;
		return true;
	}
	
	public static boolean notNull(Object... paras) {
		if (paras == null)
			return false;
		for (Object obj : paras)
			if (obj == null)
				return false;
		return true;
	}
	
	/**
	   * 将驼峰式命名的字符串转换为下划线方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。</br>
	   * 例如：HelloWorld->hello_world
	   *
	   * @param name 转换前的驼峰式命名的字符串
	   * @return 转换后下划线大写方式命名的字符串
	   */
	  public static String underlineCase(String name) {
	    if (name == null) {
	      return null;
	    }
	    if (name.contains("_")) {
	      return name;
	    } else {
	      StringBuilder sb = new StringBuilder();
	      boolean upperCase = false;
	      for (int i = 0; i < name.length(); i++) {
	        char c = name.charAt(i);
	        boolean nextUpperCase = true;
	        if (i < (name.length() - 1)) {
	          nextUpperCase = Character.isUpperCase(name.charAt(i + 1));
	        }
	        if ((i >= 0) && Character.isUpperCase(c)) {
	          if (!upperCase || !nextUpperCase) {
	            if (i > 0) sb.append(SEPARATOR);
	          }
	          upperCase = true;
	        } else {
	          upperCase = false;
	        }
	        sb.append(Character.toLowerCase(c));
	      }
	      return sb.toString();
	    }
	  }
	
	/**
	   * 将下划线方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
	   * 例如：hello_world->HelloWorld
	   *
	   * @param name 转换前的下划线大写方式命名的字符串
	   * @return 转换后的驼峰式命名的字符串
	   */
	  public static String camelCase(String name) {
	    if (name == null) {
	      return null;
	    }
	    if (name.contains("_")) {
	      name = name.toLowerCase();

	      StringBuilder sb = new StringBuilder(name.length());
	      boolean upperCase = false;
	      for (int i = 0; i < name.length(); i++) {
	        char c = name.charAt(i);

	        if (c == SEPARATOR) {
	          upperCase = true;
	        } else if (upperCase) {
	          sb.append(Character.toUpperCase(c));
	          upperCase = false;
	        } else {
	          sb.append(c);
	        }
	      }
	      return sb.toString();
	    } else
	      return name;
	  }

	  public static String firstUpperCamelCase(String name) {
	    if (name == null) {
	      return null;
	    }
	    name = camelCase(name);
	    return firstCharToUpperCase(name);
	  }
	  
	  public static String getToken(){
		  return RandomStringKit.random(10);
	  }
}




