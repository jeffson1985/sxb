package com.demo.interceptor;

import org.sxb.aop.Interceptor;
import org.sxb.aop.Invocation;
import org.sxb.core.Controller;

import com.demo.model.Admin;

public class AccessInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		
		inv.invoke();
		Controller c = inv.getController();
			
		//System.out.println("status: " + c.getSession().getAttribute("status"));
		
		Admin loginedAdmin = (Admin)c.getSessionAttr("logined_admin");
		if(loginedAdmin == null){
			c.redirect("/manage/login");
			return;
		}
		

	}

}
