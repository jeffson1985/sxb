package com.demo.validator;


import org.sxb.core.Controller;
import org.sxb.ext.render.CaptchaRender;
import org.sxb.validate.Validator;

import com.demo.model.Admin;

public class LoginValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		this.validateRequired("admin.username", "usernameMsg", "ユーザーIDを入力してください");
		this.validateRequired("admin.password", "passwordMsg", "パスワードを入力してください");
		boolean rst = CaptchaRender.validate(c, c.getPara("code"), "mykey");
		String remberMe = c.getPara("rember_me");
		if("on".equals(remberMe)){
			c.setCookie("username", c.getPara("admin.username"), 30*60);
			c.setCookie("password", c.getPara("admin.password"), 30 * 60);
		}else{
			c.removeCookie("username");
			c.removeCookie("password");
		}
		if(!rst){
			c.setAttr("code", c.getPara("code"));
			addError("imgCodeMsg", "検証コードエラー");
			return;
		}
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Admin.class);
		c.render("login.html");
	}

}
