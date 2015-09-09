
/**
 * SXB预置国际化类包
 * 
 * * Example:<br>
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
 * @author Jeffson
 *
 */
package org.sxb.i18n;