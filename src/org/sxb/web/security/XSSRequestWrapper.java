package org.sxb.web.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.sxb.kit.html.HtmlKits;
/**
 * XSS攻击用Request包装类
 * @author Jeffson
 *
 */
public class XSSRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

	public XSSRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	/**
	 * 重写并过滤getParameter方法
	 */
	@Override
	public String getParameter(String name) {
		return HtmlKits.htmlEscape(super.getParameter(name));
	}

	/**
	 * 重写并过滤getParameterValues方法
	 */
	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (null == values) {
			return null;
		}
		String[] newValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			newValues[i] = HtmlKits.htmlEscape(values[i]);
		}
		return newValues;
	}

	/**
	 * 重写并过滤getParameterMap方法
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paraMap = super.getParameterMap();
		// 对于paraMap为空的直接return
		if (null == paraMap || paraMap.isEmpty()) {
			return paraMap;
		}
		Map<String, String[]> newParaMap = new HashMap<String, String[]>();
		for (Entry<String, String[]> entry : paraMap.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			if (null == values) {
				continue;
			}
			String[] newValues = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				newValues[i] = HtmlKits.htmlEscape(values[i]);
			}
			newParaMap.put(key, newValues);
		}
		return newParaMap;
	}
}