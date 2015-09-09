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

package org.sxb.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.sxb.kit.StrKit;

/**
 * I18N support.
 * <p>
 * Example:<br>
 * 1: Create resource file "demo_zh_CN.properties" content with: msg=你好 Sxb! 你好{0}! <br>
 *    Create resource file "demo_en_US.properties" content with: msg=Hello Sxb! Hello {0}! <br><br>
 *    
 * 2: Res res = I18n.use("demo", "zh_CN");<br>
 *    res.get("msg");				// return value: 你好 Sxb! 你好{0}!<br>
 *    res.format("msg", "孙日昌");		// return value: 你好 Sxb! 你好孙日昌!<br><br>
 *    
 *    res = I18n.use("demo", "en_US");<br>
 *    res.get("msg");				// return value: Hello Sxb! Hello{0}!<br>
 *    res.format("msg", "Jeffson");	// return value: Hello Sxb! Hello Jeffson!<br>
 * </p>
 */
public class I18n {
	
	static String defaultBaseName = "i18n";
	static String defaultLocale = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
	
	private static final Map<String, Res> resMap = new HashMap<String, Res>();
	
	private I18n(){
	}
	
	public static void setDefaultBaseName(String defaultBaseName) {
		if (StrKit.isBlank(defaultBaseName))
			throw new IllegalArgumentException("defaultBaseName can not be blank.");
		I18n.defaultBaseName = defaultBaseName;
	}
	
	public static void setDefaultLocale(String defaultLocale) {
		if (StrKit.isBlank(defaultLocale))
			throw new IllegalArgumentException("defaultLocale can not be blank.");
		I18n.defaultLocale = defaultLocale;
	}
	
	/**
	 * Using the base name and locale to get the Res object, which is used to get i18n message value from the resource file.
	 * @param baseName the base name to load Resource bundle
	 * @param locale the locale string like this: "zh_CN" "en_US"
	 * @return the Res object to get i18n message value
	 */
	public static Res use(String baseName, String locale) {
		String resKey = baseName + locale;
		Res res = resMap.get(resKey);
		if (res == null) {
			synchronized (resMap) {
				res = resMap.get(resKey);
				if (res == null) {
					res = new Res(baseName, locale);
					resMap.put(resKey, res);
				}
			}
		}
		return res;
	}
	
	public static Res use(String baseName, Locale locale) {
		return use(baseName, locale.getLanguage() + "_" + locale.getCountry());
	}
	
	public static Res use(String locale) {
		return use(defaultBaseName, locale);
	}
	
	public static Res use() {
		return use(defaultBaseName, defaultLocale);
	}
}