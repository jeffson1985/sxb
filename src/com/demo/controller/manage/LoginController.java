package com.demo.controller.manage;

import org.sxb.aop.Before;
import org.sxb.core.Controller;

import com.demo.model.Admin;
import com.demo.service.AdminService;
import com.demo.validator.LoginValidator;

//@ControllerBind(controllerKey = "/manage/login")
public class LoginController extends Controller {

	private AdminService adminService = new AdminService();

	public void index() {
		render("login.html");
	}

	@Before(LoginValidator.class)
	public void checkLogin() {
		Admin admin = getModel(Admin.class);
		if (admin.getCount() == 0) {
			admin.save();
		}
		if (adminService.checkLogin(admin) != null) {
			setSessionAttr("logined_admin", admin);
			redirect("/manage");
		} else {
			keepModel(Admin.class);
			this.setAttr("usernameMsg", "ユーザーとパスワードは一致ではらりません");
			render("login.html");
		}
	}

	public void logout() {
		this.removeSessionAttr("logined_admin");
		redirect("/login");
	}
}
