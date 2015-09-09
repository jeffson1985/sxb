package com.demo.controller;

import org.sxb.core.Controller;
import org.sxb.ext.render.CaptchaRender;
import org.sxb.ext.route.ControllerBind;
@ControllerBind(controllerKey = "/common")
public class CommonController extends Controller {
	
	public void imgCode(){
		CaptchaRender img = new CaptchaRender("mykey");
		
		render(img);
	}

}
