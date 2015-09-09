package com.demo.validator;

import org.sxb.core.Controller;
import org.sxb.validate.Validator;

import com.demo.model.Admin;

public class AdminValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequired("admin.username", "usernameMsg", "ユーザーIDを入力してください");
		validateRequired("admin.password", "passwordMsg", "パスワードを入力してください");
		validateToken("adminToken", "tokenMsg", "alert('上次已保存，请不要重复提交')");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Admin.class);
		c.render("login.html");
	}

}
