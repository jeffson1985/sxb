package com.demo.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sxb.handler.Handler;



public class AcitonExtentionHandler extends Handler {

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		
		if(target.endsWith(".html")){
			target = target.substring(0 , target.lastIndexOf("."));
			nextHandler.handle(target, request, response, isHandled);
		}else{
			nextHandler.handle(target, request, response, isHandled);
		}
		
	}

}
