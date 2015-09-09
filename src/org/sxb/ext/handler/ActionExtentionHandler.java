package org.sxb.ext.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sxb.handler.Handler;
import org.sxb.kit.HandlerKit;
import org.sxb.kit.StrKit;


/**
 * URL访问后缀处理 html
 * @author Sun
 *
 */
public class ActionExtentionHandler extends Handler {
	
	private String viewPostfix;
	
	// 默认使用html后缀
	public ActionExtentionHandler(){
		this.viewPostfix = ".html";
	}
	// 自行设定后缀
	public ActionExtentionHandler(String viewPostfix){
		if (StrKit.isBlank(viewPostfix))
			throw new IllegalArgumentException("viewPostfix can not be blank.");
		this.viewPostfix = viewPostfix;
	}
	

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		
		if ("/".equals(target)) {
			nextHandler.handle(target, request, response, isHandled);
			return;
		}
		// 此处采用强制加后缀方式，如果设定此控制器后，必须在url后边加扩展名，否则抛出404错误
		// 并且强制使用设定的后缀
		if (target.indexOf('.') == -1) {
		//if (!target.endsWith(viewPostfix)){
			System.out.println("Wrong postfix");
			HandlerKit.renderError404(request, response, isHandled);
			return ;
		}
		
		// 如果存在后缀，排除后缀
		int index = target.lastIndexOf(viewPostfix);
		if (index != -1)
			target = target.substring(0, index);
		
		nextHandler.handle(target, request, response, isHandled);
		
		
		/*
		if(target.endsWith(viewPostfix)){
			target = target.substring(0 , target.lastIndexOf("."));
			nextHandler.handle(target, request, response, isHandled);
		}else{
			nextHandler.handle(target, request, response, isHandled);
		}
		*/
		
	}

}
