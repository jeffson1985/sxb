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

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sxb.aop.Enhancer;
import org.sxb.aop.Interceptor;
import org.sxb.http.MediaType;
import org.sxb.http.converter.json.MappingJackson2HttpMessageConverter;
import org.sxb.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.sxb.http.server.ServletServerHttpRequest;
import org.sxb.http.server.ServletServerHttpResponse;
import org.sxb.kit.StrKit;
import org.sxb.kit.http.IpKit;
import org.sxb.render.ContentType;
import org.sxb.render.Render;
import org.sxb.render.RenderFactory;
import org.sxb.render.report.ReportType;
import org.sxb.upload.MultipartRequest;
import org.sxb.upload.UploadFile;
import org.sxb.upload.ext.multipart.MultipartFile;
import org.sxb.upload.ext.multipart.MultipartHttpServletRequest;
import org.sxb.upload.util.MultiPartUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * Controller（控制器类） <br>
 * 框架只不过是一个舞台，数据流在其中流转。<br>
 * Framework just is a stage that the data stream  runs in.<br>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class Controller {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletServerHttpRequest ssres;
	@SuppressWarnings("unused")
	private ServletServerHttpResponse ssrep;

	private String urlPara;
	private String[] urlParaArray;

	private static final String[] NULL_URL_PARA_ARRAY = new String[0];
	private static final String URL_PARA_SEPARATOR = Config.getConstants()
			.getUrlParaSeparator();

	void init(HttpServletRequest request, HttpServletResponse response,
			String urlPara) {
		this.request = request;
		this.response = response;
		this.urlPara = urlPara;
	}
	
	void init(ServletServerHttpRequest request, ServletServerHttpResponse response,
			String urlPara) {
		this.ssrep = response;
		this.ssres = request;
		this.request = request.getServletRequest();
		this.response = response.getServletResponse();
		
		this.urlPara = urlPara;
	}

	public void setUrlPara(String urlPara) {
		this.urlPara = urlPara;
		this.urlParaArray = null;
	}

	/**
	 * Stores an attribute in this request
	 * 
	 * @param name
	 *            a String specifying the name of the attribute
	 * @param value
	 *            the Object to be stored
	 * @return  controller
	 * 			  the Controller of this
	 */
	public Controller setAttr(String name, Object value) {
		request.setAttribute(name, value);
		return this;
	}

	/**
	 * Removes an attribute from this request
	 * 
	 * @param name
	 *            a String specifying the name of the attribute to remove
	 * @return  controller
	 * 			  the Controller of this                      
	 */
	public Controller removeAttr(String name) {
		request.removeAttribute(name);
		return this;
	}

	/**
	 * Stores attributes in this request, key of the map as attribute name and
	 * value of the map as attribute value
	 * 
	 * @param attrMap
	 *            key and value as attribute of the map to be stored
	 * @return  controller
	 * 			  the Controller of this
	 */
	public Controller setAttrs(Map<String, Object> attrMap) {
		for (Map.Entry<String, Object> entry : attrMap.entrySet())
			request.setAttribute(entry.getKey(), entry.getValue());
		return this;
	}

	/**
	 * Returns the value of a request parameter as a String, or null if the
	 * parameter does not exist.
	 * <p>
	 * You should only use this method when you are sure the parameter has only
	 * one value. If the parameter might have more than one value, use
	 * getParaValues(java.lang.String).
	 * <p>
	 * If you use this method with a multivalued parameter, the value returned
	 * is equal to the first value in the array returned by getParameterValues.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @return a String representing the single value of the parameter
	 */
	public String getPara(String name) {
		return request.getParameter(name);
	}

	/**
	 * Returns the value of a request parameter as a String, or default value if
	 * the parameter does not exist.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @param defaultValue
	 *            a String value be returned when the value of parameter is null
	 * @return a String representing the single value of the parameter
	 */
	public String getPara(String name, String defaultValue) {
		String result = request.getParameter(name);
		return result != null && !"".equals(result) ? result : defaultValue;
	}

	/**
	 * Returns the values of the request parameters as a Map.
	 * 
	 * @return a Map contains all the parameters name and value
	 */
	public Map<String, String[]> getParaMap() {
		return request.getParameterMap();
	}

	/**
	 * Returns the values of the request parameters as a JSONObject.
	 * @return a JSONObject contains all the parameters name and value
	 */
	public JSONObject getParaJson() {	
		JSONObject rst = null;
		try {
			//System.out.println(ssres.getHeaders().getContentType().getSubtype());
			MediaType contentType = ssres.getHeaders().getContentType();
//System.out.println("Jeffson->Head-Accept" + input.getHeaders().getAccept().toString());
			System.out.println(contentType.toString());
			if(contentType.includes(MediaType.TEXT_XML)){
				
				rst = (JSONObject)new MappingJackson2XmlHttpMessageConverter().read(JSONObject.class, ssres);
				return  rst;
			}
			else if(contentType.includes(MediaType.APPLICATION_JSON)){
				
				return (JSONObject)new MappingJackson2HttpMessageConverter().read(JSONObject.class, ssres);
				// use alibaba fastJson jar
				//rst = (JSONObject)new FastJsonHttpMessageConverter().read(JSONObject.class, input);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rst;
		
	}

	/**
	 * Returns an Enumeration of String objects containing the names of the
	 * parameters contained in this request. If the request has no parameters,
	 * the method returns an empty Enumeration.
	 * 
	 * @return an Enumeration of String objects, each String containing the name
	 *         of a request parameter; or an empty Enumeration if the request
	 *         has no parameters
	 */
	public Enumeration<String> getParaNames() {
		return request.getParameterNames();
	}

	/**
	 * Returns an array of String objects containing all of the values the given
	 * request parameter has, or null if the parameter does not exist. If the
	 * parameter has a single value, the array has a length of 1.
	 * 
	 * @param name
	 *            a String containing the name of the parameter whose value is
	 *            requested
	 * @return an array of String objects containing the parameter's values
	 */
	public String[] getParaValues(String name) {
		return request.getParameterValues(name);
	}

	/**
	 * Returns an array of Integer objects containing all of the values the
	 * given request parameter has, or null if the parameter does not exist. If
	 * the parameter has a single value, the array has a length of 1.
	 * 
	 * @param name
	 *            a String containing the name of the parameter whose value is
	 *            requested
	 * @return an array of Integer objects containing the parameter's values
	 */
	public Integer[] getParaValuesToInt(String name) {
		String[] values = request.getParameterValues(name);
		if (values == null)
			return null;
		Integer[] result = new Integer[values.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Integer.parseInt(values[i]);
		return result;
	}

	public Long[] getParaValuesToLong(String name) {
		String[] values = request.getParameterValues(name);
		if (values == null)
			return null;
		Long[] result = new Long[values.length];
		for (int i = 0; i < result.length; i++)
			result[i] = Long.parseLong(values[i]);
		return result;
	}

	/**
	 * Returns an Enumeration containing the names of the attributes available
	 * to this request. This method returns an empty Enumeration if the request
	 * has no attributes available to it.
	 * 
	 * @return an Enumeration of strings containing the names of the request's
	 *         attributes
	 */
	public Enumeration<String> getAttrNames() {
		return request.getAttributeNames();
	}

	/**
	 * Returns the value of the named attribute as an Object, or null if no
	 * attribute of the given name exists.
	 * 
	 * @param <T>  
	 * 			  the return param type
	 * @param name
	 *            a String specifying the name of the attribute
	 * @return an Object containing the value of the attribute, or null if the
	 *         attribute does not exist
	 */
	public <T> T getAttr(String name) {
		return (T) request.getAttribute(name);
	}

	/**
	 * Returns the value of the named attribute as an Object, or null if no
	 * attribute of the given name exists.
	 * 
	 * @param name
	 *            a String specifying the name of the attribute
	 * @return an String Object containing the value of the attribute, or null
	 *         if the attribute does not exist
	 */
	public String getAttrForStr(String name) {
		return (String) request.getAttribute(name);
	}

	/**
	 * Returns the value of the named attribute as an Object, or null if no
	 * attribute of the given name exists.
	 * 
	 * @param name
	 *            a String specifying the name of the attribute
	 * @return an Integer Object containing the value of the attribute, or null
	 *         if the attribute does not exist
	 */
	public Integer getAttrForInt(String name) {
		return (Integer) request.getAttribute(name);
	}

	private Integer toInt(String value, Integer defaultValue) {
		try {
			if (value == null || "".equals(value.trim()))
				return defaultValue;
			value = value.trim();
			if (value.startsWith("N") || value.startsWith("n"))
				return -Integer.parseInt(value.substring(1));
			return Integer.parseInt(value);
		} catch (Exception e) {
			throw new ActionException(404, renderFactory.getErrorRender(404),
					"Can not parse the parameter \"" + value
							+ "\" to Integer value.");
		}
	}

	/**
	 * Returns the value of a request parameter and convert to Integer.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name) {
		return toInt(request.getParameter(name), null);
	}

	/**
	 * Returns the value of a request parameter and convert to Integer with a
	 * default value if it is null.
	 * 
	 * @param name 
	 * 			  a String specifying the name of the parameter
	 * @param defaultValue
	 *            a default value 
	 * @return a Integer representing the single value of the parameter
	 */
	public Integer getParaToInt(String name, Integer defaultValue) {
		return toInt(request.getParameter(name), defaultValue);
	}

	private Long toLong(String value, Long defaultValue) {
		try {
			if (value == null || "".equals(value.trim()))
				return defaultValue;
			value = value.trim();
			if (value.startsWith("N") || value.startsWith("n"))
				return -Long.parseLong(value.substring(1));
			return Long.parseLong(value);
		} catch (Exception e) {
			throw new ActionException(404, renderFactory.getErrorRender(404),
					"Can not parse the parameter \"" + value
							+ "\" to Long value.");
		}
	}

	/**
	 * Returns the value of a request parameter and convert to Long.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name) {
		return toLong(request.getParameter(name), null);
	}

	/**
	 * Returns the value of a request parameter and convert to Long with a
	 * default value if it is null.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @param defaultValue
	 * 			a Long defalut value when the value is null
	 * @return a Integer representing the single value of the parameter
	 */
	public Long getParaToLong(String name, Long defaultValue) {
		return toLong(request.getParameter(name), defaultValue);
	}

	/**
	 * 
	 * @param value
	 * 			a String specifying the name of the parameter
	 * @param defaultValue
	 * 			a  defalut value when the value is null
	 * @return  Boolean 
	 */
	private Boolean toBoolean(String value, Boolean defaultValue) {
		if (value == null || "".equals(value.trim()))
			return defaultValue;
		value = value.trim().toLowerCase();
		if ("1".equals(value) || "true".equals(value))
			return Boolean.TRUE;
		else if ("0".equals(value) || "false".equals(value))
			return Boolean.FALSE;
		throw new ActionException(404, renderFactory.getErrorRender(404),
				"Can not parse the parameter \"" + value
						+ "\" to Boolean value.");
	}

	/**
	 * Returns the value of a request parameter and convert to Boolean.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @return true if the value of the parameter is "true" or "1", false if it
	 *         is "false" or "0", null if parameter is not exists
	 */
	public Boolean getParaToBoolean(String name) {
		return toBoolean(request.getParameter(name), null);
	}

	/**
	 * Returns the value of a request parameter and convert to Boolean with a
	 * default value if it is null.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @param defaultValue
	 * 			a default value
	 * @return true if the value of the parameter is "true" or "1", false if it
	 *         is "false" or "0", default value if it is null
	 */
	public Boolean getParaToBoolean(String name, Boolean defaultValue) {
		return toBoolean(request.getParameter(name), defaultValue);
	}

	/**
	 * Get all para from url and convert to Boolean
	 * @return Boolean
	 */
	public Boolean getParaToBoolean() {
		return toBoolean(getPara(), null);
	}

	/**
	 * Get para from url and conver to Boolean. The first index is 0
	 * @param index
	 * 			the position of the para from url
	 * @return Boolean
	 */
	public Boolean getParaToBoolean(int index) {
		return toBoolean(getPara(index), null);
	}

	/**
	 * Get para from url and conver to Boolean with default value if it is null.
	 * @param index
	 *  			the position of the para from url
	 * @param defaultValue
	 * 			a default value
	 * @return Boolean
	 */
	public Boolean getParaToBoolean(int index, Boolean defaultValue) {
		return toBoolean(getPara(index), defaultValue);
	}

	/**
	 * Convert to date
	 * @param value
	 * @param defaultValue
	 * @return Date
	 */
	private Date toDate(String value, Date defaultValue) {
		try {
			if (value == null || "".equals(value.trim()))
				return defaultValue;
			return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value
					.trim());
		} catch (Exception e) {
			throw new ActionException(404, renderFactory.getErrorRender(404),
					"Can not parse the parameter \"" + value
							+ "\" to Date value.");
		}
	}

	/**
	 * Returns the value of a request parameter and convert to Date.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @return a Date representing the single value of the parameter
	 */
	public Date getParaToDate(String name) {
		return toDate(request.getParameter(name), null);
	}

	/**
	 * Returns the value of a request parameter and convert to Date with a
	 * default value if it is null.
	 * 
	 * @param name
	 *            a String specifying the name of the parameter
	 * @param defaultValue
	 * 			  a default value
	 * @return a Date representing the single value of the parameter
	 */
	public Date getParaToDate(String name, Date defaultValue) {
		return toDate(request.getParameter(name), defaultValue);
	}

	/**
	 * Get all para from url and convert to Date
	 * @return Date
	 */
	public Date getParaToDate() {
		return toDate(getPara(), null);
	}

	/**
	 * Return HttpServletRequest. Do not use HttpServletRequest Object in
	 * constructor of Controller
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Return HttpServletResponse. Do not use HttpServletResponse Object in
	 * constructor of Controller
	 * @return HttpServletRequest
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * Return HttpSession.
	 * @return HttpSession
	 */
	public HttpSession getSession() {
		return request.getSession();
	}

	/**
	 * Return HttpSession.
	 * 
	 * @param create
	 *            a boolean specifying create HttpSession if it not exists
	 * @return HttpSession
	 */
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}

	/**
	 * Return a Object from session.
	 * 
	 * @param key
	 *            a String specifying the key of the Object stored in session
	 * @param <T> return type
	 * @return T
	 */
	public <T> T getSessionAttr(String key) {
		HttpSession session = request.getSession(false);
		return session != null ? (T) session.getAttribute(key) : null;
	}

	/**
	 * Store Object to session.
	 * 
	 * @param key
	 *            a String specifying the key of the Object stored in session
	 * @param value
	 *            a Object specifying the value stored in session
	 * @return Controller
	 */
	public Controller setSessionAttr(String key, Object value) {
		request.getSession().setAttribute(key, value);
		return this;
	}

	/**
	 * Remove Object in session.
	 * 
	 * @param key
	 *            a String specifying the key of the Object stored in session
	 * @return Controller
	 */
	public Controller removeSessionAttr(String key) {
		HttpSession session = request.getSession(false);
		if (session != null)
			session.removeAttribute(key);
		return this;
	}

	/**
	 * Get cookie value by cookie name.
	 * @param name
	 * 			the name of a cookie
	 * @param defaultValue
	 * 			a default value of the cookie name
	 * @return String
	 */
	public String getCookie(String name, String defaultValue) {
		Cookie cookie = getCookieObject(name);
		return cookie != null ? cookie.getValue() : defaultValue;
	}

	/**
	 * Get cookie value by cookie name.
	 * @param name
	 * 			the name of a cookie
	 * @return String
	 */
	public String getCookie(String name) {
		return getCookie(name, null);
	}

	/**
	 * Get cookie value by cookie name and convert to Integer.
	 * @param name
	 * 			the name of a cookie
	 * @return Integer
	 */
	public Integer getCookieToInt(String name) {
		String result = getCookie(name);
		return result != null ? Integer.parseInt(result) : null;
	}

	/**
	 * Get cookie value by cookie name and convert to Integer.
	 * @param name
	 * 			the name of the cookie
	 * @param defaultValue
	 * 			a default value of the cookie
	 * @return Integer
	 */
	public Integer getCookieToInt(String name, Integer defaultValue) {
		String result = getCookie(name);
		return result != null ? Integer.parseInt(result) : defaultValue;
	}

	/**
	 * Get cookie value by cookie name and convert to Long.
	 * @param name
	 * 			the cookie name
	 * @return Long
	 */
	public Long getCookieToLong(String name) {
		String result = getCookie(name);
		return result != null ? Long.parseLong(result) : null;
	}

	/**
	 *  Get cookie value by cookie name and convert to Long.
	 * @param name
	 * 			the cookie name
	 * @param defaultValue
	 * 			a default value of the cookie
	 * @return Long
	 */
	public Long getCookieToLong(String name, Long defaultValue) {
		String result = getCookie(name);
		return result != null ? Long.parseLong(result) : defaultValue;
	}

	/**
	 * Get cookie object by cookie name.
	 * @param name
	 * 			the name of cookie
	 * @return Cookie
	 */
	public Cookie getCookieObject(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (Cookie cookie : cookies)
				if (cookie.getName().equals(name))
					return cookie;
		return null;
	}

	/**
	 * Get all cookie objects.
	 * @return Cookie[]
	 * 			Cookie　array
	 */
	public Cookie[] getCookieObjects() {
		Cookie[] result = request.getCookies();
		return result != null ? result : new Cookie[0];
	}

	/**
	 * Set Cookie to response.
	 * @param cookie
	 * 			the cookie will be set up
	 * @return Controller
	 */
	public Controller setCookie(Cookie cookie) {
		response.addCookie(cookie);
		return this;
	}

	/**
	 * Set Cookie to response.
	 * 
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @param maxAgeInSeconds
	 *            -1: clear cookie when close browser. 0: clear cookie
	 *            immediately. n&#62;0 : max age in n seconds.
	 * @param path
	 *            see Cookie.setPath(String)
	 * @return Controller
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds,
			String path) {
		setCookie(name, value, maxAgeInSeconds, path, null);
		return this;
	}

	/**
	 * Set Cookie to response.
	 * 
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @param maxAgeInSeconds
	 *            -1: clear cookie when close browser. 0: clear cookie
	 *            immediately. n&#62;0 : max age in n seconds.
	 * @param path
	 *            see Cookie.setPath(String)
	 * @param domain
	 *            the domain name within which this cookie is visible; form is
	 *            according to RFC 2109
	 * @return Controller
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds,
			String path, String domain) {
		Cookie cookie = new Cookie(name, value);
		if (domain != null)
			cookie.setDomain(domain);
		cookie.setMaxAge(maxAgeInSeconds);
		cookie.setPath(path);
		response.addCookie(cookie);
		return this;
	}

	/**
	 * Set Cookie with path = "/".
	 * @param name
	 * 			cookie name
	 * @param value
	 * 			cookie value
	 * @param maxAgeInSeconds
	 * 		    max age in n seconds.
	 * @return Controller
	 */
	public Controller setCookie(String name, String value, int maxAgeInSeconds) {
		setCookie(name, value, maxAgeInSeconds, "/", null);
		return this;
	}

	/**
	 * Remove Cookie with path = "/".
	 * @param name
	 * 			cookie name
	 * @return Controller
	 */
	public Controller removeCookie(String name) {
		setCookie(name, null, 0, "/", null);
		return this;
	}

	/**
	 * Remove Cookie.
	 * @param name
	 * 			cookie name
	 * @param path
	 * 			cookie path
	 * @return Controller
	 */
	public Controller removeCookie(String name, String path) {
		setCookie(name, null, 0, path, null);
		return this;
	}

	/**
	 *  Remove Cookie.
	 * @param name
	 * 			cookie name
	 * @param path
	 * 			cookie path
	 * @param domain
	 * 			domain
	 * @return Controller
	 */
	public Controller removeCookie(String name, String path, String domain) {
		setCookie(name, null, 0, path, domain);
		return this;
	}

	// --------

	/**
	 * Get all para with separator char from url
	 * @return String
	 */
	public String getPara() {
		if ("".equals(urlPara)) // urlPara maybe is "" see
								// ActionMapping.getAction(String)
			urlPara = null;
		return urlPara;
	}

	/**
	 * Get para from url. The index of first url para is 0.
	 * 多个参数传入时，本框架采用［－］作为默认分割符，原因有2
	 * 1，本框架url采用restful格式，也即每一个资源必须对应唯一url
	 * 2，为了优化seo，多加一个［／］等于多加一层目录，seo探测深度也增加
	 * @param index
	 * 		the position of the value from url
	 * @return String
	 */
	public String getPara(int index) {
		if (index < 0)
			return getPara();

		if (urlParaArray == null) {
			if (urlPara == null || "".equals(urlPara)) // urlPara maybe is ""
														// see
														// ActionMapping.getAction(String)
				urlParaArray = NULL_URL_PARA_ARRAY;
			else
				urlParaArray = urlPara.split(URL_PARA_SEPARATOR);

			for (int i = 0; i < urlParaArray.length; i++)
				if ("".equals(urlParaArray[i]))
					urlParaArray[i] = null;
		}
		// 防止数组下标溢出，采用三目运算返回
		return urlParaArray.length > index ? urlParaArray[index] : null;
	}

	/**
	 * Get para from url with default value if it is null or "".
	 * @param index
	 * 			the position of the para from url
	 * @param defaultValue
	 * 			a default value
	 * @return String
	 */
	public String getPara(int index, String defaultValue) {
		String result = getPara(index);
		return result != null && !"".equals(result) ? result : defaultValue;
	}

	/**
	 * Get para from url and conver to Integer. The first index is 0
	 * @param index
	 * 			the position of the para from url
	 * @return Integer
	 */
	public Integer getParaToInt(int index) {
		return toInt(getPara(index), null);
	}

	/**
	 * Get para from url and conver to Integer with default value if it is null.
	 * @param index
	 * 			the position of the para from url
	 * @param defaultValue
	 * 			a default value
	 * @return Integer
	 */
	public Integer getParaToInt(int index, Integer defaultValue) {
		return toInt(getPara(index), defaultValue);
	}

	/**
	 * Get para from url and conver to Long.
	 * @param index
	 * 			the position of the para from url
	 * @return Long
	 */
	public Long getParaToLong(int index) {
		return toLong(getPara(index), null);
	}

	/**
	 * Get para from url and conver to Long with default value if it is null.
	 * @param index
	 * 			the position of the para from url
	 * @param defaultValue
	 * 			a default value
	 * @return Long
	 */
	public Long getParaToLong(int index, Long defaultValue) {
		return toLong(getPara(index), defaultValue);
	}

	/**
	 * Get all para from url and convert to Integer
	 * @return Integer
	 */
	public Integer getParaToInt() {
		return toInt(getPara(), null);
	}

	/**
	 * Get all para from url and convert to Long
	 * @return Long
	 */
	public Long getParaToLong() {
		return toLong(getPara(), null);
	}

	/**
	 * Get model from http request.
	 * @param <T>
	 * 			return type
	 * @param modelClass
	 * 			class of model
	 * @return T
	 */
	public <T> T getModel(Class<T> modelClass) {
		return (T) ModelInjector.inject(modelClass, request, false);
	}

	/**
	 * Get model from http request.
	 * @param <T>
	 * 			return type
	 * @param modelClass
	 * 			class of model
	 * @param modelName
	 * 			name of model
	 * @return T
	 */
	public <T> T getModel(Class<T> modelClass, String modelName) {
		return (T) ModelInjector.inject(modelClass, modelName, request, false);
	}

	// TODO public <T> List<T> getModels(Class<T> modelClass, String modelName)
	// {}

	// --------

	/**
	 * Get upload file from multipart request.
	 * @param saveDirectory
	 * 			save directory of the file
	 * @param maxPostSize
	 * 			max size of the upload file
	 * @param encoding
	 * 			encoding of the file
	 * @return Controller
	 */
	public List<UploadFile> getFiles(String saveDirectory, Integer maxPostSize,
			String encoding) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, saveDirectory, maxPostSize,
					encoding);
		return ((MultipartRequest) request).getFiles();
	}

	/**
	 * Get upload file from multipart request.
	 * @param parameterName
	 * 			form data name
	 * @param saveDirectory
	 * 			save directory of the upload file
	 * @param maxPostSize
	 * 			max size of the upload file
	 * @param encoding
	 * 			encoding of the upload file
	 * @return Controller
	 */
	public UploadFile getFile(String parameterName, String saveDirectory,
			Integer maxPostSize, String encoding) {
		getFiles(saveDirectory, maxPostSize, encoding);
		return getFile(parameterName);
	}

	/**
	 * Get upload file from multipart request.
	 * @param saveDirectory
	 * 			save directory of the file
	 * @param maxPostSize
	 * 			max size of the file
	 * @return Controller
	 */
	public List<UploadFile> getFiles(String saveDirectory, int maxPostSize) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, saveDirectory, maxPostSize);
		return ((MultipartRequest) request).getFiles();
	}

	/**
	 * Get upload file from multipart request.
	 * @param parameterName
	 * 			form data name
	 * @param saveDirectory
	 * 			save directory of the file
	 * @param maxPostSize
	 * 			max size of the file
	 * @return Controller
	 */
	public UploadFile getFile(String parameterName, String saveDirectory,
			int maxPostSize) {
		getFiles(saveDirectory, maxPostSize);
		return getFile(parameterName);
	}

	/**
	 * Get upload file from multipart request.
	 * @param saveDirectory
	 * 			save directory of the file
	 * @return Controller
	 */
	public List<UploadFile> getFiles(String saveDirectory) {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request, saveDirectory);
		return ((MultipartRequest) request).getFiles();
	}

	/**
	 * Get upload file from multipart request.
	 * @param parameterName
	 * 		form data name
	 * @param saveDirectory
	 * 			save directory of the file
	 * @return  Controller
	 */
	public UploadFile getFile(String parameterName, String saveDirectory) {
		getFiles(saveDirectory);
		return getFile(parameterName);
	}

	/**
	 * Get upload file from multipart request.
	 * @return Files of list
	 */
	public List<UploadFile> getFiles() {
		if (request instanceof MultipartRequest == false)
			request = new MultipartRequest(request);
		return ((MultipartRequest) request).getFiles();
	}

	/**
	 * Get upload file from multipart request.
	 * @return  file
	 */
	public UploadFile getFile() {
		List<UploadFile> uploadFiles = getFiles();
		return uploadFiles.size() > 0 ? uploadFiles.get(0) : null;
	}

	/**
	 * Get upload file from multipart request.
	 * @param parameterName
	 * 			form data name
	 * @return  File
	 */
	public UploadFile getFile(String parameterName) {
		List<UploadFile> uploadFiles = getFiles();
		for (UploadFile uploadFile : uploadFiles) {
			if (uploadFile.getParameterName().equals(parameterName)) {
				return uploadFile;
			}
		}
		return null;
	}
	/**
	 * Get remote IP address from request
	 * @return  Remote ip address
	 */
	public String getRemoteIp(){
		return IpKit.getIpFromRequest(request);
	}

	/**
	 * Keep all parameter's value except model value
	 * @return  Controller
	 */
	public Controller keepPara() {
		Map<String, String[]> map = request.getParameterMap();
		for (Entry<String, String[]> e : map.entrySet()) {
			String[] values = e.getValue();
			if (values.length == 1)
				request.setAttribute(e.getKey(), values[0]);
			else
				request.setAttribute(e.getKey(), values);
		}
		return this;
	}

	/**
	 * Keep parameter's value names pointed, model value can not be kept
	 * @param names   
	 * 			the names that want to keep
	 * @return Controller
	 */
	public Controller keepPara(String... names) {
		for (String name : names) {
			String[] values = request.getParameterValues(name);
			if (values != null) {
				if (values.length == 1)
					request.setAttribute(name, values[0]);
				else
					request.setAttribute(name, values);
			}
		}
		return this;
	}

	/**
	 * Convert para to special type and keep it
	 * @param type
	 * 			type clacc
	 * @param name
	 * 			the name of a parameter
	 * @return Controller
	 */
	public Controller keepPara(Class type, String name) {
		String[] values = request.getParameterValues(name);
		if (values != null) {
			if (values.length == 1)
				try {
					request.setAttribute(name,
							TypeConverter.convert(type, values[0]));
				} catch (ParseException e) {
				}
			else
				request.setAttribute(name, values);
		}
		return this;
	}

	/**
	 * Convert para to special type and keep it
	 * @param type
	 * 			convert to type class
	 * @param names
	 * 			names of the parameters
	 * @return  Controller
	 */
	public Controller keepPara(Class type, String... names) {
		if (type == String.class)
			return keepPara(names);

		if (names != null)
			for (String name : names)
				keepPara(type, name);
		return this;
	}

	/**
	 * keep model values
	 * @param modelClass
	 * 			model class
	 * @param modelName
	 * 			model name
	 * @return  Controller
	 */
	public Controller keepModel(Class modelClass, String modelName) {
		Object model = ModelInjector.inject(modelClass, modelName, request,
				true);
		request.setAttribute(modelName, model);
		return this;
	}

	/**
	 * keep mode value
	 * @param modelClass
	 * 			model class
	 * @return Controller
	 */
	public Controller keepModel(Class modelClass) {
		String modelName = StrKit.firstCharToLowerCase(modelClass
				.getSimpleName());
		keepModel(modelClass, modelName);
		return this;
	}

	/**
	 * Create a token.
	 * 
	 * @param tokenName
	 *            the token name used in view
	 * @param secondsOfTimeOut
	 *            the seconds of time out, secondsOfTimeOut &#62;=
	 *            Const.MIN_SECONDS_OF_TOKEN_TIME_OUT
	 */
	public void createToken(String tokenName, int secondsOfTimeOut) {
		org.sxb.token.TokenManager.createToken(this, tokenName,
				secondsOfTimeOut);
	}

	/**
	 * Create a token with default token name and with default seconds of time
	 * out.
	 */
	public void createToken() {
		createToken(Const.DEFAULT_TOKEN_NAME,
				Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
	}

	/**
	 * Create a token with default seconds of time out.
	 * 
	 * @param tokenName
	 *            the token name used in view
	 */
	public void createToken(String tokenName) {
		createToken(tokenName, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
	}

	/**
	 * Check token to prevent resubmit.
	 * 
	 * @param tokenName
	 *            the token name used in view's form
	 * @return true if token is correct
	 */
	public boolean validateToken(String tokenName) {
		return org.sxb.token.TokenManager.validateToken(this, tokenName);
	}

	/**
	 * Check token to prevent resubmit with default token key 
	 * "SXB_TOKEN_KEY"
	 * 
	 * @return true if token is correct
	 */
	public boolean validateToken() {
		return validateToken(Const.DEFAULT_TOKEN_NAME);
	}

	/**
	 * Return true if the para value is blank otherwise return false
	 * @param paraName
	 * 			parameter name
	 * @return boolean
	 */
	public boolean isParaBlank(String paraName) {
		String value = request.getParameter(paraName);
		return value == null || value.trim().length() == 0;
	}

	/**
	 * Return true if the urlPara value is blank otherwise return false
	 * @param index
	 * 		the position of the para from url
	 * @return boolean
	 */
	public boolean isParaBlank(int index) {
		String value = getPara(index);
		return value == null || value.trim().length() == 0;
	}

	/**
	 * Return true if the para exists otherwise return false
	 * @param paraName
	 * 			parameter name
	 * @return boolean
	 */
	public boolean isParaExists(String paraName) {
		return request.getParameterMap().containsKey(paraName);
	}

	/**
	 * Return true if the urlPara exists otherwise return false
	 * @param index
	 * 			the position of parameter from url
	 * @return boolean
	 */
	public boolean isParaExists(int index) {
		return getPara(index) != null;
	}

	// ----------------
	// render below ---
	private static final RenderFactory renderFactory = RenderFactory.me();

	/**
	 * Hold Render object when invoke renderXxx(...)
	 */
	private Render render;

	/**
	 * get the default render
	 * @return Render
	 */
	public Render getRender() {
		return render;
	}

	/**
	 * Render with any Render which extends Render
	 * @param render
	 * 		the view render
	 */
	public void render(Render render) {
		this.render = render;
	}

	/**
	 * Render with view use default type Render configured in SxbConfig
	 * @param view
	 * 			view name
	 */
	public void render(String view) {
		render = renderFactory.getRender(view);
	}

	/**
	 * Render with jsp view
	 * @param view
	 * 			view name
	 */
	public void renderJsp(String view) {
		render = renderFactory.getJspRender(view);
	}

	/**
	 * Render with freemarker view
	 * @param view
	 * 			view name
	 */
	public void renderFreeMarker(String view) {
		render = renderFactory.getFreeMarkerRender(view);
	}

	/**
	 * Render with velocity view
	 * @param view
	 * 			view name
	 */
	public void renderVelocity(String view) {
		render = renderFactory.getVelocityRender(view);
	}

	/**
	 * Render with json
	 * @param key
	 * 			json key
	 * @param value 
	 * 			json value
	 * <p>
	 * Example:<br>
	 * renderJson("message", "Save successful");<br>
	 * renderJson("users", users);<br>
	 */
	public void renderJson(String key, Object value) {
		render = renderFactory.getJsonRender(key, value);
	}

	/**
	 * Render with json
	 */
	public void renderJson() {
		render = renderFactory.getJsonRender();
	}

	/**
	 * Render with attributes set by setAttr(...) before.
	 * @param attrs
	 * 			attribute values
	 * <p>
	 * Example: renderJson(new String[]{"blogList", "user"});
	 */
	public void renderJson(String[] attrs) {
		render = renderFactory.getJsonRender(attrs);
	}

	/**
	 * Render with json text.
	 * @param jsonText	
	 * 			json String
	 * <p>
	 * Example: renderJson("{\"message\":\"Please input password!\"}");
	 */
	public void renderJson(String jsonText) {
		render = renderFactory.getJsonRender(jsonText);
	}

	/**
	 * Render json with object.
	 * @param object
	 * 			Java object 
	 * <p>
	 * Example: renderJson(new User().set("name", "Sxb").set("age", 18));
	 */
	public void renderJson(Object object) {
		render = renderFactory.getJsonRender(object);
	}

	/**
	 * Render CSV with object.
	 * @param headers
	 * 			CSV header titles
	 * @param data
	 * 			content data
	 * <p>
	 * Example: {@code renderCsv(List&#60;String&#62; headers, List&#60;&#63;&#62; data);}
	 */
	public void renderCsv(List<String> headers, List<?> data) {
		render = renderFactory.getCsvRender(headers, data);
	}
	
	/**
	 * Render Jasper with view and format.
	 * @param view
	 * 			view name
	 * @param  format
	 * 			report type eg. (PDF,Excel,Word,PowerPoint)
	 * <p>
	 * Example: {@code renderJasper();}
	 */
	public void renderReport(String view, ReportType format) {
		render = renderFactory.getReportRender(view, format);
	}
	/**
	 * Rend jasper with the view and format and data result set
	 * @param view	
	 * 			view name
	 * @param format
	 * 			report type
	 * @param rs
	 * 			result set
	 */
	public void renderReport(String view, ReportType format, ResultSet rs) {
		render = renderFactory.getReportRender(view, format);
	}
	/**
	 * rend jasper with view and format and data source
	 * @param view
	 * 			view name
	 * @param format
	 * 			report type
	 * @param dataSource
	 * 			data source
	 */
	public void renderReport(String view, ReportType format, Collection<Map<String, ?>> dataSource) {
		render = renderFactory.getReportRender(view, format);
	}

	/**
	 * Render with text. The contentType is: "text/plain".
	 * @param text
	 * 		content string
	 */
	public void renderText(String text) {
		render = renderFactory.getTextRender(text);
	}

	/**
	 * Render with text and content type.
	 * @param text
	 * 			content string
	 * @param contentType
	 * 			content type
	 * <p>
	 * Example: {@code renderText("&lt;user id='5888'&gt;James&lt;/user&gt;",
	 * "application/xml");}
	 */
	public void renderText(String text, String contentType) {
		render = renderFactory.getTextRender(text, contentType);
	}
	

	/**
	 * Render with text and ContentType.
	 * @param text	
	 * 			content string
	 * @param contentType
	 * 			content type
	 * <p>
	 * Example: renderText("&lt;html&gt;Hello James&lt;/html&gt;",
	 * ContentType.HTML);
	 */
	public void renderText(String text, ContentType contentType) {
		render = renderFactory.getTextRender(text, contentType);
	}

	/**
	 * Forward to an action
	 * @param actionUrl
	 * 			action url
	 */
	public void forwardAction(String actionUrl) {
		render = new ActionRender(actionUrl);
	}

	/**
	 * Render with file
	 * @param fileName
	 * 			download file name
	 */
	public void renderFile(String fileName) {
		render = renderFactory.getFileRender(fileName);
	}

	/**
	 * Render with file
	 * @param file
	 * 			download file
	 */
	public void renderFile(File file) {
		render = renderFactory.getFileRender(file);
	}

	/**
	 * Redirect to url
	 * @param url
	 * 			redirect url
	 */
	public void redirect(String url) {
		render = renderFactory.getRedirectRender(url);
	}

	/**
	 * Redirect to url
	 * @param url
	 * 		redirect url
	 * @param withQueryString
	 * 			redirect with parameter
	 */
	public void redirect(String url, boolean withQueryString) {
		render = renderFactory.getRedirectRender(url, withQueryString);
	}

	/**
	 * Render with view and status use default type Render configured in
	 * SxbConfig
	 * @param view
	 * 			view name
	 * @param status
	 * 			status code
	 */
	public void render(String view, int status) {
		render = renderFactory.getRender(view);
		response.setStatus(status);
	}

	/**
	 * Render with url and 301 status
	 * @param url
	 * 		301 redirect url
	 */
	public void redirect301(String url) {
		render = renderFactory.getRedirect301Render(url);
	}

	/**
	 * Render with url and 301 status
	 * @param url
	 * 		301 redirect url
	 * @param withQueryString
	 * 		query value
	 */
	public void redirect301(String url, boolean withQueryString) {
		render = renderFactory.getRedirect301Render(url, withQueryString);
	}

	/**
	 * Render with view and errorCode status
	 * @param errorCode
	 * 			error code
	 * @param view
	 * 			view name
	 */
	public void renderError(int errorCode, String view) {
		throw new ActionException(errorCode, renderFactory.getErrorRender(
				errorCode, view));
	}

	/**
	 * Render with render and errorCode status
	 * @param errorCode
	 * 			error code
	 * @param render
	 * 			view render
	 */
	public void renderError(int errorCode, Render render) {
		throw new ActionException(errorCode, render);
	}

	/**
	 * Render with view and errorCode status configured in SxbConfig
	 * @param errorCode
	 * 			error code
	 */
	public void renderError(int errorCode) {
		throw new ActionException(errorCode,
				renderFactory.getErrorRender(errorCode));
	}

	/**
	 * Render nothing, no response to browser
	 */
	public void renderNull() {
		render = renderFactory.getNullRender();
	}

	/**
	 * Render with javascript text. The contentType is: "text/javascript".
	 * @param javascriptText
	 * 			java script string
	 */
	public void renderJavascript(String javascriptText) {
		render = renderFactory.getJavascriptRender(javascriptText);
	}

	/**
	 * Render with html text. The contentType is: "text/html".
	 * @param htmlText
	 * 			html string
	 */
	public void renderHtml(String htmlText) {
		render = renderFactory.getHtmlRender(htmlText);
	}

	/**
	 * Render with xml view using freemarker.
	 * @param view
	 * 		view name
	 */
	public void renderXml(String view) {
		render = renderFactory.getXmlRender(view);
	}

	/**
	 * check url parameter
	 * @param minLength
	 * 			min length
	 * @param maxLength
	 * 			max length
	 */
	public void checkUrlPara(int minLength, int maxLength) {
		getPara(0);
		if (urlParaArray.length < minLength || urlParaArray.length > maxLength)
			renderError(404);
	}

	/**
	 * check url parameter
	 * @param length
	 * 			length
	 */
	public void checkUrlPara(int length) {
		checkUrlPara(length, length);
	}

	// AOP切入 ---------

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @return T
	 */
	public <T> T enhance(Class<T> targetClass) {
		return (T) Enhancer.enhance(targetClass);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @param injectInters
	 * 			inject inters
	 * @return T
	 */
	public <T> T enhance(Class<T> targetClass, Interceptor... injectInters) {
		return (T) Enhancer.enhance(targetClass, injectInters);
	}

	/**
	 * 
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @param injectIntersClasses
	 * 			inject inter classes
	 * @return T
	 */
	public <T> T enhance(Class<T> targetClass,
			Class<? extends Interceptor>... injectIntersClasses) {
		return (T) Enhancer.enhance(targetClass, injectIntersClasses);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @param injectIntersClass
	 *   		inject inters class
	 * @return T
	 */
	public <T> T enhance(Class<T> targetClass,
			Class<? extends Interceptor> injectIntersClass) {
		return (T) Enhancer.enhance(targetClass, injectIntersClass);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @param injectIntersClass1
	 *   		inject inters class1
	 * @param injectIntersClass2
	 *   		inject inters class2
	 * @return T
	 */
	public <T> T enhance(Class<T> targetClass,
			Class<? extends Interceptor> injectIntersClass1,
			Class<? extends Interceptor> injectIntersClass2) {
		return (T) Enhancer.enhance(targetClass, injectIntersClass1,
				injectIntersClass2);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param targetClass
	 * 			target class
	 * @param injectIntersClass1
	 *   		inject inters class1
	 * @param injectIntersClass2
	 *   		inject inters class2
	 * @param injectIntersClass3
	 *  		inject inters class3
	 * @return  T
	 */
	public <T> T enhance(Class<T> targetClass,
			Class<? extends Interceptor> injectIntersClass1,
			Class<? extends Interceptor> injectIntersClass2,
			Class<? extends Interceptor> injectIntersClass3) {
		return (T) Enhancer.enhance(targetClass, injectIntersClass1,
				injectIntersClass2, injectIntersClass3);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param targetClass
	 * 			target class
	 * @return T
	 */
	public <T> T enhance(String singletonKey, Class<T> targetClass) {
		return (T) Enhancer.enhance(singletonKey, targetClass);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param targetClass
	 * 			target class
	 * @param injectInters
	 *			inject inters
	 * @return T
	 */
	public <T> T enhance(String singletonKey, Class<T> targetClass,
			Interceptor... injectInters) {
		return (T) Enhancer.enhance(singletonKey, targetClass, injectInters);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param targetClass
	 * 			target class
	 * @param injectIntersClasses
	 *  		inject inters class
	 * @return T
	 */
	public <T> T enhance(String singletonKey, Class<T> targetClass,
			Class<? extends Interceptor>... injectIntersClasses) {
		return (T) Enhancer.enhance(singletonKey, targetClass,
				injectIntersClasses);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @return T
	 */
	public <T> T enhance(Object target) {
		return (T) Enhancer.enhance(target);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @param injectInters
	 *  		inject inters
	 * @return T
	 */
	public <T> T enhance(Object target, Interceptor... injectInters) {
		return (T) Enhancer.enhance(target, injectInters);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @param injectIntersClasses
	 *  		inject inters class
	 * @return T
	 */
	public <T> T enhance(Object target,
			Class<? extends Interceptor>... injectIntersClasses) {
		return (T) Enhancer.enhance(target, injectIntersClasses);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @param injectIntersClass
	 *  		inject inters class
	 * @return T
	 */
	public <T> T enhance(Object target,
			Class<? extends Interceptor> injectIntersClass) {
		return (T) Enhancer.enhance(target, injectIntersClass);
	}

	/**
	 * enhance method
	 *  @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @param injectIntersClass1
	 *  			inject inters class1
	 * @param injectIntersClass2
	 *  			inject inters class2
	 * @return T
	 */
	public <T> T enhance(Object target,
			Class<? extends Interceptor> injectIntersClass1,
			Class<? extends Interceptor> injectIntersClass2) {
		return (T) Enhancer.enhance(target, injectIntersClass1,
				injectIntersClass2);
	}

	/**
	 * enhance method
	 *  @param <T>
	 * 			return type
	 * @param target
	 * 			target
	 * @param injectIntersClass1
	 *  			inject inters class1
	 * @param injectIntersClass2
	 *  			inject inters class3
	 * @param injectIntersClass3
	 * 			inject inters class 3
	 * @return T
	 */
	public <T> T enhance(Object target,
			Class<? extends Interceptor> injectIntersClass1,
			Class<? extends Interceptor> injectIntersClass2,
			Class<? extends Interceptor> injectIntersClass3) {
		return (T) Enhancer.enhance(target, injectIntersClass1,
				injectIntersClass2, injectIntersClass3);
	}

	/**
	 * enhance method
	 *  @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param target
	 * 			target
	 * @return T
	 */
	public <T> T enhance(String singletonKey, Object target) {
		return (T) Enhancer.enhance(singletonKey, target);
	}

	/**
	 * enhance method
	 *  @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param target
	 * 			target
	 * @param injectInters
	 * 			inject inters
	 * @return T
	 */
	public <T> T enhance(String singletonKey, Object target,
			Interceptor... injectInters) {
		return (T) Enhancer.enhance(singletonKey, target, injectInters);
	}

	/**
	 * enhance method
	 * @param <T>
	 * 			return type
	 * @param singletonKey
	 * 			singleton key
	 * @param target
	 * 			target
	 * @param injectIntersClasses
	 * 			inject inters classes
	 * @return  T
	 */
	public <T> T enhance(String singletonKey, Object target,
			Class<? extends Interceptor>... injectIntersClasses) {
		return (T) Enhancer.enhance(singletonKey, target, injectIntersClasses);
	}
	
	/**
	 * commons file upload
	 * get file
	 * @param filename
	 * 		form data file name
	 * @return  MultipartFile
	 */
	public MultipartFile getMultipartFile(String filename){
		if(request instanceof MultipartHttpServletRequest == false){
			request = (MultipartHttpServletRequest)MultiPartUtil.wrapMultiPartRequest(request, response);
		}
		
		return ((MultipartHttpServletRequest)request).getFile(filename);
	}
	
	/**
	 * commons file upload file 
	 * get files
	 * @return  List
	 */
	public List<MultipartFile> getMultipartFiles(){
		if(request instanceof MultipartHttpServletRequest == false){
			request = (MultipartHttpServletRequest)MultiPartUtil.wrapMultiPartRequest(request, response);
			
		}
		List<MultipartFile> files = new ArrayList<MultipartFile>();
		for(Entry<String, List<MultipartFile>> entry:((MultipartHttpServletRequest)request).getMultiFileMap().entrySet()){
			List<MultipartFile> multipartFiles = entry.getValue();
			files.addAll(multipartFiles);
		}
		return files;
	}
	
}
