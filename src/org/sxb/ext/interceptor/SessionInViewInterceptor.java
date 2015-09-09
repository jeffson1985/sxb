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

package org.sxb.ext.interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.sxb.aop.Interceptor;
import org.sxb.aop.Invocation;
import org.sxb.core.Controller;

/**
 * SessionInViewInterceptor.
 * 将Session放入视图的拦截器类
 */
public class SessionInViewInterceptor implements Interceptor {
	
	private boolean createSession = false;
	
	public SessionInViewInterceptor() {
	}
	
	public SessionInViewInterceptor(boolean createSession) {
		this.createSession = createSession;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})	
	public void intercept(Invocation inv) {
		inv.invoke();
		
		Controller c = inv.getController();
		if (c.getRender() instanceof org.sxb.render.JsonRender)
			return ;
		
		HttpSession hs = c.getSession(createSession);
		//System.out.println("拦截器：" + c.getSession().getClass().getSuperclass().getName());
		if (hs != null) {
			Map session = new SxbSession(hs);
			for (Enumeration<String> names=hs.getAttributeNames(); names.hasMoreElements();) {
				String name = names.nextElement();
				//System.out.println(name + ":视图:" + hs.getAttribute(name));
				session.put(name, hs.getAttribute(name));
			}
			c.setAttr("session", session);
		}
	}
}

@SuppressWarnings({"rawtypes", "deprecation"})
class SxbSession extends HashMap implements HttpSession {
	private static final long serialVersionUID = -6148316613614087335L;
	private HttpSession session;
	
	public SxbSession(HttpSession session) {
		this.session = session;
	}
	
	public Object getAttribute(String key) {
		return session.getAttribute(key);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return session.getAttributeNames();
	}
	
	public long getCreationTime() {
		return session.getCreationTime();
	}
	
	public String getId() {
		return session.getId();
	}
	
	public long getLastAccessedTime() {
		return session.getLastAccessedTime();
	}
	
	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}
	
	public ServletContext getServletContext() {
		return session.getServletContext();
	}
	
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		return session.getSessionContext();
	}
	
	public Object getValue(String key) {
		return session.getValue(key);
	}
	
	public String[] getValueNames() {
		return session.getValueNames();
	}
	
	public void invalidate() {
		session.invalidate();
	}
	
	public boolean isNew() {
		return session.isNew();
	}
	
	public void putValue(String key, Object value) {
		session.putValue(key, value);
	}
	
	public void removeAttribute(String key) {
		session.removeAttribute(key);
	}
	
	public void removeValue(String key) {
		session.removeValue(key);
	}
	
	public void setAttribute(String key, Object value) {
		session.setAttribute(key, value);
	}
	
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		session.setMaxInactiveInterval(maxInactiveInterval);
	}
}

/*
public void intercept(Invocation inv) {
	inv.invoke();
	
	Controller c = inv.getController();
	HttpSession hs = c.getSession(createSession);
	if (hs != null) {
		c.setAttr("session", new SxbSession(hs));
	}
}
*/
